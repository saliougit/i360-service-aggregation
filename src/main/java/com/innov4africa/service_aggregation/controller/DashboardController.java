package com.innov4africa.service_aggregation.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.innov4africa.service_aggregation.model.BalanceHistoryResponse;
import com.innov4africa.service_aggregation.model.GlobalBalanceResponse;
import com.innov4africa.service_aggregation.model.ServiceStatus;
import com.innov4africa.service_aggregation.service.AggregationService;
import com.innov4africa.service_aggregation.service.JwtUtil;
import com.innov4africa.service_aggregation.model.Period;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/dashboard")
@Tag(name = "Dashboard", description = "API pour les fonctionnalités agrégées")
public class DashboardController {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    
    @Autowired
    private AggregationService aggregationService;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Operation(
        summary = "Obtenir l'historique des soldes",
        description = "Récupère l'historique consolidé des soldes iPay et iBanking sur une période donnée"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Historique récupéré avec succès"),
        @ApiResponse(responseCode = "400", description = "Paramètres invalides"),
        @ApiResponse(responseCode = "401", description = "Non autorisé ou token invalide"),
        @ApiResponse(responseCode = "500", description = "Erreur serveur lors de la récupération des historiques")
    })
    @GetMapping("/balance/history")
    public Mono<ResponseEntity<BalanceHistoryResponse>> getBalanceHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) @Parameter(description = "Date de début (format: yyyy-MM-dd'T'HH:mm:ss)") String startDate,
            @RequestParam(required = false) @Parameter(description = "Date de fin (format: yyyy-MM-dd'T'HH:mm:ss)") String endDate,
            @RequestParam(required = false, defaultValue = "WEEK") @Parameter(description = "Type de période (WEEK, MONTH, YEAR)") String periodStr) {
        
        // 1. Vérification de la présence du header Authorization
        if (authHeader == null || authHeader.isBlank()) {
            logger.warn("Tentative d'accès sans header Authorization");
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new BalanceHistoryResponse("error", "Token d'authentification manquant", null,
                    List.of(new ServiceStatus("auth", false, "Non autorisé")))));
        }
        
        // 2. Vérification du format Bearer
        if (!authHeader.startsWith("Bearer ")) {
            logger.warn("Format de token invalide: {}", authHeader);
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new BalanceHistoryResponse("error", "Format de token invalide", null,
                    List.of(new ServiceStatus("auth", false, "Format de token invalide")))));
        }
        
        String jwt = authHeader.substring(7);
        
        // 3. Validation du token JWT
        if (!jwtUtil.validateToken(jwt)) {
            logger.warn("Token JWT invalide ou expiré");
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new BalanceHistoryResponse("error", "Token invalide ou expiré", null,
                    List.of(new ServiceStatus("auth", false, "Non autorisé")))));
        }
        
        // 4. Validation de la période
        Period period;
        try {
            period = Period.valueOf(periodStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Type de période invalide: {}", periodStr);
            return Mono.just(ResponseEntity.badRequest()
                .body(new BalanceHistoryResponse("error", "Type de période invalide", null,
                    List.of(new ServiceStatus("validation", false, "Période invalide, utilisez WEEK, MONTH ou YEAR")))));
        }
        
        // 5. Extraction des claims nécessaires du token
        String telephone = jwtUtil.extractTelephone(jwt);
        String email = jwtUtil.extractUsername(jwt);
        String ipayToken = jwtUtil.extractIpayToken(jwt);
        String accountId = jwtUtil.extractAccountIdIPay(jwt);
        
        if (telephone == null || ipayToken == null || accountId == null) {
            logger.warn("Token ne contient pas les claims requis - telephone: {}, ipayToken: {}, accountId: {}", 
                telephone, ipayToken, accountId);
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new BalanceHistoryResponse("error", "Token incomplet", null,
                    List.of(new ServiceStatus("auth", false, "Token incomplet")))));
        }
        
        // 6. Calcul ou vérification des dates
        LocalDateTime start, end;  
              
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            // On utilise toujours la date actuelle comme point de référence
            end = LocalDateTime.now();
            
            // Les dates de début et de fin fournies sont ignorées car nous calculons 
            // toujours à partir de la date actuelle selon la période
            List<LocalDateTime> dateRange = period.calculateDateRange(end);
            if (!dateRange.isEmpty()) {
                start = dateRange.get(0);
            } else {
                // Si aucune date n'est retournée (cas improbable), on utilise la date actuelle
                start = end;
            }
        } catch (Exception e) {
            logger.warn("Format de date invalide - startDate: {}, endDate: {}", startDate, endDate);
            return Mono.just(ResponseEntity.badRequest()
                .body(new BalanceHistoryResponse("error", "Format de date invalide", null,
                    List.of(new ServiceStatus("validation", false, "Format de date invalide")))));
        }
        
        logger.info("Demande d'historique des soldes pour telephone: {}, période: {} de {} à {}", 
            telephone, period, start, end);
        
        return aggregationService.getBalanceHistory(telephone, email, ipayToken, accountId, start, end, period)
        .map(balancePoints -> new BalanceHistoryResponse(
            "success",
            "Historique récupéré avec succès",
            balancePoints,
            List.of(new ServiceStatus("service", true, "OK"))
        ))
        .map(ResponseEntity::ok)
        .onErrorResume(e -> {
            logger.error("Erreur lors de la récupération de l'historique des soldes", e);
            return Mono.just(ResponseEntity.internalServerError()
                .body(new BalanceHistoryResponse("error", "Erreur technique", null,
                    List.of(new ServiceStatus("service", false, "Service temporairement indisponible")))));
        });
    }

    @Operation(
        summary = "Obtenir le solde global",
        description = "Récupère et agrège les soldes des comptes iPay et iBanking de l'utilisateur"
    )
    
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Solde global récupéré avec succès"),
        @ApiResponse(responseCode = "401", description = "Non autorisé ou token invalide"),
        @ApiResponse(responseCode = "500", description = "Erreur serveur lors de la récupération des soldes")
    })
    @GetMapping("/solde")
    public Mono<ResponseEntity<GlobalBalanceResponse>> getGlobalBalance(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        // 1. Vérifier la présence du header Authorization
        if (authHeader == null || authHeader.isBlank()) {
            logger.warn("Tentative d'accès sans header Authorization");            return Mono.just(ResponseEntity.status(401).body(
                GlobalBalanceResponse.authError("Token d'authentification manquant", "Token manquant")
            ));
        }

        // 2. Vérifier le format Bearer
        if (!authHeader.startsWith("Bearer ")) {
            logger.warn("Format de token invalide: {}", authHeader);            return Mono.just(ResponseEntity.status(401).body(
                GlobalBalanceResponse.authError("Format de token invalide", "Format de token invalide")
            ));
        }

        String jwt = authHeader.substring(7);
        
        // 3. Valider le token JWT
        if (!jwtUtil.validateToken(jwt)) {
            logger.warn("Token JWT invalide ou expiré");            return Mono.just(ResponseEntity.status(401).body(
                GlobalBalanceResponse.authError("Token invalide ou expiré", "Token invalide ou expiré")
            ));
        }

        // 4. Extraire les claims nécessaires
        String telephone = jwtUtil.extractTelephone(jwt);
        String ipayToken = jwtUtil.extractIpayToken(jwt);
        String email = jwtUtil.extractUsername(jwt);



        if (telephone == null || ipayToken == null || email == null) {
            logger.warn("Token ne contient pas les claims requis - telephone: {}, email: {}, ipayToken: {}", 
                      telephone, email, ipayToken);            return Mono.just(ResponseEntity.status(401).body(
                GlobalBalanceResponse.authError("Token incomplet", "Token incomplet")
            ));
        }

        // 5. Appeler le service d'agrégation
        return aggregationService.getGlobalBalance(telephone, email, ipayToken)
            .map(ResponseEntity::ok)
            .onErrorResume(e -> {
                logger.error("Erreur lors de la récupération du solde global", e);
                return Mono.just(ResponseEntity.internalServerError().body(
                    new GlobalBalanceResponse(
                        "error",
                        "Erreur technique",
                        null,
                        null,
                        null,
                        List.of(new ServiceStatus("service", false, "Service temporairement indisponible"))
                    )
                ));
            });
    }
}
