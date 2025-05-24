package com.innov4africa.service_aggregation.model;

import java.util.List;

public class SoldeResponse {
    private String status;
    private String message;
    private String montant;
    private List<ServiceStatus> serviceStatuses;

    // Constructeurs
    public SoldeResponse(String status, String message, String montant, List<ServiceStatus> serviceStatuses) {
        this.status = status;
        this.message = message;
        this.montant = montant;
        this.serviceStatuses = serviceStatuses;
    }

    // Getters
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public String getMontant() { return montant; }
    public List<ServiceStatus> getServiceStatuses() { return serviceStatuses; }
}
