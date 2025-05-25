package com.innov4africa.service_aggregation.model;

import java.util.List;

public class BalanceHistoryResponse {
    private String status;
    private String message;
    private List<BalanceHistoryPoint> history;
    private List<ServiceStatus> services;


    public BalanceHistoryResponse(String status, String message, List<BalanceHistoryPoint> history, List<ServiceStatus> services) {
        this.status = status;
        this.message = message;
        this.history = history;
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
    public List<BalanceHistoryPoint> getHistory() {
        return history;
    }

    public void setHistory(List<BalanceHistoryPoint> history) {
        this.history = history;
    }

    public List<ServiceStatus> getServices() {
        return services;
    }

    public void setServices(List<ServiceStatus> services) {
        this.services = services;
    }

    
}


