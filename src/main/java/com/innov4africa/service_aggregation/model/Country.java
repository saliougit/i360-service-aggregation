package com.innov4africa.service_aggregation.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Country {
    @JsonProperty("country_id")
    private Integer countryId;
    private String name;
    private Boolean supprime;
    private String indicateur;

    // Getters and Setters
    public Integer getCountryId() { return countryId; }
    public void setCountryId(Integer countryId) { this.countryId = countryId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Boolean getSupprime() { return supprime; }
    public void setSupprime(Boolean supprime) { this.supprime = supprime; }
    
    public String getIndicateur() { return indicateur; }
    public void setIndicateur(String indicateur) { this.indicateur = indicateur; }
}
