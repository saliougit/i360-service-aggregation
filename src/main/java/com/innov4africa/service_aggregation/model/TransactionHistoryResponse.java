package com.innov4africa.service_aggregation.model;

import java.util.List;

public class TransactionHistoryResponse {
    private String status;
    private String message;   
    private List<TransactionHistoryPoint> data;

    public TransactionHistoryResponse(String status, String message, List<TransactionHistoryPoint> data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // Getters et Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }    public List<TransactionHistoryPoint> getData() { return data; }
    public void setData(List<TransactionHistoryPoint> data) { this.data = data; }
}


