package com.innov4africa.service_aggregation.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.List;

// @Configuration
// @EnableWebFluxSecurity
// public class SecurityConfig {

//     private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

//     @Autowired
//     private JwtAuthenticationFilter jwtAuthenticationFilter;

//     @Bean
//     public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
//         logger.info("Configuring security web filter chain");
        
//         return http
//                 .csrf(ServerHttpSecurity.CsrfSpec::disable)
//                 .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//                 // Utiliser NoOpServerSecurityContextRepository pour éviter de stocker le contexte de sécurité
//                 // et s'appuyer uniquement sur les tokens JWT à chaque requête
//                 .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
//                 .authorizeExchange(exchanges -> exchanges
//                         // Routes publiques accessibles sans authentification
//                         .pathMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", 
//                                      "/webjars/**", "/swagger-resources/**", "/favicon.ico").permitAll()
//                         .pathMatchers("/", "/auth/login", "/auth/register", "/auth/logout").permitAll()
//                         // Toutes les autres routes nécessitent une authentification
//                         .anyExchange().authenticated()
//                 )
//                 // Désactiver le formulaire de login par défaut
//                 .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
//                 // Désactiver l'authentification HTTP basic
//                 .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
//                 // Ajouter le filtre JWT avant l'étape d'authentification
//                 .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
//                 .exceptionHandling(exceptionHandlingSpec -> exceptionHandlingSpec
//                     .authenticationEntryPoint((exchange, ex) -> {
//                         logger.warn("Accès non autorisé: {}, path: {}", 
//                                   ex.getMessage(), exchange.getRequest().getPath());
//                         // Définir explicitement le code de réponse 401 Unauthorized
//                         exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//                         return exchange.getResponse().setComplete();
//                     })
//                     .accessDeniedHandler((exchange, denied) -> {
//                         logger.warn("Accès refusé: {}, path: {}", 
//                                   denied.getMessage(), exchange.getRequest().getPath());
//                         exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                         return exchange.getResponse().setComplete();
//                     })
//                 )
//                 .build();
//     }
    
//     @Bean
//     public CorsConfigurationSource corsConfigurationSource() {
//         CorsConfiguration configuration = new CorsConfiguration();
//         configuration.setAllowedOrigins(List.of("*"));
//         configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
//         configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Origin", "Accept"));
//         configuration.setExposedHeaders(List.of("Authorization"));
//         configuration.setAllowCredentials(true);
//         configuration.setMaxAge(3600L);
        
//         UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//         source.registerCorsConfiguration("/**", configuration);
        
//         return source;
//     }
// }

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf().disable()
                .authorizeExchange(exchanges -> exchanges
                        .anyExchange().permitAll() // Autorise tout sans authentification
                )
                .httpBasic().disable()
                .formLogin().disable()
                .build();
    }
}
