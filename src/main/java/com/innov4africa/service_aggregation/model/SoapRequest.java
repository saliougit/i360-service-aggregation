package com.innov4africa.service_aggregation.model;

import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "Envelope", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"body"}) // Pas de header pour simplifier
public class SoapRequest {

    @XmlElement(name = "Body", namespace = "http://schemas.xmlsoap.org/soap/envelope/", required = true)
    private SoapRequestBody body;

    public SoapRequest() {
    }

    public SoapRequest(LoginRequest loginRequest) {
        this.body = new SoapRequestBody(loginRequest);
    }

    public SoapRequestBody getBody() {
        return body;
    }

    public void setBody(SoapRequestBody body) {
        this.body = body;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "Body", propOrder = {"login"})
    public static class SoapRequestBody {  // Renommée ici

        @XmlElement(name = "login", namespace = "http://runtime.services.cash.innov.sn/")
        private LoginRequest login;

        public SoapRequestBody() {
        }

        public SoapRequestBody(LoginRequest loginRequest) {
            this.login = loginRequest;
        }

        public LoginRequest getLogin() {
            return login;
        }

        public void setLogin(LoginRequest login) {
            this.login = login;
        }
    }
}
