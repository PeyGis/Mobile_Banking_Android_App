package com.example.pagescoffie.nwallet.model;

/**
 * Created by Pages Coffie on 5/28/2018.
 */

public class TransactionHistory {
    private int transactionId;
    private String details;
    private String debitCreditStatus;
    private String date;
    private double amount;

    public TransactionHistory(int transactionId, String details, String debitCreditStatus, String date, double amount) {
        this.transactionId = transactionId;
        this.details = details;
        this.debitCreditStatus = debitCreditStatus;
        this.date = date;
        this.amount = amount;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getDebitCreditStatus() {
        return debitCreditStatus;
    }

    public void setDebitCreditStatus(String debitCreditStatus) {
        this.debitCreditStatus = debitCreditStatus;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
