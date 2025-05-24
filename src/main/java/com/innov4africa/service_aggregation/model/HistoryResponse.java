package com.innov4africa.service_aggregation.model;

import java.util.List;

public class HistoryResponse {
    private String status;
    private String message;
    private List<HistoryItem> history;
    private List<ServiceStatus> serviceStatuses;

    // Constructeurs
    public HistoryResponse() {}

    public HistoryResponse(String status, String message, List<HistoryItem> history, List<ServiceStatus> serviceStatuses) {
        this.status = status;
        this.message = message;
        this.history = history;
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

    public List<HistoryItem> getHistory() {
        return history;
    }

    public void setHistory(List<HistoryItem> history) {
        this.history = history;
    }

    public List<ServiceStatus> getServiceStatuses() {
        return serviceStatuses;
    }

    public void setServiceStatuses(List<ServiceStatus> serviceStatuses) {
        this.serviceStatuses = serviceStatuses;
    }

    @Override
    public String toString() {
        return "HistoryResponse{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", history=" + history +
                ", serviceStatuses=" + serviceStatuses +
                '}';
    }
}
