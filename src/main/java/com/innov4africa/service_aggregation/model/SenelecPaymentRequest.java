package com.innov4africa.service_aggregation.model;

/**
 * Classe représentant une requête de paiement de facture Senelec
 */
public class SenelecPaymentRequest {
    private String numeroPolice;
    private String numeroFacture;
    private String montant;
    private String commission;
    private String cellular;
    private String commagent;

    public SenelecPaymentRequest() {
    }

    public SenelecPaymentRequest(String numeroPolice, String numeroFacture, String montant, 
                               String commission, String cellular, String commagent) {
        this.numeroPolice = numeroPolice;
        this.numeroFacture = numeroFacture;
        this.montant = montant;
        this.commission = commission;
        this.cellular = cellular;
        this.commagent = commagent;
    }

    public String getNumeroPolice() {
        return numeroPolice;
    }

    public void setNumeroPolice(String numeroPolice) {
        this.numeroPolice = numeroPolice;
    }

    public String getNumeroFacture() {
        return numeroFacture;
    }

    public void setNumeroFacture(String numeroFacture) {
        this.numeroFacture = numeroFacture;
    }

    public String getMontant() {
        return montant;
    }

    public void setMontant(String montant) {
        this.montant = montant;
    }

    public String getCommission() {
        return commission;
    }

    public void setCommission(String commission) {
        this.commission = commission;
    }

    public String getCellular() {
        return cellular;
    }

    public void setCellular(String cellular) {
        this.cellular = cellular;
    }

    public String getCommagent() {
        return commagent;
    }

    public void setCommagent(String commagent) {
        this.commagent = commagent;
    }
}
