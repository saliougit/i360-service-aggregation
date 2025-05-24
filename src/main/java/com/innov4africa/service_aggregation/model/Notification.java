package com.innov4africa.service_aggregation.model;

/**
 * Classe représentant une notification provenant de IPay
 */
public class Notification {
    private String id;
    private String date;
    private String message;
    private String type;
    private String status;
    
    public Notification() {
    }
    
    public Notification(String id, String date, String message, String type, String status) {
        this.id = id;
        this.date = date;
        this.message = message;
        this.type = type;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
