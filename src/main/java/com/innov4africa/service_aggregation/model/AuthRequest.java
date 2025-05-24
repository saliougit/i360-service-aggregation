package com.innov4africa.service_aggregation.model;

public class AuthRequest {

    private String email;
    private String password;

    // Constructeurs, getters, setters
    public AuthRequest() {}

    public AuthRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

}
