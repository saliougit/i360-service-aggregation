package com.innov4africa.service_aggregation.model;

import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "login", namespace = "http://runtime.services.cash.innov.sn/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"login", "password", "mode", "token"}) // Ordre des éléments XML
public class LoginRequest {

    @XmlElement(required = true, namespace = "http://runtime.services.cash.innov.sn/")
    private String login;

    @XmlElement(required = true, namespace = "http://runtime.services.cash.innov.sn/")
    private String password;

    @XmlElement(required = true, namespace = "http://runtime.services.cash.innov.sn/")
    private String mode;

    @XmlElement(required = true, namespace = "http://runtime.services.cash.innov.sn/")
    private String token;

    public LoginRequest() {
        this.mode = "APP";
        this.token = "?";
    }

    public LoginRequest(String login, String password) {
        this();
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
