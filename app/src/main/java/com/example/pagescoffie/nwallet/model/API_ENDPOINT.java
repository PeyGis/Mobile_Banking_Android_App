package com.example.pagescoffie.nwallet.model;

/**
 * Created by Pages Coffie on 5/22/2018.
 */

public class API_ENDPOINT {
    private static String endpoint = "http://192.168.0.58:8000";
    public static final String REGISTER_URL = endpoint + "/user/";
    public static final String LOGIN_URL = endpoint + "/login/";
    public static final String CHANGE_PIN_URL = endpoint + "/change_pin/";
    public static final String CHANGE_CONTACT_URL = endpoint + "/change_phone/";
    public static final String USER_ACCOUNTS_URL = endpoint + "/account/user/";
    public static final String USER_WALLETS_URL = endpoint + "/wallet/user/";
    public static final String ADD_WALLET_URL = endpoint + "/wallet/add/";
    public static final String TRANSACTION_HISTORY_URL = endpoint + "/tr/";
    public static final String RECHARGE_AIRTIME_URL = endpoint + "/tr/recharge/";
    public static final String ACCOUNT_ACCOUNT_TRANSFER_URL = endpoint + "/tr/account_account/";
    public static final String ACCOUNT_WALLET_TRANSFER_URL = endpoint + "/tr/account_wallet/";
    public static final String WALLET_ACCOUNT_TRANSFER_URL = endpoint + "/tr/wallet_account/";
    public static final String PAY_MERCHANT_URL = endpoint + "/merchants/pay/";
    public static final String MERCHANTS_URL = endpoint + "/merchants/";
    public static final String GENERIC_CHART_URL = endpoint + "/chart_generic/";
}
