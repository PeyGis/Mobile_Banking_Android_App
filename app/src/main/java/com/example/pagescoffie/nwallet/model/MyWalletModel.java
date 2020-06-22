package com.example.pagescoffie.nwallet.model;

/**
 * Created by Pages Coffie on 5/26/2018.
 */

public class MyWalletModel {
    private String walletId;
    private double walletBalance;
    private String networkOperator;

    public MyWalletModel(String walletId, double walletBalance, String networkOperator) {
        this.walletId = walletId;
        this.walletBalance = walletBalance;
        this.networkOperator = networkOperator;
    }

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public double getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(double walletBalance) {
        this.walletBalance = walletBalance;
    }

    public String getNetworkOperator() {
        return networkOperator;
    }

    public void setNetworkOperator(String networkOperator) {
        this.networkOperator = networkOperator;
    }
}
