package com.innov4africa.service_aggregation.model;

/**
 * Classe représentant une requête de virement compte à compte
 */
public class TransferRequest {
    private String montant;
    private String idAccountBeneficiary;
    private String objet;
    private String commission;
    private String commissionRetrait;

    public TransferRequest() {
    }

    public TransferRequest(String montant, String idAccountBeneficiary, String objet, String commission, String commissionRetrait) {
        this.montant = montant;
        this.idAccountBeneficiary = idAccountBeneficiary;
        this.objet = objet;
        this.commission = commission;
        this.commissionRetrait = commissionRetrait;
    }

    public String getMontant() {
        return montant;
    }

    public void setMontant(String montant) {
        this.montant = montant;
    }

    public String getIdAccountBeneficiary() {
        return idAccountBeneficiary;
    }

    public void setIdAccountBeneficiary(String idAccountBeneficiary) {
        this.idAccountBeneficiary = idAccountBeneficiary;
    }

    public String getObjet() {
        return objet;
    }

    public void setObjet(String objet) {
        this.objet = objet;
    }

    public String getCommission() {
        return commission;
    }

    public void setCommission(String commission) {
        this.commission = commission;
    }

    public String getCommissionRetrait() {
        return commissionRetrait;
    }

    public void setCommissionRetrait(String commissionRetrait) {
        this.commissionRetrait = commissionRetrait;
    }
}
