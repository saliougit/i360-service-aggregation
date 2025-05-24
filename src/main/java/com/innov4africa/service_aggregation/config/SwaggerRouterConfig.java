package com.innov4africa.service_aggregation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springdoc.core.properties.SwaggerUiConfigParameters;

/**
 * Configuration spécifique pour les routes liées à Swagger UI dans l'environnement WebFlux
 */
@Configuration
public class SwaggerRouterConfig {

    /**
     * Configure les routes pour la documentation OpenAPI dans l'environnement WebFlux
     */
    @Bean
    public RouterFunction<ServerResponse> swaggerRouterFunction(SwaggerUiConfigParameters swaggerUiConfigParameters) {
        return RouterFunctions.route()
                .GET("/", request -> 
                    ServerResponse.temporaryRedirect(
                        java.net.URI.create("/swagger-ui.html")
                    ).build()
                )
                .build();
    }
}
