package com.innov4africa.service_aggregation.service;

import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
import com.innov4africa.service_aggregation.model.BalanceHistoryPoint;
import com.innov4africa.service_aggregation.model.GlobalBalanceResponse;
import com.innov4africa.service_aggregation.model.IBankingBalanceResponse;
import com.innov4africa.service_aggregation.model.Period;
import com.innov4africa.service_aggregation.model.ServiceStatus;
import com.innov4africa.service_aggregation.utils.DateUtils;

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
     * Récupère l'historique des soldes pour une période donnée
     */
    public Mono<List<BalanceHistoryPoint>> getBalanceHistory(
            String telephone, String email, String ipayToken, String accountId,
            LocalDateTime startDate, LocalDateTime endDate, Period period) {
        
        logger.info("Récupération de l'historique - période: {}, du {} au {}", period, startDate, endDate);
        
        return Mono.zip(
            ipayService.getBalanceHistory(ipayToken, accountId, startDate, endDate, period),
            iBankingService.getBalanceHistory(email, startDate, endDate, period)
        ).map(tuple -> {
            List<BalanceHistoryPoint> ipayHistory = tuple.getT1();
            List<BalanceHistoryPoint> iBankingHistory = tuple.getT2();
            List<BalanceHistoryPoint> result = new ArrayList<>();

            switch (period) {
                case WEEK -> {
                    List<LocalDate> daysInWeek = DateUtils.getDaysInPeriod(startDate, endDate);
                    for (LocalDate day : daysInWeek) {
                        LocalDateTime dayStart = day.atStartOfDay();
                        double ipayAmount = getAmountForDate(ipayHistory, dayStart);
                        double iBankingAmount = getAmountForDate(iBankingHistory, dayStart);
                        
                        result.add(new BalanceHistoryPoint(
                            dayStart,
                            ipayAmount + iBankingAmount,
                            ipayAmount,
                            iBankingAmount,
                            period
                        ));
                    }
                }
                case MONTH -> {
                    List<LocalDateTime[]> weekRanges = DateUtils.getWeekRangesForMonth(endDate);
                    for (LocalDateTime[] weekRange : weekRanges) {
                        LocalDateTime weekStart = weekRange[0];
                        double ipayAmount = getAmountForDate(ipayHistory, weekStart);
                        double iBankingAmount = getAmountForDate(iBankingHistory, weekStart);
                        
                        result.add(new BalanceHistoryPoint(
                            weekStart,
                            ipayAmount + iBankingAmount,
                            ipayAmount,
                            iBankingAmount,
                            period
                        ));
                    }
                }
                case YEAR -> {
                    List<LocalDateTime[]> monthRanges = DateUtils.getMonthRangesForYear(endDate);
                    for (LocalDateTime[] monthRange : monthRanges) {
                        LocalDateTime monthStart = monthRange[0];
                        double ipayAmount = getAmountForDate(ipayHistory, monthStart);
                        double iBankingAmount = getAmountForDate(iBankingHistory, monthStart);
                        
                        result.add(new BalanceHistoryPoint(
                            monthStart,
                            ipayAmount + iBankingAmount,
                            ipayAmount,
                            iBankingAmount,
                            period
                        ));
                    }
                }
            }
            
            return result;
        });
    }

    private double getAmountForDate(List<BalanceHistoryPoint> history, LocalDateTime date) {
        return history.stream()
            .filter(point -> point.getRawDate().toLocalDate().equals(date.toLocalDate()))
            .findFirst()
            .map(BalanceHistoryPoint::getGlobalBalance)
            .orElse(0.0);
    }

    private double getAmountForPeriod(List<BalanceHistoryPoint> history, LocalDateTime start, LocalDateTime end) {
        return history.stream()
            .filter(point -> {
                LocalDateTime date = point.getRawDate();
                return !date.isBefore(start) && !date.isAfter(end);
            })
            .mapToDouble(BalanceHistoryPoint::getGlobalBalance)
            .sum();
    }
}
