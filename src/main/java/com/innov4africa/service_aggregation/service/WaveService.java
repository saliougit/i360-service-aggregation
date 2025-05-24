package com.innov4africa.service_aggregation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.SSLException;
import java.util.Map;

/**
 * Service pour les opérations avec Wave via l'API iSwitch
 */
@Service
public class WaveService {

    private static final Logger logger = LoggerFactory.getLogger(WaveService.class);
    
    private final WebClient webClient;
    
    @Value("${iswitch.api.url:https://ibusinesscompanies.com:8443/iswitch-ws}")
    private String iswitchApiUrl;
    
    @Value("${wave.membre.payeur:SN001}")
    private String membrePayeur;
    
    @Value("${wave.membre.paye:SN005}")
    private String membrePaye;

    public WaveService(WebClient.Builder webClientBuilder, 
                      @Value("${iswitch.api.url:https://ibusinesscompanies.com:8443/iswitch-ws}") String apiUrl) {
        this.iswitchApiUrl = apiUrl;
        
        // Configuration de WebClient avec SSL ignorant les certificats invalides
        HttpClient httpClient = createSslIgnoringHttpClient();
        
        this.webClient = webClientBuilder
            .baseUrl(apiUrl)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }

    /**
     * Crée un HttpClient qui ignore les problèmes de certificats SSL
     */
    private HttpClient createSslIgnoringHttpClient() {
        try {
            SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
            
            return HttpClient.create().secure(t -> t.sslContext(sslContext));
        } catch (SSLException e) {
            logger.error("Erreur lors de la création du contexte SSL", e);
            // Fallback vers un client HTTP standard en cas d'erreur
            return HttpClient.create();
        }
    }

    /**
     * Récupère les frais de commission pour un type d'opération
     * 
     * @param montant Montant de l'opération
     * @param service Type de service ("PRELEVEMENT" ou "VIREMENT")
     * @param type Type de transaction (généralement "NORMAL")
     * @return Un objet JSON contenant les informations de commission
     */
    public Mono<Map<String, Object>> getCommission(String montant, String service, String type) {
        logger.info("Récupération des frais de commission pour {} de montant {}", service, montant);
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/service/getCommission/systeme")
                    .queryParam("montant", montant)
                    .queryParam("service", service)
                    .queryParam("type", type)
                    .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
                .doOnNext(response -> logger.debug("Réponse commission: {}", response))
                .doOnError(err -> logger.error("Erreur lors de la récupération des commissions", err));
    }

    /**
     * Effectue un prélèvement Wave
     * 
     * @param destinatairePaye Numéro de téléphone du bénéficiaire
     * @param montant Montant à prélever
     * @param envoyeurPayeur Numéro de téléphone de l'émetteur
     * @param commission Frais de commission (peut être 0)
     * @return Un objet JSON contenant la réponse avec URL QR code et statut
     */
    public Mono<Map<String, Object>> prelevement(String destinatairePaye, String montant, 
                                              String envoyeurPayeur, String commission) {
        logger.info("Demande de prélèvement Wave de {} vers {} pour un montant de {}", 
                 envoyeurPayeur, destinatairePaye, montant);
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/service/prelevement/wave")
                    .queryParam("destinataire_paye", destinatairePaye)
                    .queryParam("montant", montant)
                    .queryParam("membre_payeur", membrePaye)  // Pour un prélèvement, l'ordre est inversé
                    .queryParam("membre_paye", membrePayeur)
                    .queryParam("envoyeur_payeur", envoyeurPayeur)
                    .queryParam("commission", commission)
                    .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
                .doOnNext(response -> logger.debug("Réponse prélèvement: {}", response))
                .doOnError(err -> logger.error("Erreur lors du prélèvement Wave", err));
    }

    /**
     * Effectue un virement Wave
     * 
     * @param destinatairePaye Numéro de téléphone du bénéficiaire
     * @param montant Montant à virer
     * @param envoyeurPayeur Numéro de téléphone de l'émetteur
     * @param commission Frais de commission (peut être 0)
     * @return Un objet JSON contenant la réponse avec URL QR code et statut
     */
    public Mono<Map<String, Object>> virement(String destinatairePaye, String montant, 
                                           String envoyeurPayeur, String commission) {
        logger.info("Demande de virement Wave de {} vers {} pour un montant de {}", 
                 envoyeurPayeur, destinatairePaye, montant);
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/service/virement/wave")
                    .queryParam("destinataire_paye", destinatairePaye)
                    .queryParam("montant", montant)
                    .queryParam("membre_payeur", membrePayeur)  // Pour un virement, l'ordre est normal
                    .queryParam("membre_paye", membrePaye)
                    .queryParam("envoyeur_payeur", envoyeurPayeur)
                    .queryParam("commission", commission)
                    .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
                .doOnNext(response -> logger.debug("Réponse virement: {}", response))
                .doOnError(err -> logger.error("Erreur lors du virement Wave", err));
    }
}
