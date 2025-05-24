package com.innov4africa.service_aggregation.model;

import java.util.List;

public class UOResponse {
    private String status;
    private String message;
    private UO uo;
    private List<ServiceStatus> serviceStatuses;

    // Constructeur par défaut nécessaire pour la désérialisation
    public UOResponse() {}

    // Constructeur avec tous les champs
    public UOResponse(String status, String message, UO uo, List<ServiceStatus> serviceStatuses) {
        this.status = status;
        this.message = message;
        this.uo = uo;
        this.serviceStatuses = serviceStatuses;
    }

    // Getters et Setters
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

    public UO getUo() {
        return uo;
    }

    public void setUo(UO uo) {
        this.uo = uo;
    }

    public List<ServiceStatus> getServiceStatuses() {
        return serviceStatuses;
    }

    public void setServiceStatuses(List<ServiceStatus> serviceStatuses) {
        this.serviceStatuses = serviceStatuses;
    }

    @Override
    public String toString() {
        return "UOResponse{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", uo=" + uo +
                ", serviceStatuses=" + serviceStatuses +
                '}';
    }
}
