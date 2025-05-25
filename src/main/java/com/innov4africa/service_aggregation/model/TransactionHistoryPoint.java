package com.innov4africa.service_aggregation.model;

public class TransactionHistoryPoint {
    private String date;
    private String solde;
    private String periode;

    public TransactionHistoryPoint(String date, String solde, String periode) {
        this.date = date;
        this.solde = solde;
        this.periode = periode;
    }

    // Getters et Setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getSolde() { return solde; }
    public void setSolde(String solde) { this.solde = solde; }
    public String getPeriode() { return periode; }
    public void setPeriode(String periode) { this.periode = periode; }
}
