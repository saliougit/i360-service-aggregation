package com.innov4africa.service_aggregation.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.innov4africa.service_aggregation.utils.DateUtils;

public class BalanceHistoryPoint {
    @JsonIgnore
    private LocalDateTime rawDate;
    private double globalBalance;
    private double iPayBalance;
    private double iBankingBalance;
    @JsonIgnore
    private Period period;

    public BalanceHistoryPoint(LocalDateTime date, double globalBalance, double iPayBalance, double iBankingBalance, Period period) {
        this.rawDate = date;
        this.globalBalance = DateUtils.roundToTwoDecimals(globalBalance);
        this.iPayBalance = DateUtils.roundToTwoDecimals(iPayBalance);
        this.iBankingBalance = DateUtils.roundToTwoDecimals(iBankingBalance);
        this.period = period != null ? period : Period.getDefault();
    }

    public String getDate() {
        return period.formatDate(rawDate);
    }

    @JsonIgnore
    public LocalDateTime getRawDate() {
        return rawDate;
    }

    public void setDate(LocalDateTime date) {
        this.rawDate = date;
    }

    public double getGlobalBalance() {
        return globalBalance;
    }

    public void setGlobalBalance(double globalBalance) {
        this.globalBalance = DateUtils.roundToTwoDecimals(globalBalance);
    }

    public double getiPayBalance() {
        return iPayBalance;
    }

    public void setiPayBalance(double iPayBalance) {
        this.iPayBalance = DateUtils.roundToTwoDecimals(iPayBalance);
    }

    public double getiBankingBalance() {
        return iBankingBalance;
    }

    public void setiBankingBalance(double iBankingBalance) {
        this.iBankingBalance = DateUtils.roundToTwoDecimals(iBankingBalance);
    }

    @JsonIgnore
    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }
}
