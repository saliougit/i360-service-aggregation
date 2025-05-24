
package com.innov4africa.service_aggregation.service;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class SSLUtil {
    // **NE JAMAIS UTILISER CETTE MÉTHODE EN PRODUCTION!**
    // Elle désactive complètement la vérification SSL, ce qui rend l'application
    // vulnérable aux attaques de type "man-in-the-middle".

    // Utiliser cette méthode UNIQUEMENT pour le développement local et les tests,
    // et seulement si vous comprenez les risques.

    // Pour une production sécurisée, vous devez configurer un TrustStore
    // contenant les certificats des serveurs auxquels vous vous connectez.

    // Cette méthode est laissée ici à des fins de démonstration uniquement.
    public static void disableSslVerification() {
        try {
            // Créer un trust manager qui ne valide pas les chaînes de certificats
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
            };

            // Installer le trust manager
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Créer un hostname verifier qui accepte tous les noms d'hôte
            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Failed to disable SSL verification", e);
        }
    }
}
