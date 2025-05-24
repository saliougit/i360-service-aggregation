package com.innov4africa.service_aggregation.model;

import java.util.List;

/**
 * Classe représentant la réponse HTTP pour une opération Wave (prélèvement ou virement)
 */
public class WaveOperationResponse {
    private String status;            // "success" ou "error"
    private String message;           // Message descriptif
    private String qrCodeUrl;         // URL de l'image QR code à scanner
    private String wavePayUrl;        // URL de paiement Wave
    private String qrCodeImage;       // Image QR code encodée en base64
    private String transactionId;     // Identifiant de la transaction
    private String requestId;         // Identifiant de la requête
    private List<ServiceStatus> services;  // Statut des services
    
    // Constructeurs
    public WaveOperationResponse() {
    }
    
    public WaveOperationResponse(String status, String message, String qrCodeUrl, String wavePayUrl,
                                String qrCodeImage, String transactionId, String requestId, 
                                List<ServiceStatus> services) {
        this.status = status;
        this.message = message;
        this.qrCodeUrl = qrCodeUrl;
        this.wavePayUrl = wavePayUrl;
        this.qrCodeImage = qrCodeImage;
        this.transactionId = transactionId;
        this.requestId = requestId;
        this.services = services;
    }
    
    // Constructeur simplifié
    public WaveOperationResponse(String status, String message, List<ServiceStatus> services) {
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
    
    public String getQrCodeUrl() {
        return qrCodeUrl;
    }
    
    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }
    
    public String getWavePayUrl() {
        return wavePayUrl;
    }
    
    public void setWavePayUrl(String wavePayUrl) {
        this.wavePayUrl = wavePayUrl;
    }
    
    public String getQrCodeImage() {
        return qrCodeImage;
    }
    
    public void setQrCodeImage(String qrCodeImage) {
        this.qrCodeImage = qrCodeImage;
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
