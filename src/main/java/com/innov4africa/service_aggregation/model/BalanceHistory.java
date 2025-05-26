package com.innov4africa.service_aggregation.model;

import java.time.LocalDateTime;

public class BalanceHistory {
    private LocalDateTime date;
    private double balance;

    public BalanceHistory() {
    }

    public BalanceHistory(LocalDateTime date, double balance) {
        this.date = date;
        this.balance = balance;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
