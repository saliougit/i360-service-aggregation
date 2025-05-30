package com.innov4africa.service_aggregation.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.innov4africa.service_aggregation.model.IBankingBalanceResponse;
import com.innov4africa.service_aggregation.model.IBankingUserCheckResponse;
import com.innov4africa.service_aggregation.model.ServiceStatus;
import com.innov4africa.service_aggregation.service.IBankingService;
import com.innov4africa.service_aggregation.service.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/ibanking")
@Tag(name = "IBanking", description = "API pour la gestion des comptes iBanking et la synchronisation avec iPay")
public class IBankingController {
    
    private static final Logger logger = LoggerFactory.getLogger(IBankingController.class);
    
    @Autowired
    private IBankingService iBankingService;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Operation(
        summary = "Vérifier l'existence d'un utilisateur",
        description = "Vérifie si un utilisateur existe dans iBanking en utilisant son email ou son numéro de téléphone. " +
                     "Au moins l'un des deux paramètres doit être fourni."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Vérification effectuée avec succès",
            content = @Content(schema = @Schema(implementation = IBankingUserCheckResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Paramètres invalides - email ou téléphone requis",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erreur serveur lors de la vérification",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))
        )
    })
    @GetMapping("/user-check")
    public Mono<ResponseEntity<IBankingUserCheckResponse>> checkUser(
            @Parameter(description = "Email de l'utilisateur à vérifier")
            @RequestParam(required = false) String email,
            
            @Parameter(description = "Numéro de téléphone de l'utilisateur à vérifier")
            @RequestParam(required = false) String telephone) {
        
        logger.info("Vérification utilisateur - email: {}, telephone: {}", email, telephone);

        if ((email == null || email.isBlank()) && (telephone == null || telephone.isBlank())) {
            return Mono.just(ResponseEntity.badRequest().body(
                new IBankingUserCheckResponse("error", "Email ou téléphone requis", false)
            ));
        }

        return iBankingService.verifyUserExists(email, telephone)
            .map(exists -> {
                String message = exists ? 
                    "Utilisateur trouvé dans iBanking" : 
                    "Utilisateur non trouvé dans iBanking";
                
                return ResponseEntity.ok(new IBankingUserCheckResponse(
                    exists ? "success" : "not_found",
                    message,
                    exists
                ));
            })
            .onErrorResume(e -> {
                logger.error("Erreur lors de la vérification de l'utilisateur", e);
                return Mono.just(ResponseEntity.internalServerError().body(
                    new IBankingUserCheckResponse("error", "Erreur technique", false)
                ));
            });
    }

    @Operation(
        summary = "Créer un utilisateur iBanking",
        description = "Crée un nouveau compte utilisateur dans iBanking avec les informations fournies. " +
                     "Le mot de passe peut être synchronisé avec celui d'iPay pour une meilleure expérience utilisateur."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Utilisateur créé avec succès",
            content = @Content(schema = @Schema(implementation = IBankingUserCheckResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Données invalides ou manquantes",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Un utilisateur avec cet email ou ce téléphone existe déjà",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erreur serveur lors de la création",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))
        )
    })
    @PostMapping("/user")
    public Mono<ResponseEntity<IBankingUserCheckResponse>> createUser(
            @Parameter(description = "Email de l'utilisateur", required = true)
            @RequestParam String email,
            
            @Parameter(description = "Numéro de téléphone", required = true)
            @RequestParam String telephone,
            
            @Parameter(description = "Prénom de l'utilisateur", required = true)
            @RequestParam(name = "firstname") String firstName,
            
            @Parameter(description = "Nom de l'utilisateur", required = false)
            @RequestParam(name = "lastname", required = false) String lastName,
            
            @Parameter(
                description = "Mot de passe (utiliser le même que iPay pour la synchronisation)",
                required = true
            )
            @RequestParam String password) {
        
        logger.info("Création d'un utilisateur - email: {}, telephone: {}, prénom: {}, nom: {}", 
                   email, telephone, firstName, lastName != null ? lastName : "");

        String finalLastName = lastName != null ? lastName : "";
        
        return iBankingService.createUser(email, telephone, firstName, finalLastName, password)
            .map(created -> {
                String message = created ? 
                    "Utilisateur créé avec succès" : 
                    "Erreur lors de la création de l'utilisateur";
                
                return ResponseEntity.ok(new IBankingUserCheckResponse(
                    created ? "success" : "error",
                    message,
                    created
                ));
            })
            .onErrorResume(e -> {
                logger.error("Erreur lors de la création de l'utilisateur", e);
                return Mono.just(ResponseEntity.internalServerError().body(
                    new IBankingUserCheckResponse("error", "Erreur technique", false)
                ));
            });
    }

    @Operation(
        summary = "Récupérer le solde iBanking",
        description = "Récupère le solde du compte iBanking de l'utilisateur authentifié"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Solde récupéré avec succès",
            content = @Content(schema = @Schema(implementation = IBankingBalanceResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Non authentifié",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erreur serveur lors de la récupération du solde",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))
        )
    })
    @GetMapping("/balance")
    public Mono<ResponseEntity<IBankingBalanceResponse>> getBalance(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        // Vérification du header Authorization
        if (authHeader == null || authHeader.isBlank()) {
            logger.warn("Tentative d'accès au solde sans header Authorization");
            return Mono.just(ResponseEntity.status(401).body(
                new IBankingBalanceResponse("error", "Token d'authentification manquant", "0.00",
                    List.of(new ServiceStatus("i-banking", false, "Non autorisé")))
            ));
        }

        // Vérification du format Bearer
        if (!authHeader.startsWith("Bearer ")) {
            logger.warn("Format de token invalide pour le solde: {}", authHeader);
            return Mono.just(ResponseEntity.status(401).body(
                new IBankingBalanceResponse("error", "Format de token invalide", "0.00",
                    List.of(new ServiceStatus("i-banking", false, "Non autorisé")))
            ));
        }

        String jwt = authHeader.substring(7);
        
        // Validation du token JWT
        if (!jwtUtil.validateToken(jwt)) {
            logger.warn("Token JWT invalide ou expiré pour le solde");
            return Mono.just(ResponseEntity.status(401).body(
                new IBankingBalanceResponse("error", "Token invalide ou expiré", "0.00",
                    List.of(new ServiceStatus("i-banking", false, "Non autorisé")))
            ));
        }

        // Extraction de l'email
        String email = jwtUtil.extractUsername(jwt);
        if (email == null) {
            return Mono.just(ResponseEntity.status(401).body(
                new IBankingBalanceResponse("error", "Email non trouvé dans le token", "0.00",
                    List.of(new ServiceStatus("i-banking", false, "Non autorisé")))
            ));
        }

        logger.info("Récupération du solde iBanking pour l'email: {}", email);
        
        return iBankingService.getSolde(email)
            .map(ResponseEntity::ok)
            .onErrorResume(e -> {
                logger.error("Erreur lors de la récupération du solde iBanking", e);
                return Mono.just(ResponseEntity.internalServerError().body(
                    new IBankingBalanceResponse("error", "Erreur technique", "0.00",
                        List.of(new ServiceStatus("i-banking", false, "Service indisponible")))
                ));
            });
    }
}
