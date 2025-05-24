package com.innov4africa.service_aggregation.model;

import java.util.List;

/**
 * Classe représentant la réponse HTTP pour un envoi de code de paiement par SMS
 */
public class SmsPayResponse {
    private String status;
    private String message;
    private List<ServiceStatus> services;

    public SmsPayResponse() {
    }

    public SmsPayResponse(String status, String message, List<ServiceStatus> services) {
        this.status = status;
        this.message = message;
        this.services = services;
    }

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

    public List<ServiceStatus> getServices() {
        return services;
    }

    public void setServices(List<ServiceStatus> services) {
        this.services = services;
    }
}
