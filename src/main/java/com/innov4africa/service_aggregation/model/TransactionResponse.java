package com.innov4africa.service_aggregation.model;

import java.util.List;

/**
 * Classe représentant la réponse HTTP pour les transactions
 */
public class TransactionResponse {
    private String status;
    private String message;
    private Integer total;
    private List<Transaction> transactions;
    private List<ServiceStatus> services;

    public TransactionResponse() {
    }

    public TransactionResponse(String status, String message, Integer total, List<Transaction> transactions, List<ServiceStatus> services) {
        this.status = status;
        this.message = message;
        this.total = total;
        this.transactions = transactions;
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

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public List<ServiceStatus> getServices() {
        return services;
    }

    public void setServices(List<ServiceStatus> services) {
        this.services = services;
    }
}
