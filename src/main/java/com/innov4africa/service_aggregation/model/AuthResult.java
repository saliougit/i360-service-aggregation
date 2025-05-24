
// package com.innov4africa.service_aggregation.model;

// public class AuthResult {
//     private boolean success;
//     private String message;
//     private String token;
//     private String nom;
//     private String prenom;

//     // Constructeur pour le cas sans token
//     public AuthResult(boolean success, String message) {
//         this.success = success;
//         this.message = message;
//     }

//     // Constructeur avec token
//     public AuthResult(boolean success, String message, String token) {
//         this.success = success;
//         this.message = message;
//         this.token = token;
//     }

//     // Nouveau constructeur avec tous les champs
//     public AuthResult(boolean success, String message, String token, String nom, String prenom) {
//         this.success = success;
//         this.message = message;
//         this.token = token;
//         this.nom = nom;
//         this.prenom = prenom;
//     }

//     // Getters et Setters pour tous les champs
//     public boolean isSuccess() {
//         return success;
//     }

//     public String getMessage() {
//         return message;
//     }

//     public String getToken() {
//         return token;
//     }

//     public String getNom() {
//         return nom;
//     }

//     public String getPrenom() {
//         return prenom;
//     }

//     public void setSuccess(boolean success) {
//         this.success = success;
//     }

//     public void setMessage(String message) {
//         this.message = message;
//     }

//     public void setToken(String token) {
//         this.token = token;
//     }

//     public void setNom(String nom) {
//         this.nom = nom;
//     }

//     public void setPrenom(String prenom) {
//         this.prenom = prenom;
//     }


// }

package com.innov4africa.service_aggregation.model;

public class AuthResult {
    private boolean success;
    private String message;
    private String token;       // Token IPay
    private String nom;
    private String prenom;
    private String telephone;
    private String iduser;
    private String profil;

    // Constructeur de base
    public AuthResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // Constructeur avec token IPay
    public AuthResult(boolean success, String message, String token) {
        this(success, message);
        this.token = token;
    }

    // Constructeur complet
    public AuthResult(boolean success, String message, String token, 
                     String nom, String prenom, String telephone, 
                     String iduser, String profil) {
        this(success, message, token);
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.iduser = iduser;
        this.profil = profil;
    }

    // Getters et Setters
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getIduser() {
        return iduser;
    }

    public String getProfil() {
        return profil;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void setIduser(String iduser) {
        this.iduser = iduser;
    }

    public void setProfil(String profil) {
        this.profil = profil;
    }

    @Override
    public String toString() {
        return "AuthResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", token='" + (token != null ? "***" : "null") + '\'' +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", telephone='" + telephone + '\'' +
                ", iduser='" + iduser + '\'' +
                ", profil='" + profil + '\'' +
                '}';
    }
}
