package com.innov4africa.service_aggregation.model;

/**
 * Classe représentant une transaction/opération sur un compte
 */
public class Transaction {
    private String date;
    private String montant;
    private String typeOperation; // DEBIT/CREDIT
    private String typeTransaction; // Type spécifique (VIREMENT_ISWITCH, etc.)
    private String idTransaction; // ID unique de la transaction
    private String soldeCompte; // Solde du compte après la transaction
    private String source; // Pour distinguer entre ipay et ibanking
    
    public Transaction() {
    }
    
    public Transaction(String date, String montant, String typeOperation, String typeTransaction, 
                      String idTransaction, String soldeCompte, String source) {
        this.date = date;
        this.montant = montant;
        this.typeOperation = typeOperation;
        this.typeTransaction = typeTransaction;
        this.idTransaction = idTransaction;
        this.soldeCompte = soldeCompte;
        this.source = source;
    }

    // Constructeur de compatibilité pour l'ancien format
    public Transaction(String date, String montant, String type, String description, String reference) {
        this.date = date;
        this.montant = montant;
        this.typeOperation = type;
        this.typeTransaction = description;
        this.idTransaction = reference;
        this.soldeCompte = "";
        this.source = "legacy";
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMontant() {
        return montant;
    }

    public void setMontant(String montant) {
        this.montant = montant;
    }

    public String getTypeOperation() {
        return typeOperation;
    }

    public void setTypeOperation(String typeOperation) {
        this.typeOperation = typeOperation;
    }
    
    public String getTypeTransaction() {
        return typeTransaction;
    }

    public void setTypeTransaction(String typeTransaction) {
        this.typeTransaction = typeTransaction;
    }

    public String getIdTransaction() {
        return idTransaction;
    }

    public void setIdTransaction(String idTransaction) {
        this.idTransaction = idTransaction;
    }

    public String getSoldeCompte() {
        return soldeCompte;
    }

    public void setSoldeCompte(String soldeCompte) {
        this.soldeCompte = soldeCompte;
    }
    
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
    
    // Pour la compatibilité avec l'ancien format
    public String getType() {
        return typeOperation;
    }

    public void setType(String type) {
        this.typeOperation = type;
    }

    public String getDescription() {
        return typeTransaction;
    }

    public void setDescription(String description) {
        this.typeTransaction = description;
    }

    public String getReference() {
        return idTransaction;
    }

    public void setReference(String reference) {
        this.idTransaction = reference;
    }
}
