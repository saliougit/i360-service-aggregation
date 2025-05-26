package com.innov4africa.service_aggregation.service;

import java.io.StringReader;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innov4africa.service_aggregation.model.BalanceHistory;
import com.innov4africa.service_aggregation.model.BalanceHistoryPoint;
import com.innov4africa.service_aggregation.model.BalanceHistoryResponse;
import com.innov4africa.service_aggregation.model.GlobalBalanceResponse;
import com.innov4africa.service_aggregation.model.IBankingBalanceResponse;
import com.innov4africa.service_aggregation.model.ServiceStatus;
import com.innov4africa.service_aggregation.model.Period;

import reactor.core.publisher.Mono;

@Service
public class AggregationService {
    private static final Logger logger = LoggerFactory.getLogger(AggregationService.class);
    private static final String CACHE_KEY_PREFIX = "balance:";
    private static final long CACHE_TTL_SECONDS = 300; // 5 minutes
    
    @Autowired
    private IPayService ipayService;
    
    @Autowired
    private IBankingService iBankingService;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private InMemoryBalanceCache memoryCache;

    // Map Period to aggregation intervals
    private Duration getIntervalForPeriod(Period period) {
        Duration interval;
        switch (period) {
            case WEEK -> interval = Duration.ofDays(1);      // Daily aggregation for week view
            case MONTH -> interval = Duration.ofDays(7);     // Weekly aggregation for month view  
            case YEAR -> interval = Duration.ofDays(30);     // Monthly aggregation for year view
            default -> interval = Duration.ofDays(1);        // Default to daily
        }
        return interval;
    }
    
    /**
     * Agrège les soldes de iPay et iBanking pour un utilisateur
     */
    public Mono<GlobalBalanceResponse> getGlobalBalance(String telephone, String email, String ipayToken) {
        String cacheKey = CACHE_KEY_PREFIX + telephone + ":" + email;
        
        // Essayer d'abord Redis
        GlobalBalanceResponse cachedBalance = null;
        try {
            String cachedValue = redisTemplate.opsForValue().get(cacheKey);
            if (cachedValue != null) {
                logger.info("Utilisation du cache Redis pour le solde global - telephone: {}", telephone);
                ObjectMapper mapper = new ObjectMapper();
                cachedBalance = mapper.readValue(cachedValue, GlobalBalanceResponse.class);
            }
        } catch (Exception e) {
            logger.warn("Redis indisponible: {}", e.getMessage());
            // Si Redis est down, essayer le cache en mémoire
            cachedBalance = memoryCache.get(telephone, email);
        }

        if (cachedBalance != null) {
            return Mono.just(cachedBalance);
        }

        List<ServiceStatus> services = new ArrayList<>();
        // 1. Appel parallèle des services iPay et iBanking
        return Mono.zip(
            ipayService.getSolde(telephone, ipayToken),
            iBankingService.getSolde(email)
        ).flatMap(tuple -> {
            String ipayXmlResponse = tuple.getT1();
            IBankingBalanceResponse iBankingResponse = tuple.getT2();
            String montantIPay = "0.00";
            String montantIBanking = "0.00";

            try {
                // Parser la réponse XML iPay
                Document doc = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder()
                        .parse(new InputSource(new StringReader(ipayXmlResponse)));
                
                var xpath = XPathFactory.newInstance().newXPath();
                String error = xpath.evaluate("//return/error", doc);
                String message = xpath.evaluate("//return/message", doc);

                if (!"0".equals(error)) {
                    services.add(new ServiceStatus("i-pay", false, message));
                } else {
                    montantIPay = xpath.evaluate("//return/montant", doc);
                    services.add(new ServiceStatus("i-pay", true, "Solde récupéré"));
                }

                // Traiter la réponse iBanking
                if ("success".equals(iBankingResponse.getStatus())) {
                    montantIBanking = iBankingResponse.getMontant();
                    services.add(new ServiceStatus("i-banking", true, "Solde récupéré"));
                } else {
                    services.add(new ServiceStatus("i-banking", false, iBankingResponse.getMessage()));
                }

                // Calculer le total
                double total = Double.parseDouble(montantIPay.replace(",", ".")) 
                            + Double.parseDouble(montantIBanking.replace(",", "."));
                
                GlobalBalanceResponse response = new GlobalBalanceResponse(
                    "success",
                    "Solde global récupéré",
                    String.format("%.2f", total),
                    montantIPay,
                    montantIBanking,
                    services
                );

                // Essayer de mettre en cache Redis
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    String jsonValue = mapper.writeValueAsString(response);
                    redisTemplate.opsForValue().set(cacheKey, jsonValue, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
                } catch (Exception e) {
                    logger.warn("Impossible de mettre en cache Redis: {}", e.getMessage());
                    // Utiliser le cache en mémoire comme fallback
                    memoryCache.store(telephone, email, response);
                }

                return Mono.just(response);

            } catch (Exception e) {
                logger.error("Erreur de traitement de la réponse iPay", e);
                return Mono.just(new GlobalBalanceResponse(
                    "error",
                    "Erreur technique",
                    "0.00",
                    "0.00",
                    "0.00",
                    List.of(new ServiceStatus("i-pay", false, "Erreur de traitement"),
                           new ServiceStatus("i-banking", false, "Service en cours d'implémentation"))
                ));
            }
        })
        .onErrorResume(e -> {
            logger.error("Erreur lors de la récupération des soldes", e);
            return Mono.just(new GlobalBalanceResponse(
                "error",
                "Service indisponible",
                "0.00",
                "0.00",
                "0.00",
                List.of(new ServiceStatus("i-pay", false, "Service indisponible"),
                       new ServiceStatus("i-banking", false, "Service en cours d'implémentation"))
            ));
        });
    }

    /**
     * Récupère l'historique consolidé des soldes iPay et iBanking
     */
    // public Mono<BalanceHistoryResponse> getBalanceHistory(String telephone, String email, String ipayToken, String accountId, LocalDateTime startDate, LocalDateTime endDate) {
    //     String cacheKey = CACHE_KEY_PREFIX + "history:" + telephone + ":" + email + ":" + startDate + ":" + endDate;
        
    //     // Vérification du cache Redis
    //     try {
    //         String cachedValue = redisTemplate.opsForValue().get(cacheKey);
    //         if (cachedValue != null) {
    //             logger.info("Utilisation du cache Redis pour l'historique - telephone: {}", telephone);
    //             ObjectMapper mapper = new ObjectMapper();
    //             return Mono.just(mapper.readValue(cachedValue, BalanceHistoryResponse.class));
    //         }
    //     } catch (Exception e) {
    //         logger.warn("Redis indisponible: {}", e.getMessage());
    //     }
        
    //     // Récupération parallèle des historiques
    //     return Mono.zip(
    //         ipayService.getBalanceHistory(ipayToken, accountId, startDate, endDate),
    //         iBankingService.getBalanceHistory(email, startDate, endDate)
    //     ).map(tuple -> {
    //         List<BalanceHistoryPoint> ipayHistory = tuple.getT1();
    //         List<BalanceHistoryPoint> iBankingHistory = tuple.getT2();
            
    //         // Fusion et agrégation des historiques
    //         Map<LocalDateTime, BalanceHistoryPoint> mergedHistory = new TreeMap<>();
            
    //         // Traitement de l'historique iPay
    //         for (BalanceHistoryPoint point : ipayHistory) {
    //             mergedHistory.put(point.getDate(), point);
    //         }
            
    //         // Fusion avec l'historique iBanking
    //         for (BalanceHistoryPoint point : iBankingHistory) {
    //             LocalDateTime date = point.getDate();
    //             BalanceHistoryPoint existingPoint = mergedHistory.get(date);
    //               if (existingPoint != null) {
    //                 // Mettre à jour le point existant avec les données iBanking
    //                 existingPoint.setiBankingBalance(point.getiBankingBalance());
    //                 existingPoint.setGlobalBalance(existingPoint.getiPayBalance() + point.getiBankingBalance());
    //             } else {
    //                 // Créer un nouveau point
    //                 mergedHistory.put(date, point);
    //             }
    //         }
            
    //         BalanceHistoryResponse response = new BalanceHistoryResponse(
    //             "success",
    //             "Historique consolidé récupéré avec succès",
    //             new ArrayList<>(mergedHistory.values()),
    //             List.of(
    //                 new ServiceStatus("i-pay", true, "Historique récupéré"),
    //                 new ServiceStatus("i-banking", true, "Historique récupéré")
    //             )
    //         );
            
    //         // Mise en cache Redis
    //         try {
    //             ObjectMapper mapper = new ObjectMapper();
    //             String jsonValue = mapper.writeValueAsString(response);
    //             redisTemplate.opsForValue().set(cacheKey, jsonValue, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
    //         } catch (Exception e) {
    //             logger.warn("Impossible de mettre en cache Redis: {}", e.getMessage());
    //         }
            
    //         return response;
    //     })
    //     .onErrorResume(e -> {
    //         logger.error("Erreur lors de la récupération de l'historique", e);
    //         return Mono.just(new BalanceHistoryResponse(
    //             "error",
    //             "Erreur lors de la récupération de l'historique",
    //             List.of(),
    //             List.of(
    //                 new ServiceStatus("i-pay", false, "Service indisponible"),
    //                 new ServiceStatus("i-banking", false, "Service indisponible")
    //             )
    //         ));
    //     });
    // /**
    //  * Récupère l'historique consolidé des soldes iPay et iBanking avec filtrage par période
    //  */
    // public Mono<BalanceHistoryResponse> getBalanceHistory(String telephone, String email, String ipayToken, String accountId, 
    //         LocalDateTime startDate, LocalDateTime endDate, Period period) {
    //     String cacheKey = CACHE_KEY_PREFIX + "history:" + telephone + ":" + email + ":" + startDate + ":" + endDate + ":" + period;
        
    //     // Vérification du cache Redis
    //     try {
    //         String cachedValue = redisTemplate.opsForValue().get(cacheKey);
    //         if (cachedValue != null) {
    //             logger.info("Utilisation du cache Redis pour l'historique - telephone: {}, période: {}", telephone, period);
    //             ObjectMapper mapper = new ObjectMapper();
    //             return Mono.just(mapper.readValue(cachedValue, BalanceHistoryResponse.class));
    //         }
    //     } catch (Exception e) {
    //         logger.warn("Redis indisponible: {}", e.getMessage());
    //     }
        
    //     // Récupération parallèle des historiques
    //     return Mono.zip(
    //         ipayService.getBalanceHistory(ipayToken, accountId, startDate, endDate, period),
    //         iBankingService.getBalanceHistory(email, startDate, endDate, period)
    //     ).map(tuple -> {
    //         List<BalanceHistoryPoint> ipayHistory = tuple.getT1();
    //         List<BalanceHistoryPoint> iBankingHistory = tuple.getT2();
            
    //         // Fusion et agrégation des historiques
    //         Map<LocalDateTime, BalanceHistoryPoint> mergedHistory = new TreeMap<>();
            
    //         // Traitement de l'historique iPay
    //         for (BalanceHistoryPoint point : ipayHistory) {
    //             mergedHistory.put(point.getRawDate(), point);
    //         }
            
    //         // Fusion avec l'historique iBanking
    //         for (BalanceHistoryPoint point : iBankingHistory) {
    //             LocalDateTime date = point.getRawDate();
    //             BalanceHistoryPoint existingPoint = mergedHistory.get(date);
    //             if (existingPoint != null) {
    //                 // Mettre à jour le point existant avec les données iBanking
    //                 existingPoint.setiBankingBalance(point.getiBankingBalance());
    //                 existingPoint.setGlobalBalance(existingPoint.getiPayBalance() + point.getiBankingBalance());
    //             } else {
    //                 // Créer un nouveau point avec la période
    //                 point.setPeriod(period);
    //                 mergedHistory.put(date, point);
    //             }
    //         }
            
    //         BalanceHistoryResponse response = new BalanceHistoryResponse(
    //             "success",
    //             "Historique consolidé récupéré avec succès",
    //             new ArrayList<>(mergedHistory.values()),
    //             List.of(
    //                 new ServiceStatus("i-pay", true, "Historique récupéré"),
    //                 new ServiceStatus("i-banking", true, "Historique récupéré")
    //             )
    //         );
            
    //         // Mise en cache Redis
    //         try {
    //             ObjectMapper mapper = new ObjectMapper();
    //             String jsonValue = mapper.writeValueAsString(response);
    //             redisTemplate.opsForValue().set(cacheKey, jsonValue, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
    //         } catch (Exception e) {
    //             logger.warn("Impossible de mettre en cache Redis: {}", e.getMessage());
    //         }
            
    //         return response;
    //     })
    //     .onErrorResume(e -> {
    //         logger.error("Erreur lors de la récupération de l'historique", e);
    //         return Mono.just(new BalanceHistoryResponse(
    //             "error",
    //             "Erreur lors de la récupération de l'historique",
    //             List.of(),
    //             List.of(
    //                 new ServiceStatus("i-pay", false, "Service indisponible"),
    //                 new ServiceStatus("i-banking", false, "Service indisponible")
    //             )
    //         ));
    //     });
    // }    /**
    
    // * Récupère l'historique des soldes iPay avec agrégation par période
    //  */    
    // public Mono<BalanceHistoryResponse> getBalanceHistory(String telephone, String accountId, LocalDateTime startDate, LocalDateTime endDate, Period period, String ipayToken) {
    //     String cacheKey = CACHE_KEY_PREFIX + "history:ipay:" + telephone + ":" + startDate + ":" + endDate + ":" + period;
        
    //     // Vérification du cache Redis
    //     try {
    //         String cachedValue = redisTemplate.opsForValue().get(cacheKey);
    //         if (cachedValue != null) {
    //             logger.info("Utilisation du cache Redis pour l'historique iPay - telephone: {}, période: {}", telephone, period);
    //             ObjectMapper mapper = new ObjectMapper();
    //             return Mono.just(mapper.readValue(cachedValue, BalanceHistoryResponse.class));
    //         }
    //     } catch (Exception e) {
    //         logger.warn("Redis indisponible: {}", e.getMessage());
    //     }
        
    //     // Récupération des données selon la période
    //     Duration interval = getIntervalForPeriod(period);
    //     return ipayService.getBalanceHistory(ipayToken, accountId, startDate, endDate, period)
    //         .map(history -> {
    //             if (history.isEmpty()) {
    //                 return new BalanceHistoryResponse(
    //                     "success", 
    //                     "Aucun historique trouvé",
    //                     Collections.emptyList(),
    //                     List.of(new ServiceStatus("i-pay", true, "Historique vide"))
    //                 );
    //             }                // Aggrégation des données selon l'intervalle
    //             List<BalanceHistoryPoint> aggregatedData = aggregateBalanceHistoryPoints(history, interval);
                
    //             return new BalanceHistoryResponse(
    //                 "success",
    //                 "Historique des soldes récupéré avec succès",
    //                 aggregatedData,
    //                 List.of(new ServiceStatus("i-pay", true, "Historique récupéré"))
    //             );
    //         });
    // }

    // private List<BalanceHistoryPoint> aggregateBalanceHistoryPoints(List<BalanceHistoryPoint> rawData, Duration interval) {
    //     if (rawData == null || rawData.isEmpty()) {
    //         return Collections.emptyList();
    //     }
        
    //     // Tri des données par date
    //     rawData.sort(Comparator.comparing(BalanceHistoryPoint::getRawDate));
    //       List<BalanceHistoryPoint> aggregatedData = new ArrayList<>();
    //     final LocalDateTime startDate = rawData.get(0).getRawDate();
    //     final LocalDateTime endDate = rawData.get(rawData.size() - 1).getRawDate();
    //     final Period period = rawData.get(0).getPeriod(); // Keep the same period for aggregated points
    //       // Calculate number of intervals needed
    //     long intervalCount = interval.toDays();
    //     if (intervalCount > 0) {
    //         for (LocalDateTime date = startDate; !date.isAfter(endDate); date = date.plus(interval)) {
    //             final LocalDateTime intervalStart = date;
    //             final LocalDateTime intervalEnd = date.plus(interval);
                
    //             // Filtre et agrège les données pour l'intervalle courant
    //             List<BalanceHistoryPoint> intervalPoints = rawData.stream()
    //                 .filter(bh -> !bh.getRawDate().isBefore(intervalStart) && bh.getRawDate().isBefore(intervalEnd))
    //                 .toList();
                
    //             if (!intervalPoints.isEmpty()) {
    //                 double averageGlobalBalance = intervalPoints.stream()
    //                     .mapToDouble(BalanceHistoryPoint::getGlobalBalance)
    //                     .average()
    //                     .orElse(0.0);
                        
    //                 double averageIPayBalance = intervalPoints.stream()
    //                     .mapToDouble(BalanceHistoryPoint::getiPayBalance)
    //                     .average()
    //                     .orElse(0.0);
                        
    //                 double averageIBankingBalance = intervalPoints.stream()
    //                     .mapToDouble(BalanceHistoryPoint::getiBankingBalance)
    //                     .average()
    //                     .orElse(0.0);
                    
    //                 if (averageGlobalBalance > 0 || averageIPayBalance > 0 || averageIBankingBalance > 0) {
    //                     aggregatedData.add(new BalanceHistoryPoint(
    //                         intervalStart, 
    //                         averageGlobalBalance,
    //                         averageIPayBalance,
    //                         averageIBankingBalance,
    //                         period
    //                     ));
    //                 }
    //             }
    //         }
    //     }
    //       return aggregatedData;
    // }

    /**
     * Récupère l'historique consolidé avec agrégation par SOMME selon la période
     */
    public Mono<BalanceHistoryResponse> getBalanceHistory(String telephone, String email, String ipayToken, String accountId, 
            LocalDateTime startDate, LocalDateTime endDate, Period period) {
        
        // Récupération parallèle des historiques
        return Mono.zip(
            ipayService.getBalanceHistory(ipayToken, accountId, startDate, endDate, period),
            iBankingService.getBalanceHistory(email, startDate, endDate, period)
        ).map(tuple -> {
            List<BalanceHistoryPoint> ipayHistory = tuple.getT1();
            List<BalanceHistoryPoint> iBankingHistory = tuple.getT2();
            
            // Agrégation selon la période demandée
            List<BalanceHistoryPoint> aggregatedHistory = aggregateByPeriod(ipayHistory, iBankingHistory, period, startDate);
            
            return new BalanceHistoryResponse(
                "success",
                "Historique consolidé récupéré avec succès",
                aggregatedHistory,
                List.of(
                    new ServiceStatus("i-pay", true, "Historique récupéré"),
                    new ServiceStatus("i-banking", true, "Historique récupéré")
                )
            );
        });
    }

    /**
     * Agrège les données par période en calculant les SOMMES
     */
    private List<BalanceHistoryPoint> aggregateByPeriod(List<BalanceHistoryPoint> ipayHistory, 
                                                       List<BalanceHistoryPoint> iBankingHistory, 
                                                       Period period, 
                                                       LocalDateTime referenceDate) {
        
        // Fusion des données par date
        Map<LocalDateTime, BalanceHistoryPoint> dailyData = mergeHistories(ipayHistory, iBankingHistory, period);
        
        // Agrégation selon la période
        switch (period) {
            case WEEK:
                return aggregateByWeek(dailyData, referenceDate);
            case MONTH:
                return aggregateByMonth(dailyData, referenceDate);
            case YEAR:
                return aggregateByYear(dailyData, referenceDate);
            default:
                return new ArrayList<>(dailyData.values());
        }
    }

    /**
     * Fusionne les historiques iPay et iBanking par date
     */
    private Map<LocalDateTime, BalanceHistoryPoint> mergeHistories(List<BalanceHistoryPoint> ipayHistory, 
                                                                  List<BalanceHistoryPoint> iBankingHistory, 
                                                                  Period period) {
        Map<LocalDateTime, BalanceHistoryPoint> mergedData = new TreeMap<>();
        
        // Traitement de l'historique iPay
        for (BalanceHistoryPoint point : ipayHistory) {
            mergedData.put(point.getRawDate(), new BalanceHistoryPoint(
                point.getRawDate(),
                point.getiPayBalance(), // globalBalance = iPay pour l'instant
                point.getiPayBalance(),
                0.0,
                period
            ));
        }
        
        // Fusion avec l'historique iBanking
        for (BalanceHistoryPoint point : iBankingHistory) {
            LocalDateTime date = point.getRawDate();
            BalanceHistoryPoint existingPoint = mergedData.get(date);
            
            if (existingPoint != null) {
                // Mettre à jour avec iBanking
                existingPoint.setiBankingBalance(point.getiBankingBalance());
                existingPoint.setGlobalBalance(existingPoint.getiPayBalance() + point.getiBankingBalance());
            } else {
                // Créer nouveau point avec seulement iBanking
                mergedData.put(date, new BalanceHistoryPoint(
                    date,
                    point.getiBankingBalance(), // globalBalance = iBanking
                    0.0,
                    point.getiBankingBalance(),
                    period
                ));
            }
        }
        
        return mergedData;
    }

    /**
     * Agrégation par semaine : SOMME de tous les jours de la semaine COURANTE du mois
     */
    private List<BalanceHistoryPoint> aggregateByWeek(Map<LocalDateTime, BalanceHistoryPoint> dailyData, 
                                                     LocalDateTime referenceDate) {
        List<BalanceHistoryPoint> weeklyData = new ArrayList<>();
        
        // Obtenir les dates de la semaine courante du mois (ex: jeudi 22 → lundi 26)
        List<LocalDateTime> weekDates = Period.WEEK.calculateDateRange(referenceDate);
        
        if (!weekDates.isEmpty()) {
            LocalDateTime weekStart = weekDates.get(0);
            
            // Calculer les SOMMES pour tous les jours de cette semaine
            double totalGlobal = 0.0;
            double totalIPay = 0.0;
            double totalIBanking = 0.0;
            
            for (LocalDateTime date : weekDates) {
                BalanceHistoryPoint point = dailyData.get(date);
                if (point != null) {
                    totalGlobal += point.getGlobalBalance();
                    totalIPay += point.getiPayBalance();
                    totalIBanking += point.getiBankingBalance();
                }
            }
            
            // Créer le point agrégé pour la semaine courante
            weeklyData.add(new BalanceHistoryPoint(
                weekStart,
                totalGlobal,
                totalIPay,
                totalIBanking,
                Period.WEEK
            ));
        }
        
        return weeklyData;
    }

    /**
     * Agrégation par mois : SOMME des semaines du mois (semaines définies à partir du 1er)
     */
    private List<BalanceHistoryPoint> aggregateByMonth(Map<LocalDateTime, BalanceHistoryPoint> dailyData, 
                                                      LocalDateTime referenceDate) {
        List<BalanceHistoryPoint> monthlyData = new ArrayList<>();
        
        // Obtenir toutes les semaines du mois (calculées à partir du 1er du mois)
        List<LocalDateTime> monthWeeks = Period.MONTH.calculateDateRange(referenceDate);
        
        for (LocalDateTime weekStart : monthWeeks) {
            // Pour chaque semaine, calculer les jours qui la composent
            LocalDate weekStartDate = weekStart.toLocalDate();
            LocalDate weekEndDate = weekStartDate.plusDays(6);
            
            // Ne pas dépasser la fin du mois
            LocalDate lastDayOfMonth = weekStartDate.with(TemporalAdjusters.lastDayOfMonth());
            if (weekEndDate.isAfter(lastDayOfMonth)) {
                weekEndDate = lastDayOfMonth;
            }
            
            // Ne pas dépasser aujourd'hui
            LocalDate today = referenceDate.toLocalDate();
            if (weekEndDate.isAfter(today)) {
                weekEndDate = today;
            }
            
            double totalGlobal = 0.0;
            double totalIPay = 0.0;
            double totalIBanking = 0.0;
            
            // Somme des jours de cette semaine
            LocalDate currentDay = weekStartDate;
            while (!currentDay.isAfter(weekEndDate)) {
                BalanceHistoryPoint point = dailyData.get(currentDay.atStartOfDay());
                if (point != null) {
                    totalGlobal += point.getGlobalBalance();
                    totalIPay += point.getiPayBalance();
                    totalIBanking += point.getiBankingBalance();
                }
                currentDay = currentDay.plusDays(1);
            }
            
            // Ajouter le point de la semaine si il y a des données
            if (totalGlobal > 0 || totalIPay > 0 || totalIBanking > 0) {
                monthlyData.add(new BalanceHistoryPoint(
                    weekStart,
                    totalGlobal,
                    totalIPay,
                    totalIBanking,
                    Period.MONTH
                ));
            }
        }
        
        return monthlyData;
    }

    /**
     * Agrégation par année : SOMME des mois de l'année
     */
    private List<BalanceHistoryPoint> aggregateByYear(Map<LocalDateTime, BalanceHistoryPoint> dailyData, 
                                                     LocalDateTime referenceDate) {
        List<BalanceHistoryPoint> yearlyData = new ArrayList<>();
        
        LocalDate today = referenceDate.toLocalDate();
        LocalDate firstDayOfYear = today.withDayOfYear(1);
        
        // Parcourir chaque mois de l'année
        LocalDate currentMonth = firstDayOfYear;
        
        while (!currentMonth.isAfter(today)) {
            LocalDate lastDayOfMonth = currentMonth.with(TemporalAdjusters.lastDayOfMonth());
            
            double totalGlobal = 0.0;
            double totalIPay = 0.0;
            double totalIBanking = 0.0;
            
            // Somme de tous les jours du mois
            LocalDate currentDay = currentMonth;
            while (!currentDay.isAfter(lastDayOfMonth) && !currentDay.isAfter(today)) {
                BalanceHistoryPoint point = dailyData.get(currentDay.atStartOfDay());
                if (point != null) {
                    totalGlobal += point.getGlobalBalance();
                    totalIPay += point.getiPayBalance();
                    totalIBanking += point.getiBankingBalance();
                }
                currentDay = currentDay.plusDays(1);
            }
            
            // Ajouter le point du mois si il y a des données
            if (totalGlobal > 0 || totalIPay > 0 || totalIBanking > 0) {
                yearlyData.add(new BalanceHistoryPoint(
                    currentMonth.atStartOfDay(),
                    totalGlobal,
                    totalIPay,
                    totalIBanking,
                    Period.YEAR
                ));
            }
            
            currentMonth = currentMonth.plusMonths(1);
        }
        
        return yearlyData;
    }
        
}
