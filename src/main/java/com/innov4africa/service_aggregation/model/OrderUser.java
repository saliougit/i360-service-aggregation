package com.innov4africa.service_aggregation.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderUser {
    private Integer id;
    private String login;
    private String nom;
    private String prenom;
    @JsonProperty("mobile_no")
    private Integer mobileNo;
    private String adresse;
    private City city;
    @JsonProperty("imageProfil")
    private String imageProfil;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    
    public Integer getMobileNo() { return mobileNo; }
    public void setMobileNo(Integer mobileNo) { this.mobileNo = mobileNo; }
    
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    
    public City getCity() { return city; }
    public void setCity(City city) { this.city = city; }
    
    public String getImageProfil() { return imageProfil; }
    public void setImageProfil(String imageProfil) { this.imageProfil = imageProfil; }
}
