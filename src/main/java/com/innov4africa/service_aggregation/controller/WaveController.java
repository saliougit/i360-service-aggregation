package com.innov4africa.service_aggregation.controller;

import com.innov4africa.service_aggregation.model.ServiceStatus;
import com.innov4africa.service_aggregation.model.WaveOperationRequest;
import com.innov4africa.service_aggregation.model.WaveOperationResponse;
import com.innov4africa.service_aggregation.service.JwtUtil;
import com.innov4africa.service_aggregation.service.WaveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wave")
@Tag(name = "Wave", description = "API de transfert d'argent Wave")
@SecurityRequirement(name = "bearer-jwt")
public class WaveController {

    private static final Logger logger = LoggerFactory.getLogger(WaveController.class);
    
    @Autowired
    private WaveService waveService;
    
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Endpoint pour effectuer un prélèvement Wave
     * 
     * @param authHeader Header d'autorisation contenant le JWT
     * @param request Requête de prélèvement avec informations nécessaires
     * @return Réponse avec URL du QR code à scanner
     */
    @Operation(
        summary = "Effectuer un prélèvement Wave", 
        description = "Permet d'initier un prélèvement Wave en générant un QR code que l'utilisateur doit scanner avec l'application Wave"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Prélèvement initié avec succès", 
            content = @Content(schema = @Schema(implementation = WaveOperationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Requête invalide ou erreur de l'API Wave"),
        @ApiResponse(responseCode = "401", description = "Non autorisé - JWT invalide"),
        @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    @PostMapping("/prelevement")
    public Mono<ResponseEntity<WaveOperationResponse>> prelevement(
            @RequestHeader(value = "Authorization", required = false) 
            @Parameter(description = "JWT Bearer token (format: Bearer token)") String authHeader,
            @RequestBody 
            @Parameter(description = "Informations nécessaires pour le prélèvement Wave", 
                      required = true) WaveOperationRequest request) {
        
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
        
        logger.info("Demande de prélèvement Wave: {} -> {}, montant: {}", 
                  request.getEnvoyeurPayeur(), request.getDestinatairePaye(), request.getMontant());
        
        // Appel du service Wave pour le prélèvement
        return waveService.prelevement(
                request.getDestinatairePaye(),
                request.getMontant(),
                request.getEnvoyeurPayeur(),
                request.getCommission()
            )
            .flatMap(response -> {
                // Traitement de la réponse
                return handleWaveResponse(response, "prélèvement");
            })
            .onErrorResume(e -> {
                logger.error("Erreur technique lors du prélèvement Wave", e);
                return Mono.just(ResponseEntity.internalServerError().body(
                    new WaveOperationResponse(
                        "error",
                        "Service indisponible: " + e.getMessage(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        List.of(new ServiceStatus("wave", false, "Erreur technique"))
                    )
                ));
            });
    }

    /**
     * Endpoint pour effectuer un virement Wave
     * 
     * @param authHeader Header d'autorisation contenant le JWT
     * @param request Requête de virement avec informations nécessaires
     * @return Réponse avec URL du QR code à scanner
     */
    @Operation(
        summary = "Effectuer un virement Wave", 
        description = "Permet d'initier un virement Wave en générant un QR code que l'utilisateur doit scanner avec l'application Wave"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Virement initié avec succès", 
            content = @Content(schema = @Schema(implementation = WaveOperationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Requête invalide ou erreur de l'API Wave"),
        @ApiResponse(responseCode = "401", description = "Non autorisé - JWT invalide"),
        @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    @PostMapping("/virement")
    public Mono<ResponseEntity<WaveOperationResponse>> virement(
            @RequestHeader(value = "Authorization", required = false) 
            @Parameter(description = "JWT Bearer token (format: Bearer token)") String authHeader,
            @RequestBody 
            @Parameter(description = "Informations nécessaires pour le virement Wave", 
                      required = true) WaveOperationRequest request) {
        
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
        
        logger.info("Demande de virement Wave: {} -> {}, montant: {}", 
                  request.getEnvoyeurPayeur(), request.getDestinatairePaye(), request.getMontant());
        
        // Appel du service Wave pour le virement
        return waveService.virement(
                request.getDestinatairePaye(),
                request.getMontant(),
                request.getEnvoyeurPayeur(),
                request.getCommission()
            )
            .flatMap(response -> {
                // Traitement de la réponse
                return handleWaveResponse(response, "virement");
            })
            .onErrorResume(e -> {
                logger.error("Erreur technique lors du virement Wave", e);
                return Mono.just(ResponseEntity.internalServerError().body(
                    new WaveOperationResponse(
                        "error",
                        "Service indisponible: " + e.getMessage(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        List.of(new ServiceStatus("wave", false, "Erreur technique"))
                    )
                ));
            });
    }

    /**
     * Endpoint pour calculer les commissions d'une opération Wave
     * 
     * @param authHeader Header d'autorisation contenant le JWT
     * @param montant Montant de l'opération
     * @param service Type de service (PRELEVEMENT ou VIREMENT)
     * @return Réponse contenant les informations de commission
     */
    @Operation(
        summary = "Calculer les frais de commission Wave", 
        description = "Calcule les frais de commission pour une opération Wave selon le montant et le type d'opération"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Commission calculée avec succès"),
        @ApiResponse(responseCode = "400", description = "Paramètres invalides"),
        @ApiResponse(responseCode = "401", description = "Non autorisé - JWT invalide"),
        @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    @GetMapping("/commission")
    public Mono<ResponseEntity<Map<String, Object>>> getCommission(
            @RequestHeader(value = "Authorization", required = false) 
            @Parameter(description = "JWT Bearer token (format: Bearer token)") String authHeader,
            @RequestParam 
            @Parameter(description = "Montant de l'opération en FCFA", required = true) String montant,
            @RequestParam 
            @Parameter(description = "Type de service (PRELEVEMENT ou VIREMENT)", required = true) String service) {
        
        // Vérification du header d'autorisation
        if (authHeader == null || authHeader.isBlank()) {
            return Mono.just(ResponseEntity.status(401).body(Map.of(
                "status", "error",
                "message", "Token manquant",
                "services", List.of(Map.of(
                    "serviceName", "wave",
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
                    "serviceName", "wave",
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
                    "serviceName", "wave",
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
                    "serviceName", "wave",
                    "available", false,
                    "message", "Paramètre invalide"
                ))
            )));
        }
        
        logger.info("Calcul des commissions pour {} de montant {}", service, montant);
        
        // Type de transaction, toujours NORMAL pour l'instant
        String type = "NORMAL";
        
        // Appel du service Wave pour le calcul des commissions
        return waveService.getCommission(montant, service, type)
            .map(response -> ResponseEntity.ok(response))
            .onErrorResume(e -> {
                logger.error("Erreur lors du calcul des commissions", e);
                return Mono.just(ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Service indisponible: " + e.getMessage(),
                    "services", List.of(Map.of(
                        "serviceName", "wave",
                        "available", false,
                        "message", "Erreur technique"
                    ))
                )));
            });
    }

    /**
     * Construit une réponse d'erreur 401 Unauthorized
     */
    private Mono<ResponseEntity<WaveOperationResponse>> buildUnauthorizedResponse(String message) {
        return Mono.just(ResponseEntity.status(401).body(
            new WaveOperationResponse(
                "error",
                message,
                List.of(new ServiceStatus("wave", false, "Non autorisé"))
            )
        ));
    }

    /**
     * Construit une réponse d'erreur 400 Bad Request
     */
    private Mono<ResponseEntity<WaveOperationResponse>> buildBadRequestResponse(String message) {
        return Mono.just(ResponseEntity.badRequest().body(
            new WaveOperationResponse(
                "error",
                message,
                List.of(new ServiceStatus("wave", false, message))
            )
        ));
    }

    /**
     * Méthode commune pour gérer les réponses des opérations Wave (prélèvement/virement)
     * 
     * @param response Réponse de l'API Wave
     * @param operationType Type d'opération (prélèvement/virement)
     * @return ResponseEntity formaté pour le client
     */
    private Mono<ResponseEntity<WaveOperationResponse>> handleWaveResponse(Map<String, Object> response, String operationType) {
        // Extraction des informations de la réponse
        String status = response.containsKey("status") ? (String) response.get("status") : null;
        String message = response.containsKey("message") ? (String) response.get("message") : null;
        String code = response.containsKey("code") ? String.valueOf(response.get("code")) : null;
        String statut = response.containsKey("statut") ? (String) response.get("statut") : null;
        
        // Log de debug pour voir la structure complète de la réponse
        logger.debug("Réponse Wave reçue: {}", response);
        
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
            userMessage = "Le numéro Wave spécifié n'existe pas";
        } else if (isSuccess) {
            userMessage = operationType.equals("virement") ? 
                    "Virement Wave initié avec succès" : 
                    "Prélèvement Wave initié avec succès";
        } else {
            userMessage = "Erreur lors du " + operationType + " Wave";
            if (code != null) {
                userMessage += " (Code: " + code + ")";
            }
            if (statut != null) {
                userMessage += " - " + statut;
            }
        }
        
        if (isSuccess) {
            // Récupération du QR code image (encodé en base64)
            String qrCodeImage = response.get("qrCode") != null ? (String) response.get("qrCode") : null;
            
            // Récupération de l'URL du QR code ou du service de paiement
            String qrCodeUrl = response.get("qrCodeUrl") != null ? (String) response.get("qrCodeUrl") : null;
            
            // Récupération de l'URL Wave Pay (si disponible)
            String wavePayUrl = (String) response.get("url");
            
            String requestId = response.get("requestId") != null ? response.get("requestId").toString() : null;
            String transactionId = response.get("transactionId") != null ? 
                                   response.get("transactionId").toString() : 
                                   response.get("id_transaction") != null ? 
                                   response.get("id_transaction").toString() : null;
            
            return Mono.just(ResponseEntity.ok(
                new WaveOperationResponse(
                    "success",
                    userMessage,
                    qrCodeUrl,
                    wavePayUrl,
                    qrCodeImage,
                    transactionId,
                    requestId,
                    List.of(new ServiceStatus("wave", true, userMessage))
                )
            ));
        } else {
            return Mono.just(ResponseEntity.badRequest().body(
                new WaveOperationResponse(
                    "error",
                    userMessage,
                    null,
                    null,
                    null,
                    null,
                    null,
                    List.of(new ServiceStatus("wave", false, userMessage))
                )
            ));
        }
    }
}
