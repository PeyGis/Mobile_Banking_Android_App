package com.example.pagescoffie.nwallet.model;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by iCoffie on 24/5/2018.
 * This class manages user session
 */

public class SharedPrefManager {

    private static final String SHARED_PREF_NAME = "PrefSaveUserInfo";
    private static final String KEY_USER_NAME = "User_Name";
    private static final String KEY_USER_EMAIL = "Email";
    private static final String KEY_USER_ID = "User_Id";
    private static final String KEY_USER_PHONE = "Phone";
    private static final String FCM_TOKEN = "fcmtoken";
    private static final String KEY_USER_PIN = "User_PIN";
    private static final String KEY_USER_ACCOUNTS = "User_Accounts";
    private static final String KEY_USER_WALLETS = "User_Wallets";
    private static final String KEY_MERCHANTS = "Merchants";
    private static final String KEY_CONFIRM_PIN = "Confirm_PIN";
    private static final String KEY_OFFLINE_MODE = "Offline_Mode";

    private  static SharedPrefManager classinstance;
    private static Context context;

    private SharedPrefManager(Context cntxt)
    {
        context = cntxt;
    }

    public boolean saveUserDetails(int id, String uname, String email, String phone)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(KEY_USER_ID, id);
        editor.putString(KEY_USER_NAME, uname);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_PHONE, phone);

        editor.apply();

        return  true;
    }

    public boolean saveUserAccounts( String jsonObject)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(KEY_USER_ACCOUNTS, jsonObject);
        editor.apply();
        return  true;
    }
    public boolean saveUserWallets( String jsonObject)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(KEY_USER_WALLETS, jsonObject);
        editor.apply();
        return  true;
    }
    public boolean saveMerchants( String jsonObject)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(KEY_MERCHANTS, jsonObject);
        editor.apply();
        return  true;
    }

    public boolean confirmPINPreference( boolean value)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(KEY_CONFIRM_PIN, value);
        editor.apply();
        return  true;
    }

    public boolean offlineModePreference( boolean value)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(KEY_OFFLINE_MODE, value);
        editor.apply();
        return  true;
    }

    /**
     * a function clear user accounts
     * @return ture or false
     */
    public boolean deleteUserAccounts()
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_USER_ACCOUNTS);
        editor.apply();

        return true;
    }
    /**
     * A function to check if user is logged in
     * @return true or false
     */
    public boolean isLoggedIn()
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        if (sharedPreferences.getString(KEY_USER_NAME, null) != null){
            return  true;
        }
        return false;
    }

    /**
     * a function to log user out
     * @return ture or false
     */
    public boolean logout()
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_USER_NAME);
        editor.remove(KEY_USER_EMAIL);
        editor.remove(KEY_USER_PHONE);
        editor.remove(KEY_USER_ACCOUNTS);
        editor.remove(KEY_USER_WALLETS);
        editor.remove(KEY_MERCHANTS);
       // editor.clear();
        editor.apply();

        return true;
    }

    /**
     *
     * @param mycontext content
     * @return an instance of this class
     */
    public static synchronized SharedPrefManager getClassinstance(Context mycontext)
    {
        if (classinstance == null)
        {
            classinstance = new SharedPrefManager(mycontext);
        }
        return classinstance;
    }

    public String getUserName() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_NAME, "user");
    }

    public String getUserEmail() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_EMAIL, "admin@nsano.com");
    }

    public int getUserId() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_USER_ID, 1);
    }

    public String getUserPhone() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_PHONE, "0548771108");
    }

    //this method will fetch the user accounts from shared preferences
    public String getUserAccounts(){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return  sharedPreferences.getString(KEY_USER_ACCOUNTS, null);
    }

    //this method will fetch the user wallets from shared preferences
    public String getUserWallets(){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return  sharedPreferences.getString(KEY_USER_WALLETS, null);
    }
    //this method will fetch merchants from shared preferences
    public String getMerchants(){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return  sharedPreferences.getString(KEY_MERCHANTS, null);
    }

    //this method will fetch the device token from shared preferences
    public String getUserPin(){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return  sharedPreferences.getString(KEY_USER_PIN, null);
    }

    public boolean getConfirmPINPreference(){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return  sharedPreferences.getBoolean(KEY_CONFIRM_PIN, true);
    }

    public boolean getOfflineModePreference(){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return  sharedPreferences.getBoolean(KEY_OFFLINE_MODE, false);
    }
}
