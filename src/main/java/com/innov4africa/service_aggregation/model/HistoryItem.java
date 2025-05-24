package com.innov4africa.service_aggregation.model;

public class HistoryItem {
    private String date;
    private String id;
    private String solde;

    // Constructeurs
    public HistoryItem() {}

    public HistoryItem(String date, String id, String solde) {
        this.date = date;
        this.id = id;
        this.solde = solde;
    }

    // Getters et Setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSolde() {
        return solde;
    }

    public void setSolde(String solde) {
        this.solde = solde;
    }

    @Override
    public String toString() {
        return "HistoryItem{" +
                "date='" + date + '\'' +
                ", id='" + id + '\'' +
                ", solde='" + solde + '\'' +
                '}';
    }
    
    // Pour maintenir la compatibilité avec le code existant
    public String getMontant() {
        return solde;
    }
    
    public void setMontant(String montant) {
        this.solde = montant;
    }
    
    public String getOperation() {
        return "SOLDE";
    }
    
    public void setOperation(String operation) {
        // Cette méthode est conservée pour la compatibilité mais n'est pas utilisée avec le nouveau format
    }
}
