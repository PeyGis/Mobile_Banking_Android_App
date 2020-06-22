package com.example.pagescoffie.nwallet.model;

/**
 * Created by Pages Coffie on 5/30/2018.
 */

public class MerchantModel {
    private int merchantId;
    private String merchantName;

    public MerchantModel(int merchantId, String merchantName) {
        this.merchantId = merchantId;
        this.merchantName = merchantName;
    }

    public int getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(int merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }
}
