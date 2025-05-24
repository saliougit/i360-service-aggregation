package com.innov4africa.service_aggregation.model;

import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "Envelope", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"body"})
public class SoapResponse {

    @XmlElement(name = "Body", namespace = "http://schemas.xmlsoap.org/soap/envelope/", required = true)
    private SoapResponseBody body;  // Renommée ici

    public SoapResponse() {
    }

    public SoapResponseBody getBody() { // Renommée ici
        return body;
    }

    public void setBody(SoapResponseBody body) {  // Renommée ici
        this.body = body;
    }

    public LoginResponse getLoginResponse() {
        return body != null ? body.getLoginResponse() : null;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "Body", propOrder = {"loginResponse"})
    public static class SoapResponseBody {   // Renommée ici

        @XmlElement(name = "loginResponse", namespace = "http://runtime.services.cash.innov.sn/")
        private LoginResponse loginResponse;

        public SoapResponseBody() {
        }

        public LoginResponse getLoginResponse() {
            return loginResponse;
        }

        public void setLoginResponse(LoginResponse loginResponse) {
            this.loginResponse = loginResponse;
        }
    }
}
