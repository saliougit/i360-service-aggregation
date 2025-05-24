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
    private static final Logger logger = LoggerFactory.getLogger(TokenNotificationService.class);    public TokenNotificationService(WebClient.Builder webClientBuilder, @Value("${gateway.url:http://localhost:8080}") String gatewayUrl) {
        this.webClient = webClientBuilder
            .baseUrl(gatewayUrl)
            .build();
    }

    public Mono<Void> notifyNewToken(String username, String token) {
        logger.debug("Notification de nouveau token pour l'utilisateur: {}", username);
        
        return webClient.post()
            .uri("/internal/token-refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new TokenRefreshRequest(username, token))
            .retrieve()
            .bodyToMono(Void.class)
            .doOnSuccess(v -> logger.info("Notification réussie pour l'utilisateur: {}", username))
            .doOnError(error -> logger.error("Échec de notification Gateway pour {}: {}", username, error.getMessage()))
            .onErrorResume(e -> Mono.empty()); // Continue même en cas d'erreur
    }
}
