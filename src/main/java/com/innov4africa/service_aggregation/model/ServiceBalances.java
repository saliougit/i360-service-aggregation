package com.innov4africa.service_aggregation.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ServiceBalances {
    @JsonProperty("ipay")
    private double ipay;
    
    @JsonProperty("ibanking")
    private double ibanking;

    public ServiceBalances(double ipay, double ibanking) {
        this.ipay = ipay;
        this.ibanking = ibanking;
    }

    public double getIpay() {
        return ipay;
    }

    public void setIpay(double ipay) {
        this.ipay = ipay;
    }

    public double getIbanking() {
        return ibanking;
    }

    public void setIbanking(double ibanking) {
        this.ibanking = ibanking;
    }
}