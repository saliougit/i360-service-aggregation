package com.innov4africa.service_aggregation.model;

import java.time.LocalDateTime;


public class BalanceHistoryPoint {
    private LocalDateTime date;
    private double globalBalance;
    private double iPayBalance;
    private double iBankingBalance;

    public BalanceHistoryPoint(LocalDateTime date, double globalBalance, double iPayBalance, double iBankingBalance) {
        this.date = date;
        this.globalBalance = globalBalance;
        this.iPayBalance = iPayBalance;
        this.iBankingBalance = iBankingBalance;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public double getGlobalBalance() {
        return globalBalance;
    }

    public void setGlobalBalance(double globalBalance) {
        this.globalBalance = globalBalance;
    }
    public double getiPayBalance() {
        return iPayBalance;
    }
    public void setiPayBalance(double iPayBalance) {
        this.iPayBalance = iPayBalance;
    }
    public double getiBankingBalance() {
        return iBankingBalance;
    }
    public void setiBankingBalance(double iBankingBalance) {
        this.iBankingBalance = iBankingBalance;
    }
    
}
