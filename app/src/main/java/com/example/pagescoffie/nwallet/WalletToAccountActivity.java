package com.example.pagescoffie.nwallet;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.pagescoffie.nwallet.model.API_ENDPOINT;
import com.example.pagescoffie.nwallet.model.SharedPrefManager;
import com.example.pagescoffie.nwallet.model.SingletonAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WalletToAccountActivity extends AppCompatActivity {

    private Spinner walletsSpinner;
    private RadioGroup accountSelectionRadio;
    private TextInputLayout transferAccountTxtInput;
    private TextInputLayout amountTxtInput;
    private Button fundTransferButton;
    private ConnectivityManager connectivityManager;
    public static final int USSD_CALL_REQUEST = 10;

    ArrayList<String> userAccountsArray;
    ArrayList<String> userWalletsArray;
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_to_account);

        //set app toolbar
        Toolbar toolbar =(Toolbar) findViewById(R.id.action_bar);
        toolbar.setTitle(R.string.funds_transfer_toolbar_title);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorBackground));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //get view component
        walletsSpinner = (Spinner) findViewById(R.id.myWalletTransferSpinner);
        accountSelectionRadio = (RadioGroup) findViewById(R.id.fundTransferRadioGroup);
        transferAccountTxtInput = (TextInputLayout) findViewById(R.id.transferAccountNumberWrapper);
        amountTxtInput = (TextInputLayout) findViewById(R.id.transferAmountWrapper);
        fundTransferButton = (Button) findViewById(R.id.sendTransferBtn);

        //change spinner text color when selected
        walletsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView)view).setTextColor(Color.BLACK);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //init array for user accounts spinner
        userWalletsArray = new ArrayList<>();
        userAccountsArray = new ArrayList<>();

        //fetch user accounts
        getUserAccounts();

        //fetch user accounts
        getUserWallets();

        //set checked radio button and set account to user's synched accounts
        if (accountSelectionRadio.getCheckedRadioButtonId() == R.id.myAccountRadio) {
            if(userAccountsArray.size() <=0){
                transferAccountTxtInput.getEditText().setHint("Enter Account Number");
                showToast("No Accounts found");
            } else{
                transferAccountTxtInput.getEditText().setText(userAccountsArray.get(0));
            }
        }


        //set listener to radio button
        accountSelectionRadio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.myAccountRadio){
                    if(userAccountsArray.size() <=0){
                        showToast("No Accounts found");
                    } else{
                        transferAccountTxtInput.getEditText().setText(userAccountsArray.get(0));
                    }
                } else if (checkedId == R.id.otherAccountRadio){
                    transferAccountTxtInput.getEditText().setText("");

                } else{
                    //do nothing
                }
            }
        });


        //set listener to recharge button
        fundTransferButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String account = "";
                String amount = "";

                //get text from iedit texts
                transferAccountTxtInput.setError(null);
                amountTxtInput.setError(null);

                if(transferAccountTxtInput.getEditText() != null){
                    account = transferAccountTxtInput.getEditText().getText().toString();
                }
                if(amountTxtInput.getEditText() != null){
                    //amountTxtInput.getEditText().setText("0");
                    amount = amountTxtInput.getEditText().getText().toString();
                }

                //validate phone
                if (account.length() < 10){
                    transferAccountTxtInput.setError("Enter a valid account number");
                } else if ( amount.isEmpty() || ( !amount.isEmpty() && Double.parseDouble(amount) <= 0.0)){
                    amountTxtInput.setError("Amount must be greather than GHS 0.0");
                } else {

                    transferAccountTxtInput.setErrorEnabled(false);
                    amountTxtInput.setErrorEnabled(false);

                    //check if user selected an account
                    if(walletsSpinner.getSelectedItem().toString().equals("No Wallet") || walletsSpinner.getSelectedItem().toString().equals("Select Wallet")){

                    } else if(walletsSpinner.getSelectedItem().toString().equals(account)){
                        showToast("Cannot transfer to self account");
                    }
                    else{

                        AlertDialog.Builder enterPinBuilder = new AlertDialog.Builder(context);
                        enterPinBuilder.setTitle("Confirm Transfer");
                        enterPinBuilder.setMessage("Enter PIN to confirm your transfer of GHS " + amount + " to " +account);

                        LinearLayout container = new LinearLayout(context);
                        container.setOrientation(LinearLayout.VERTICAL);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                        lp.setMarginEnd(50);
                        lp.setMarginStart(50);
                        final EditText pin_input = new EditText(context);
                        pin_input.setHint("Your PIN");
                        pin_input.setTextColor(Color.BLUE);
                        pin_input.setInputType(InputType.TYPE_CLASS_NUMBER);
                        pin_input.setLayoutParams(lp);
                        container.addView(pin_input, lp);
                        enterPinBuilder.setView(container);

                        //set up builder for dialog
                        final String finalAmount = amount;
                        final String finalAccount = account;
                        enterPinBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String user_PIN = pin_input.getText().toString();
                                if(user_PIN.length() >=4)
                                    callWalletToAccountAPI(walletsSpinner.getSelectedItem().toString(), finalAmount, finalAccount, user_PIN);
                            }
                        });

                        //set cancel button
                        enterPinBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        //create a dialog from the builder
                        AlertDialog dialog = enterPinBuilder.create();
                        dialog.show();
                    }
                }

            }
        });
    }

    private void getUserWallets() {
        //show progress dialog
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching Wallet(s)...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        //first try getting user data from sharedpreference
        if (SharedPrefManager.getClassinstance(this).getUserWallets() != null) {
            String preferenceResponse = SharedPrefManager.getClassinstance(this).getUserWallets();
            try {
                JSONArray walletsArray = new JSONArray(preferenceResponse);
                if(walletsArray.length() ==0){
                    fundTransferButton.setEnabled(false);
                }

                for (int i = 0; i < walletsArray.length(); i++) {
                    JSONObject wallet = walletsArray.getJSONObject(i);
                    userWalletsArray.add(wallet.getString("wallet_id"));
                }
                progressDialog.dismiss();

            } catch (JSONException e) {
                e.printStackTrace();
                progressDialog.dismiss();
            }

            //bind array list to recycler view adapter
            ArrayAdapter<String> walletArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, userWalletsArray);
            walletArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            walletsSpinner.setAdapter(walletArrayAdapter);


        } else {

            StringRequest stringRequest = new StringRequest(Request.Method.POST, API_ENDPOINT.USER_WALLETS_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressDialog.dismiss();

                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (!jsonObject.getBoolean("error")) {

                                    //get user wallet and add to arrayList
                                    JSONArray walletsArray = jsonObject.getJSONArray("my_wallets");

                                    if (walletsArray.length() > 0) {
                                        for (int i = 0; i < walletsArray.length(); i++) {
                                            JSONObject wallet = walletsArray.getJSONObject(i);
                                            userWalletsArray.add(wallet.getString("wallet_id"));
                                        }
                                    } else {
                                        userWalletsArray.add("No Wallet");
                                        fundTransferButton.setEnabled(false);
                                    }
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            //bind array list to recycler view adapter
                            ArrayAdapter<String> walletArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, userWalletsArray);
                            walletArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            walletsSpinner.setAdapter(walletArrayAdapter);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressDialog.dismiss();
                            error.printStackTrace();
                            userWalletsArray.add("Select Wallet");
                            fundTransferButton.setEnabled(false);
                            //bind array list to recycler view adapter
                            ArrayAdapter<String> walletArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, userWalletsArray);
                            walletArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            walletsSpinner.setAdapter(walletArrayAdapter);

                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> map = new HashMap<>();
                    map.put("person_id", String.valueOf(SharedPrefManager.getClassinstance(getApplicationContext()).getUserId()));
                    return map;
                }
            };

            //requestQueue.add(stringRequest);
            SingletonAPI.getClassinstance(getApplicationContext()).addToRequest(stringRequest);
        }
    }

    /**
     * A function to show toast
     * @param msg message
     */
    private void showToast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    public void getUserAccounts() {
        //show progress dialog
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching Account(s)...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        //first try getting user data from sharedpreference
        if (SharedPrefManager.getClassinstance(this).getUserAccounts() != null) {
            String preferenceResponse = SharedPrefManager.getClassinstance(this).getUserAccounts();
            try {
                JSONArray accountsArray = new JSONArray(preferenceResponse);
                if(accountsArray.length() ==0){
                    fundTransferButton.setEnabled(false);
                }

                for (int i = 0; i < accountsArray.length(); i++) {
                    JSONObject account = accountsArray.getJSONObject(i);
                    userAccountsArray.add(account.getString("account_id"));
                }
                progressDialog.dismiss();

            } catch (JSONException e) {
                e.printStackTrace();
                progressDialog.dismiss();
            }

            //bind array list to recycler view adapter
            ArrayAdapter<String> accountArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, userAccountsArray);
            accountArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            walletsSpinner.setAdapter(accountArrayAdapter);


        } else {

            StringRequest stringRequest = new StringRequest(Request.Method.POST, API_ENDPOINT.USER_ACCOUNTS_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressDialog.dismiss();

                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (!jsonObject.getBoolean("error")) {

                                    //get user accounts and add to arrayList
                                    JSONArray accountsArray = jsonObject.getJSONArray("my_accounts");

                                    if (accountsArray.length() > 0) {
                                        for (int i = 0; i < accountsArray.length(); i++) {
                                            JSONObject account = accountsArray.getJSONObject(i);
                                            userAccountsArray.add(account.getString("account_id"));
                                        }
                                    } else {
                                        userAccountsArray.add("No Account");
                                        fundTransferButton.setEnabled(false);
                                    }
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            //bind array list to recycler view adapter
                            ArrayAdapter<String> accountArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, userAccountsArray);
                            accountArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            walletsSpinner.setAdapter(accountArrayAdapter);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressDialog.dismiss();
                            error.printStackTrace();
                            userAccountsArray.add("Select Account");
                            fundTransferButton.setEnabled(false);
                            //bind array list to recycler view adapter
                            ArrayAdapter<String> accountArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, userAccountsArray);
                            accountArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            walletsSpinner.setAdapter(accountArrayAdapter);

                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> map = new HashMap<>();
                    map.put("person_id", String.valueOf(SharedPrefManager.getClassinstance(getApplicationContext()).getUserId()));
                    return map;
                }
            };

            //requestQueue.add(stringRequest);
            SingletonAPI.getClassinstance(getApplicationContext()).addToRequest(stringRequest);
        }
    }

    public void callWalletToAccountAPI(final String wallet_id, final String amount, final String account_id, final String pin) {
        //first check for the internet connection
        boolean connectionStatus = isConnectedToInternet();
        //if user is not connected to the internet, check for user's preference
        if (!connectionStatus) {
            final AlertDialog.Builder alertBuilder;
            if (SharedPrefManager.getClassinstance(getApplicationContext()).getOfflineModePreference()) {

                //dispaly dialog to user
                alertBuilder = new AlertDialog.Builder(WalletToAccountActivity.this);
                alertBuilder.setTitle("We can help you");
                alertBuilder.setMessage("You are not connected to the internet but we can help you process your request offline");
                alertBuilder.setCancelable(false);
                alertBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertBuilder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        makeUSSDCall();
                    }
                });

                AlertDialog dialog = alertBuilder.create();
                dialog.show();
            } else {

                alertBuilder = new AlertDialog.Builder(WalletToAccountActivity.this);
                alertBuilder.setTitle("Enable offline mode");
                alertBuilder.setMessage("You can enable offline mode in the settings screen to continue your transaction");
                alertBuilder.setCancelable(false);
                alertBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertBuilder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(WalletToAccountActivity.this, GeneralSettingsActivity.class));
                    }
                });

                AlertDialog dialog = alertBuilder.create();
                dialog.show();
            }
        } else {
            //show progress dialog
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Processing Transaction...");
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, API_ENDPOINT.WALLET_ACCOUNT_TRANSFER_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressDialog.dismiss();

                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                showToast(jsonObject.getString("message"));

                                if (!jsonObject.getBoolean("error")) {
                                    startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                                    finish();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressDialog.dismiss();
                            error.printStackTrace();
                            showToast(error.getMessage());

                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> map = new HashMap<>();
                    map.put("person_id", String.valueOf(SharedPrefManager.getClassinstance(getApplicationContext()).getUserId()));
                    map.put("pin", pin);
                    map.put("amount", amount);
                    map.put("account_id", account_id);
                    map.put("wallet_id", wallet_id);

                    return map;
                }
            };

            //requestQueue.add(stringRequest);
            SingletonAPI.getClassinstance(getApplicationContext()).addToRequest(stringRequest);
        }
    }
    // Determine whether or not the user is connected to the internet
    private boolean isConnectedToInternet() {

        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnectedOrConnecting());
    }

    private void makeUSSDCall() {

        String ussdCode = "*718" + Uri.encode("#");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, USSD_CALL_REQUEST);
            return;
        }
        startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + ussdCode)));

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == USSD_CALL_REQUEST){
            makeUSSDCall();
        }
    }
}
