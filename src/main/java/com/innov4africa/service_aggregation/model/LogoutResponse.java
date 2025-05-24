package com.innov4africa.service_aggregation.model;

import java.util.List;

public class LogoutResponse {
    private String status;
    private String message;
    private List<ServiceStatus> services;

    public LogoutResponse() {
    }

    public LogoutResponse(String status, String message, List<ServiceStatus> services) {
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
