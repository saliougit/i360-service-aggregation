package com.innov4africa.service_aggregation.controller;

import com.innov4africa.service_aggregation.model.OMOperationRequest;
import com.innov4africa.service_aggregation.model.OMOperationResponse;
import com.innov4africa.service_aggregation.model.ServiceStatus;
import com.innov4africa.service_aggregation.service.JwtUtil;
import com.innov4africa.service_aggregation.service.OrangeMoneyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/om")
public class OrangeMoneyController {

    private static final Logger logger = LoggerFactory.getLogger(OrangeMoneyController.class);
    
    @Autowired
    private OrangeMoneyService orangeMoneyService;
    
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Endpoint pour effectuer un prélèvement Orange Money
     * 
     * @param authHeader Header d'autorisation contenant le JWT
     * @param request Requête de prélèvement avec informations nécessaires
     * @return Réponse avec le résultat de l'opération
     */
    @PostMapping("/prelevement")
    public Mono<ResponseEntity<OMOperationResponse>> prelevement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody OMOperationRequest request) {
        
        // Vérification du header d'autorisation
        if (authHeader == null || authHeader.isBlank()) {
            return buildUnauthorizedResponse("Token manquant");
        }

        if (!authHeader.startsWith("Bearer ")) {
            return buildUnauthorizedResponse("Format token invalide");
        }

        String jwt = authHeader.substring(7);
        if (!jwtUtil.validateToken(jwt)) {
            return buildUnauthorizedResponse("Token invalide/expiré");
        }
        
        // Extraction du téléphone depuis le JWT si non fourni dans la requête
        String telephone = jwtUtil.extractTelephone(jwt);
        if (request.getEnvoyeurPayeur() == null || request.getEnvoyeurPayeur().isBlank()) {
            if (telephone != null) {
                request.setEnvoyeurPayeur(telephone);
            } else {
                return buildBadRequestResponse("Numéro de téléphone manquant");
            }
        }
        
        // Utilisation de 0 comme valeur par défaut pour la commission si non fournie
        if (request.getCommission() == null || request.getCommission().isBlank()) {
            request.setCommission("0");
        }
        
        logger.info("Demande de prélèvement Orange Money: {} -> {}, montant: {}", 
                  request.getEnvoyeurPayeur(), request.getDestinatairePaye(), request.getMontant());
        
        // Appel du service Orange Money pour le prélèvement
        return orangeMoneyService.prelevement(
                request.getDestinatairePaye(),
                request.getMontant(),
                request.getEnvoyeurPayeur(),
                request.getCommission()
            )
            .flatMap(response -> {
                // Traitement de la réponse
                return handleOMResponse(response, "prélèvement");
            })
            .onErrorResume(e -> {
                logger.error("Erreur technique lors du prélèvement Orange Money", e);
                return Mono.just(ResponseEntity.internalServerError().body(
                    new OMOperationResponse(
                        "error",
                        "Service indisponible: " + e.getMessage(),
                        null,
                        null,
                        List.of(new ServiceStatus("om", false, "Erreur technique"))
                    )
                ));
            });
    }

    /**
     * Endpoint pour effectuer un virement Orange Money
     * 
     * @param authHeader Header d'autorisation contenant le JWT
     * @param request Requête de virement avec informations nécessaires
     * @return Réponse avec le résultat de l'opération
     */
    @PostMapping("/virement")
    public Mono<ResponseEntity<OMOperationResponse>> virement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody OMOperationRequest request) {
        
        // Vérification du header d'autorisation
        if (authHeader == null || authHeader.isBlank()) {
            return buildUnauthorizedResponse("Token manquant");
        }

        if (!authHeader.startsWith("Bearer ")) {
            return buildUnauthorizedResponse("Format token invalide");
        }

        String jwt = authHeader.substring(7);
        if (!jwtUtil.validateToken(jwt)) {
            return buildUnauthorizedResponse("Token invalide/expiré");
        }
        
        // Extraction du téléphone depuis le JWT si non fourni dans la requête
        String telephone = jwtUtil.extractTelephone(jwt);
        if (request.getEnvoyeurPayeur() == null || request.getEnvoyeurPayeur().isBlank()) {
            if (telephone != null) {
                request.setEnvoyeurPayeur(telephone);
            } else {
                return buildBadRequestResponse("Numéro de téléphone manquant");
            }
        }
        
        // Utilisation de 0 comme valeur par défaut pour la commission si non fournie
        if (request.getCommission() == null || request.getCommission().isBlank()) {
            request.setCommission("0");
        }
        
        logger.info("Demande de virement Orange Money: {} -> {}, montant: {}", 
                  request.getEnvoyeurPayeur(), request.getDestinatairePaye(), request.getMontant());
        
        // Appel du service Orange Money pour le virement
        return orangeMoneyService.virement(
                request.getDestinatairePaye(),
                request.getMontant(),
                request.getEnvoyeurPayeur(),
                request.getCommission()
            )
            .flatMap(response -> {
                // Traitement de la réponse
                return handleOMResponse(response, "virement");
            })
            .onErrorResume(e -> {
                logger.error("Erreur technique lors du virement Orange Money", e);
                return Mono.just(ResponseEntity.internalServerError().body(
                    new OMOperationResponse(
                        "error",
                        "Service indisponible: " + e.getMessage(),
                        null,
                        null,
                        List.of(new ServiceStatus("om", false, "Erreur technique"))
                    )
                ));
            });
    }

    /**
     * Méthode commune pour gérer les réponses des opérations Orange Money (prélèvement/virement)
     * 
     * @param response Réponse de l'API Orange Money
     * @param operationType Type d'opération (prélèvement/virement)
     * @return ResponseEntity formaté pour le client
     */
    private Mono<ResponseEntity<OMOperationResponse>> handleOMResponse(Map<String, Object> response, String operationType) {
        // Extraction des informations de la réponse
        String status = response.containsKey("status") ? (String) response.get("status") : null;
        String message = response.containsKey("message") ? (String) response.get("message") : null;
        String code = response.containsKey("code") ? String.valueOf(response.get("code")) : null;
        String statut = response.containsKey("statut") ? (String) response.get("statut") : null;
        
        // Log de debug pour voir la structure complète de la réponse
        logger.debug("Réponse Orange Money reçue: {}", response);
        
        // Vérification si c'est un succès (code 200 et/ou status=success)
        boolean isSuccess = ("200".equals(code) || "success".equalsIgnoreCase(status)) && 
                           !("SOLDE_INSUFFISANT".equals(statut) || "COMPTE_INEXISTANT".equals(statut));
        
        // Construction du message approprié selon le code d'erreur
        String userMessage;
        if (message != null && !message.isEmpty()) {
            userMessage = message;
        } else if ("SOLDE_INSUFFISANT".equals(statut)) {
            userMessage = "Solde insuffisant pour effectuer cette opération";
        } else if ("COMPTE_INEXISTANT".equals(statut)) {
            userMessage = "Le numéro Orange Money spécifié n'existe pas";
        } else if (isSuccess) {
            userMessage = operationType.equals("virement") ? 
                    "Virement Orange Money initié avec succès" : 
                    "Prélèvement Orange Money initié avec succès";
        } else {
            userMessage = "Erreur lors du " + operationType + " Orange Money";
            if (code != null) {
                userMessage += " (Code: " + code + ")";
            }
            if (statut != null) {
                userMessage += " - " + statut;
            }
        }
        
        if (isSuccess) {
            String transactionId = response.get("transactionId") != null ? 
                                   response.get("transactionId").toString() : 
                                   response.get("id_transaction") != null ? 
                                   response.get("id_transaction").toString() : null;
            
            String reference = response.get("reference") != null ? 
                              response.get("reference").toString() : null;
            
            return Mono.just(ResponseEntity.ok(
                new OMOperationResponse(
                    "success",
                    userMessage,
                    transactionId,
                    reference,
                    List.of(new ServiceStatus("om", true, userMessage))
                )
            ));
        } else {
            return Mono.just(ResponseEntity.badRequest().body(
                new OMOperationResponse(
                    "error",
                    userMessage,
                    null,
                    null,
                    List.of(new ServiceStatus("om", false, userMessage))
                )
            ));
        }
    }

    /**
     * Endpoint pour calculer les commissions d'une opération Orange Money
     * 
     * @param authHeader Header d'autorisation contenant le JWT
     * @param montant Montant de l'opération
     * @param service Type de service (PRELEVEMENT ou VIREMENT)
     * @return Réponse contenant les informations de commission
     */
    @GetMapping("/commission")
    public Mono<ResponseEntity<Map<String, Object>>> getCommission(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam String montant,
            @RequestParam String service) {
        
        // Vérification du header d'autorisation
        if (authHeader == null || authHeader.isBlank()) {
            return Mono.just(ResponseEntity.status(401).body(Map.of(
                "status", "error",
                "message", "Token manquant",
                "services", List.of(Map.of(
                    "serviceName", "om",
                    "available", false,
                    "message", "Non autorisé"
                ))
            )));
        }

        if (!authHeader.startsWith("Bearer ")) {
            return Mono.just(ResponseEntity.status(401).body(Map.of(
                "status", "error",
                "message", "Format token invalide",
                "services", List.of(Map.of(
                    "serviceName", "om",
                    "available", false,
                    "message", "Non autorisé"
                ))
            )));
        }

        String jwt = authHeader.substring(7);
        if (!jwtUtil.validateToken(jwt)) {
            return Mono.just(ResponseEntity.status(401).body(Map.of(
                "status", "error",
                "message", "Token invalide/expiré",
                "services", List.of(Map.of(
                    "serviceName", "om",
                    "available", false,
                    "message", "Non autorisé"
                ))
            )));
        }
        
        // Validation du type de service
        if (!"PRELEVEMENT".equalsIgnoreCase(service) && !"VIREMENT".equalsIgnoreCase(service)) {
            return Mono.just(ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Type de service invalide. Utilisez PRELEVEMENT ou VIREMENT.",
                "services", List.of(Map.of(
                    "serviceName", "om",
                    "available", false,
                    "message", "Paramètre invalide"
                ))
            )));
        }
        
        logger.info("Calcul des commissions pour {} de montant {}", service, montant);
        
        // Type de transaction, toujours NORMAL pour l'instant
        String type = "NORMAL";
        
        // Appel du service Orange Money pour le calcul des commissions
        return orangeMoneyService.getCommission(montant, service, type)
            .map(response -> ResponseEntity.ok(response))
            .onErrorResume(e -> {
                logger.error("Erreur lors du calcul des commissions", e);
                return Mono.just(ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Service indisponible: " + e.getMessage(),
                    "services", List.of(Map.of(
                        "serviceName", "om",
                        "available", false,
                        "message", "Erreur technique"
                    ))
                )));
            });
    }

    /**
     * Construit une réponse d'erreur 401 Unauthorized
     */
    private Mono<ResponseEntity<OMOperationResponse>> buildUnauthorizedResponse(String message) {
        return Mono.just(ResponseEntity.status(401).body(
            new OMOperationResponse(
                "error",
                message,
                List.of(new ServiceStatus("om", false, "Non autorisé"))
            )
        ));
    }

    /**
     * Construit une réponse d'erreur 400 Bad Request
     */
    private Mono<ResponseEntity<OMOperationResponse>> buildBadRequestResponse(String message) {
        return Mono.just(ResponseEntity.badRequest().body(
            new OMOperationResponse(
                "error",
                message,
                List.of(new ServiceStatus("om", false, message))
            )
        ));
    }
}
