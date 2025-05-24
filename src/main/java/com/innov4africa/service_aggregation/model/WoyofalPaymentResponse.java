package com.innov4africa.service_aggregation.model;

import java.util.List;

/**
 * Classe représentant la réponse HTTP pour un paiement Woyofal
 */
public class WoyofalPaymentResponse {
    private String status;
    private String message;
    private String reference;
    private String code; // Code de rechargement Woyofal
    private String montantEnergy; // Montant équivalent en énergie
    private List<ServiceStatus> services;

    public WoyofalPaymentResponse() {
    }

    public WoyofalPaymentResponse(String status, String message, String reference, 
                               String code, String montantEnergy, List<ServiceStatus> services) {
        this.status = status;
        this.message = message;
        this.reference = reference;
        this.code = code;
        this.montantEnergy = montantEnergy;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMontantEnergy() {
        return montantEnergy;
    }

    public void setMontantEnergy(String montantEnergy) {
        this.montantEnergy = montantEnergy;
    }

    public List<ServiceStatus> getServices() {
        return services;
    }

    public void setServices(List<ServiceStatus> services) {
        this.services = services;
    }
}
