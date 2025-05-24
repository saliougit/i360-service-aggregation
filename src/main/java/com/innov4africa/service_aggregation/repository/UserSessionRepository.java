package com.innov4africa.service_aggregation.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository pour stocker les sessions utilisateur et leurs informations associées
 */
@Repository
public class UserSessionRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(UserSessionRepository.class);
    
    // Map pour stocker les informations de session utilisateur par token IPay
    private final Map<String, UserSessionInfo> userSessionMap = new ConcurrentHashMap<>();
    
    public void saveUserSession(String ipayToken, String userId, String telephone) {
        logger.info("Sauvegarde des informations de session pour le token: {}", ipayToken);
        userSessionMap.put(ipayToken, new UserSessionInfo(userId, telephone));
    }
    
    /**
     * Met à jour les informations de session avec l'ID du compte
     * @param ipayToken Token IPay de la session
     * @param accountIdIPay ID du compte IPay
     */
    public void updateWithAccountId(String ipayToken, String accountIdIPay) {
        logger.info("Mise à jour de la session avec l'ID du compte: {} pour le token: {}", accountIdIPay, ipayToken);
        UserSessionInfo info = userSessionMap.get(ipayToken);
        if (info != null) {
            info.setAccountIdIPay(accountIdIPay);
        } else {
            logger.warn("Tentative de mise à jour d'une session inexistante avec l'ID du compte");
            // Créer une nouvelle entrée avec uniquement l'ID du compte
            userSessionMap.put(ipayToken, new UserSessionInfo(null, null, accountIdIPay));
        }
    }
    
    /**
     * Récupère l'ID du compte associé à un token IPay
     * @param ipayToken Token IPay de la session
     * @return L'ID du compte ou null si non trouvé
     */
    public String getAccountIdIPay(String ipayToken) {
        UserSessionInfo info = userSessionMap.get(ipayToken);
        return info != null ? info.getAccountIdIPay() : null;
    }
    
    public UserSessionInfo getUserSessionInfo(String ipayToken) {
        return userSessionMap.get(ipayToken);
    }
    
    public boolean hasSessionInfo(String ipayToken) {
        return userSessionMap.containsKey(ipayToken);
    }
    
    /**
     * Classe interne pour stocker les informations d'une session utilisateur
     */
    public static class UserSessionInfo {
        private final String userId;
        private final String telephone;
        private String accountIdIPay; // Nouveau champ pour l'ID du compte
        
        public UserSessionInfo(String userId, String telephone) {
            this.userId = userId;
            this.telephone = telephone;
            this.accountIdIPay = null;
        }
        
        public UserSessionInfo(String userId, String telephone, String accountIdIPay) {
            this.userId = userId;
            this.telephone = telephone;
            this.accountIdIPay = accountIdIPay;
        }
        
        public String getUserId() {
            return userId;
        }
        
        public String getTelephone() {
            return telephone;
        }
        
        public String getAccountIdIPay() {
            return accountIdIPay;
        }
        
        public void setAccountIdIPay(String accountIdIPay) {
            this.accountIdIPay = accountIdIPay;
        }
    }
}
