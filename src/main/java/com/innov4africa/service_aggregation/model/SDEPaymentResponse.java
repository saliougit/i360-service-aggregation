package com.innov4africa.service_aggregation.model;

import java.util.List;

/**
 * Classe représentant la réponse HTTP pour un paiement de facture SDE
 */
public class SDEPaymentResponse {
    private String status;
    private String message;
    private String reference;
    private List<ServiceStatus> services;

    public SDEPaymentResponse() {
    }

    public SDEPaymentResponse(String status, String message, String reference, List<ServiceStatus> services) {
        this.status = status;
        this.message = message;
        this.reference = reference;
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

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public List<ServiceStatus> getServices() {
        return services;
    }

    public void setServices(List<ServiceStatus> services) {
        this.services = services;
    }
}
