package com.innov4africa.service_aggregation.config;

import com.innov4africa.service_aggregation.service.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Filtre WebFlux pour valider les tokens JWT dans les requêtes entrantes
 * et établir le contexte d'authentification pour les utilisateurs authentifiés
 */
@Component
public class JwtAuthenticationFilter implements WebFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final List<String> publicPaths = List.of(
        "/auth/login",
        "/auth/register", 
        "/auth/logout",
        "/",
        "/swagger-ui.html", 
        "/swagger-ui/", 
        "/v3/api-docs/", 
        "/webjars/",
        "/swagger-resources/",
        "/favicon.ico"
    );
    
    private final Predicate<String> isPublicPath = path -> 
        publicPaths.stream().anyMatch(publicPath -> path.startsWith(publicPath));

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        
        // Log pour le débogage
        logger.debug("JWT Filter processing request for path: {}", path);
        
        // Si le chemin est public, passer au filtre suivant sans vérifier le token
        if (isPublicPath.test(path)) {
            logger.debug("Public path detected, skipping JWT validation: {}", path);
            return chain.filter(exchange);
        }
        
        // Récupérer le header Authorization
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // Si pas de header ou pas au format Bearer, renvoyer une erreur 401
        if (authHeader == null || authHeader.isEmpty() || !authHeader.startsWith("Bearer ")) {
            logger.debug("No valid Authorization header found for path: {}", path);
            return sendUnauthorizedResponse(exchange, "Token d'authentification manquant ou invalide");
        }

        // Extraire le token JWT
        String jwt = authHeader.substring(7);
        
        // Si le token est valide, établir l'authentification
        if (jwtUtil.validateToken(jwt)) {
            String username = jwtUtil.extractUsername(jwt);
            logger.debug("Valid JWT token for user: {} accessing path: {}", username, path);
            
            // Créer une authentification avec une autorité USER
            List<SimpleGrantedAuthority> authorities = 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
            
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(username, null, authorities);
            
            // Ajouter des informations supplémentaires à la requête HTTP si nécessaire
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-Auth-User-ID", jwtUtil.extractUserId(jwt))
                .header("X-Auth-Telephone", jwtUtil.extractTelephone(jwt))
                .build();
            
            // Mettre à jour l'échange avec la nouvelle requête
            ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
            
            // Établir le contexte de sécurité pour cette requête
            return chain.filter(mutatedExchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
        } else {
            logger.debug("Invalid JWT token for path: {}", path);
            return sendUnauthorizedResponse(exchange, "Token d'authentification expiré ou invalide");
        }
    }

    /**
     * Envoie une réponse 401 Unauthorized avec un message d'erreur JSON
     * @param exchange L'échange WebFlux
     * @param message Le message d'erreur à inclure dans la réponse
     * @return Mono<Void> complété lorsque la réponse est envoyée
     */
    private Mono<Void> sendUnauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        String jsonBody = String.format("{\"status\":\"error\",\"message\":\"%s\"}", message);
        byte[] bytes = jsonBody.getBytes(StandardCharsets.UTF_8);
        
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)))
                      .then(response.setComplete());
    }
}
