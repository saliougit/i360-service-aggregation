package com.innov4africa.service_aggregation.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class City {
    @JsonProperty("city_id")
    private Integer cityId;
    private String name;
    private String identifiant;
    private Country country;
    private Boolean supprime;

    // Getters and Setters
    public Integer getCityId() { return cityId; }
    public void setCityId(Integer cityId) { this.cityId = cityId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getIdentifiant() { return identifiant; }
    public void setIdentifiant(String identifiant) { this.identifiant = identifiant; }
    
    public Country getCountry() { return country; }
    public void setCountry(Country country) { this.country = country; }
    
    public Boolean getSupprime() { return supprime; }
    public void setSupprime(Boolean supprime) { this.supprime = supprime; }
}
