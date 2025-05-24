package com.innov4africa.service_aggregation.model;

/**
 * Classe représentant une requête d'opération (prélèvement ou virement) vers Wave
 */
public class WaveOperationRequest {
    private String destinatairePaye; // Numéro de téléphone du destinataire
    private String montant;         // Montant de l'opération
    private String envoyeurPayeur;  // Numéro de téléphone de l'émetteur (facultatif, peut être extrait du JWT)
    private String commission;      // Commission (facultatif, peut être calculée ou = 0)
    
    // Constructeurs
    public WaveOperationRequest() {
    }
    
    public WaveOperationRequest(String destinatairePaye, String montant, String envoyeurPayeur, String commission) {
        this.destinatairePaye = destinatairePaye;
        this.montant = montant;
        this.envoyeurPayeur = envoyeurPayeur;
        this.commission = commission;
    }
    
    // Getters et setters
    public String getDestinatairePaye() {
        return destinatairePaye;
    }
    
    public void setDestinatairePaye(String destinatairePaye) {
        this.destinatairePaye = destinatairePaye;
    }
    
    public String getMontant() {
        return montant;
    }
    
    public void setMontant(String montant) {
        this.montant = montant;
    }
    
    public String getEnvoyeurPayeur() {
        return envoyeurPayeur;
    }
    
    public void setEnvoyeurPayeur(String envoyeurPayeur) {
        this.envoyeurPayeur = envoyeurPayeur;
    }
    
    public String getCommission() {
        return commission;
    }
    
    public void setCommission(String commission) {
        this.commission = commission;
    }
    
    @Override
    public String toString() {
        return "WaveOperationRequest{" +
                "destinatairePaye='" + destinatairePaye + '\'' +
                ", montant='" + montant + '\'' +
                ", envoyeurPayeur='" + envoyeurPayeur + '\'' +
                ", commission='" + commission + '\'' +
                '}';
    }
}
