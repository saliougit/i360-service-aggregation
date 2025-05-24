package com.innov4africa.service_aggregation.model;

import java.util.List;

/**
 * Classe représentant la réponse HTTP pour les notifications
 */
public class NotificationResponse {
    private String status;
    private String message;
    private List<Notification> notifications;
    private List<ServiceStatus> services;

    public NotificationResponse() {
    }

    public NotificationResponse(String status, String message, List<Notification> notifications, List<ServiceStatus> services) {
        this.status = status;
        this.message = message;
        this.notifications = notifications;
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

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }

    public List<ServiceStatus> getServices() {
        return services;
    }

    public void setServices(List<ServiceStatus> services) {
        this.services = services;
    }
}
