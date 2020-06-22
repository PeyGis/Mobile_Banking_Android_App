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
import java.util.List;
import java.util.Map;

public class AccountToWalletActivity extends AppCompatActivity {

    private Spinner myAccountsSpinner;
    private Spinner networkProviderSpinner;
    private TextInputLayout transferWalletNumberTxtInput;
    private TextInputLayout amountTxtInput;
    private Button fundTransferButton;
    private ConnectivityManager connectivityManager;
    public static final int USSD_CALL_REQUEST = 10;
    ArrayList<String> userAccountsArray;
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_to_wallet);

        //set app toolbar
        Toolbar toolbar =(Toolbar) findViewById(R.id.action_bar);
        toolbar.setTitle(R.string.funds_transfer_toolbar_title);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorBackground));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //get view component
        myAccountsSpinner = (Spinner) findViewById(R.id.myAccountTransferSpinner);
        networkProviderSpinner = (Spinner) findViewById(R.id.networkProviderSpinner);
        transferWalletNumberTxtInput = (TextInputLayout) findViewById(R.id.transferWalletNumberWrapper);
        amountTxtInput = (TextInputLayout) findViewById(R.id.transferAmountWrapper);
        fundTransferButton = (Button) findViewById(R.id.sendTransferBtn);

        transferWalletNumberTxtInput.getEditText().setText(SharedPrefManager.getClassinstance(getApplicationContext()).getUserPhone());

        //change spinner text color when selected
        myAccountsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView)view).setTextColor(Color.BLACK);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //init array for user accounts spinner
        userAccountsArray = new ArrayList<>();
        //fetch user accounts
       getUserAccounts();

        //populate network dropdown with data
        List<String> networks = new ArrayList<>();
        networks.add("MTN MOBILE MONEY");
        networks.add("VODAFONE CASH");
        networks.add("AIRTEL MONEY");
        networks.add("TIGO CASH");
        networks.add("OTHER");

        ArrayAdapter<String> networkArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, networks);
        networkArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        networkProviderSpinner.setAdapter(networkArrayAdapter);


        //set listener to recharge button
        fundTransferButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String walletNumber = "";
                String amount = "";

                //get text from iedit texts
                transferWalletNumberTxtInput.setError(null);
                amountTxtInput.setError(null);

                if(transferWalletNumberTxtInput.getEditText() != null){
                    walletNumber = transferWalletNumberTxtInput.getEditText().getText().toString();
                }
                if(amountTxtInput.getEditText() != null){
                    //amountTxtInput.getEditText().setText("0");
                    amount = amountTxtInput.getEditText().getText().toString();
                }

                //validate phone
                if (walletNumber.length() < 10){
                    transferWalletNumberTxtInput.setError("Enter a valid phone number");
                } else if ( amount.isEmpty() || ( !amount.isEmpty() && Double.parseDouble(amount) <= 0.0)){
                    amountTxtInput.setError("Amount must be greather than GHS 0.0");
                } else {

                    transferWalletNumberTxtInput.setErrorEnabled(false);
                    amountTxtInput.setErrorEnabled(false);

                    //check if user selected an account
                    if(myAccountsSpinner.getSelectedItem().toString().equals("No Account") || myAccountsSpinner.getSelectedItem().toString().equals("Select Account")){

                    } else if(myAccountsSpinner.getSelectedItem().toString().equals(walletNumber)){
                        showToast("Cannot transfer to self account");
                    }
                    else{
                        AlertDialog.Builder enterPinBuilder = new AlertDialog.Builder(context);
                        enterPinBuilder.setTitle("Confirm Transfer");
                        enterPinBuilder.setMessage("Enter PIN to confirm your transfer of GHS " + amount + " to " +walletNumber);

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
                        final String finalWallet = walletNumber;
                        enterPinBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String user_PIN = pin_input.getText().toString();
                                if(user_PIN.length() >=4)
                                    callAccountToWalletAPI(myAccountsSpinner.getSelectedItem().toString(), finalAmount, finalWallet, user_PIN);
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
           myAccountsSpinner.setAdapter(accountArrayAdapter);


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
                            myAccountsSpinner.setAdapter(accountArrayAdapter);
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
                            myAccountsSpinner.setAdapter(accountArrayAdapter);

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

    public void callAccountToWalletAPI(final String account, final String amount, final String wallet, final String pin) {

        //first check for the internet connection
        boolean connectionStatus = isConnectedToInternet();
        //if user is not connected to the internet, check for user's preference
        if (!connectionStatus) {
            final AlertDialog.Builder alertBuilder;
            if (SharedPrefManager.getClassinstance(getApplicationContext()).getOfflineModePreference()) {

                //dispaly dialog to user
                alertBuilder = new AlertDialog.Builder(AccountToWalletActivity.this);
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

                alertBuilder = new AlertDialog.Builder(AccountToWalletActivity.this);
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
                        startActivity(new Intent(AccountToWalletActivity.this, GeneralSettingsActivity.class));
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

            StringRequest stringRequest = new StringRequest(Request.Method.POST, API_ENDPOINT.ACCOUNT_WALLET_TRANSFER_URL,
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
                    map.put("account_id", account);
                    map.put("wallet_id", wallet);

                    return map;
                }
            };
            //add request to vollet request queue
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
