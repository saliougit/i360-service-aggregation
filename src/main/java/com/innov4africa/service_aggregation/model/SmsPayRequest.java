package com.innov4africa.service_aggregation.model;

/**
 * Classe représentant une requête d'envoi de code de paiement par SMS
 */
public class SmsPayRequest {
    private String beneficiaireTel;
    private String montant;
    private String numeros;

    public SmsPayRequest() {
    }

    public SmsPayRequest(String beneficiaireTel, String montant, String numeros) {
        this.beneficiaireTel = beneficiaireTel;
        this.montant = montant;
        this.numeros = numeros;
    }

    public String getBeneficiaireTel() {
        return beneficiaireTel;
    }

    public void setBeneficiaireTel(String beneficiaireTel) {
        this.beneficiaireTel = beneficiaireTel;
    }

    public String getMontant() {
        return montant;
    }

    public void setMontant(String montant) {
        this.montant = montant;
    }

    public String getNumeros() {
        return numeros;
    }

    public void setNumeros(String numeros) {
        this.numeros = numeros;
    }
}
