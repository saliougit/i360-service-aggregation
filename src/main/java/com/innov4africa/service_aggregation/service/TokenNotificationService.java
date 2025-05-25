package com.innov4africa.service_aggregation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.innov4africa.service_aggregation.model.TokenRefreshRequest;

import reactor.core.publisher.Mono;

@Service
public class TokenNotificationService {
    private final WebClient webClient;
    private static final Logger logger = LoggerFactory.getLogger(TokenNotificationService.class);

    public TokenNotificationService(WebClient.Builder webClientBuilder, @Value("${gateway.url}") String gatewayUrl) {
        this.webClient = webClientBuilder
            .baseUrl(gatewayUrl)
            .build();
        logger.info("TokenNotificationService initialized with Gateway URL: {}", gatewayUrl);
    }

    /**
     * Notifie le Gateway d'un nouveau token.
     * @param username L'email de l'utilisateur
     * @param token Le nouveau token JWT
     * @return Mono<Void> complété quand la notification est envoyée
     */
    public Mono<Void> notifyNewToken(String username, String token) {
        logger.info("Début de notifyNewToken pour l'utilisateur: {}", username);
        logger.debug("Préparation de la requête de notification avec token: {}", token);

        return webClient.post()
            .uri("/internal/token-notification")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new TokenRefreshRequest(username, token))
            .retrieve()
            .bodyToMono(Void.class)
            .doOnSubscribe(s -> logger.debug("Envoi de la notification au Gateway..."))
            .doOnSuccess(v -> {
                logger.info("Notification réussie pour l'utilisateur: {}", username);
                logger.debug("Token mis à jour avec succès dans le Gateway");
            })
            .doOnError(error -> {
                logger.error("Échec de notification Gateway pour {}: {}", username, error.getMessage());
                logger.error("Détails de l'erreur:", error);
            })
            .onErrorResume(e -> {
                logger.error("Erreur lors de la notification du Gateway, réessayer plus tard: {}", e.getMessage(), e);
                return Mono.empty();
            });
    }
}
