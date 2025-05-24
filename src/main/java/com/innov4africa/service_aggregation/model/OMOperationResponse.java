package com.innov4africa.service_aggregation.model;

import java.util.List;

/**
 * Classe représentant la réponse HTTP pour une opération Orange Money (prélèvement ou virement)
 */
public class OMOperationResponse {
    private String status;            // "success" ou "error"
    private String message;           // Message descriptif
    private String transactionId;     // Identifiant de la transaction
    private String requestId;         // Identifiant de la requête
    private List<ServiceStatus> services;  // Statut des services
    
    // Constructeurs
    public OMOperationResponse() {
    }
    
    public OMOperationResponse(String status, String message, String transactionId, 
                              String requestId, List<ServiceStatus> services) {
        this.status = status;
        this.message = message;
        this.transactionId = transactionId;
        this.requestId = requestId;
        this.services = services;
    }
    
    // Constructeur simplifié
    public OMOperationResponse(String status, String message, List<ServiceStatus> services) {
        this.status = status;
        this.message = message;
        this.services = services;
    }
    
    // Getters et setters
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public List<ServiceStatus> getServices() {
        return services;
    }
    
    public void setServices(List<ServiceStatus> services) {
        this.services = services;
    }
}
