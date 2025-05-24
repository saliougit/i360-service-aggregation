package com.innov4africa.service_aggregation.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dépôt pour gérer les tokens révoqués
 * Cette implémentation utilise un simple ConcurrentHashMap en mémoire
 * Dans un environnement de production, il faudrait utiliser une solution persistante comme Redis ou une base de données
 */
@Repository
public class TokenRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(TokenRepository.class);
    
    // Map pour stocker les tokens révoqués avec leur date d'expiration
    private final Map<String, Date> revokedTokens = new ConcurrentHashMap<>();
    
    /**
     * Sauvegarde un token révoqué avec sa date d'expiration
     * @param token Le token JWT révoqué
     * @param expiration La date d'expiration du token
     */
    public void saveRevokedToken(String token, Date expiration) {
        logger.info("Révocation du token avec expiration: {}", expiration);
        revokedTokens.put(token, expiration);
        
        // Nettoyage des tokens expirés (on pourrait aussi le faire via une tâche planifiée)
        cleanupExpiredTokens();
    }
    
    /**
     * Vérifie si un token est révoqué
     * @param token Le token JWT à vérifier
     * @return true si le token est révoqué, false sinon
     */
    public boolean isTokenRevoked(String token) {
        return revokedTokens.containsKey(token);
    }
    
    /**
     * Nettoyage des tokens expirés pour éviter de consommer trop de mémoire
     */
    private void cleanupExpiredTokens() {
        Date now = new Date();
        revokedTokens.entrySet().removeIf(entry -> entry.getValue().before(now));
    }
}
