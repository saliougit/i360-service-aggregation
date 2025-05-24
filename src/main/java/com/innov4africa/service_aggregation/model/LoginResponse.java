package com.innov4africa.service_aggregation.model;

import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "loginResponse", namespace = "http://runtime.services.cash.innov.sn/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"error", "message", "token", "nom", "prenom"})
public class LoginResponse {

    @XmlElement(required = true, namespace = "http://runtime.services.cash.innov.sn/")
    private String error;

    @XmlElement(namespace = "http://runtime.services.cash.innov.sn/")
    private String message;

    @XmlElement(namespace = "http://runtime.services.cash.innov.sn/")
    private String token;

    @XmlElement(namespace = "http://runtime.services.cash.innov.sn/")
    private String nom;

    @XmlElement(namespace = "http://runtime.services.cash.innov.sn/")
    private String prenom;

    public LoginResponse() {}

    // Getters et setters
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }
}
