package com.innov4africa.service_aggregation.model;

public class UO {
    private String error;
    private String id;
    private String nom;
    private String numTel;
    private String prenom;
    private String type;

    // Constructeur par défaut nécessaire pour la désérialisation
    public UO() {}

    // Constructeur avec tous les champs
    public UO(String error, String id, String nom, String numTel, String prenom, String type) {
        this.error = error;
        this.id = id;
        this.nom = nom;
        this.numTel = numTel;
        this.prenom = prenom;
        this.type = type;
    }

    // Getters et Setters
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getNumTel() {
        return numTel;
    }

    public void setNumTel(String numTel) {
        this.numTel = numTel;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "UO{" +
                "error='" + error + '\'' +
                ", id='" + id + '\'' +
                ", nom='" + nom + '\'' +
                ", numTel='" + numTel + '\'' +
                ", prenom='" + prenom + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
