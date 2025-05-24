package com.innov4africa.service_aggregation.model;

/**
 * Classe représentant une requête de paiement Woyofal
 */
public class WoyofalPaymentRequest {
    private String numeroPolice;
    private String numeroTelephone;
    private String montant;
    private String frais;
    private String commission;
    private String cellular;

    public WoyofalPaymentRequest() {
    }

    public WoyofalPaymentRequest(String numeroPolice, String numeroTelephone, String montant, 
                              String frais, String commission, String cellular) {
        this.numeroPolice = numeroPolice;
        this.numeroTelephone = numeroTelephone;
        this.montant = montant;
        this.frais = frais;
        this.commission = commission;
        this.cellular = cellular;
    }

    public String getNumeroPolice() {
        return numeroPolice;
    }

    public void setNumeroPolice(String numeroPolice) {
        this.numeroPolice = numeroPolice;
    }

    public String getNumeroTelephone() {
        return numeroTelephone;
    }

    public void setNumeroTelephone(String numeroTelephone) {
        this.numeroTelephone = numeroTelephone;
    }

    public String getMontant() {
        return montant;
    }

    public void setMontant(String montant) {
        this.montant = montant;
    }

    public String getFrais() {
        return frais;
    }

    public void setFrais(String frais) {
        this.frais = frais;
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
}
