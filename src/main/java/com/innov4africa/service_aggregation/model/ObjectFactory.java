package com.innov4africa.service_aggregation.model;

import jakarta.xml.bind.annotation.XmlRegistry;

/**
 * Cette classe contient des méthodes d'usine pour chaque contenu Java dans le package com.innov4africa.service_aggregation.model.
 */
@XmlRegistry
public class ObjectFactory {

    /**
     * Crée une nouvelle instance de ObjectFactory.
     */
    public ObjectFactory() {
    }

    /**
     * Crée une instance de {@link LoginRequest}.
     */
    public LoginRequest createLoginRequest() {
        return new LoginRequest();
    }

    /**
     * Crée une instance de {@link LoginResponse}.
     */
    public LoginResponse createLoginResponse() {
        return new LoginResponse();
    }

    /**
     * Crée une instance de {@link SoapRequest}.
     */
    public SoapRequest createSoapRequest() {
        return new SoapRequest();
    }

    /**
     * Crée une instance de {@link SoapResponse}.
     */
    public SoapResponse createSoapResponse() {
        return new SoapResponse();
    }

    /**
     * Crée une instance de {@link AuthResult}.
     */
    public AuthResult createAuthResult() {
        return new AuthResult(false, "");
    }

    /**
     * Crée une instance de {@link ServiceStatus}.
     */
    public ServiceStatus createServiceStatus() {
        return new ServiceStatus();
    }
}
