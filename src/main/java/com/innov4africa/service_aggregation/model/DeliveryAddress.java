package com.innov4africa.service_aggregation.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DeliveryAddress {
    private Integer id;
    private String name;
    @JsonProperty("contact_number")
    private Integer contactNumber;
    @JsonProperty("adresse_type")
    private String adresseType;
    private String email;
    private String adresse;
    private Double latitude;
    private Double longitude;
    private Integer pincode;
    private OrderUser utilisateur;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Integer getContactNumber() { return contactNumber; }
    public void setContactNumber(Integer contactNumber) { this.contactNumber = contactNumber; }
    
    public String getAdresseType() { return adresseType; }
    public void setAdresseType(String adresseType) { this.adresseType = adresseType; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public Integer getPincode() { return pincode; }
    public void setPincode(Integer pincode) { this.pincode = pincode; }
    
    public OrderUser getUtilisateur() { return utilisateur; }
    public void setUtilisateur(OrderUser utilisateur) { this.utilisateur = utilisateur; }
}
