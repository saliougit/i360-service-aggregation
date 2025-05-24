package com.innov4africa.service_aggregation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IPayLoginResponse {
    private String error;
    private String iduser;
    private String message;
    private String nom;
    private String prenom;
    private String profil;
    private String telephone;
    private String token;

    // Getters and setters
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getIduser() { return iduser; }
    public void setIduser(String iduser) { this.iduser = iduser; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getProfil() { return profil; }
    public void setProfil(String profil) { this.profil = profil; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public boolean isSuccess() {
        return "0".equals(error) && message != null && message.contains("success");
    }
}
