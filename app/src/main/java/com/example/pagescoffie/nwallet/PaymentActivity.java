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

public class PaymentActivity extends AppCompatActivity {

    String merchantNameExtra;
    private TextInputLayout merchantAccountNumberWrapper;
    private Spinner accountsSpinner;
    private TextInputLayout amountTxtInput;
    private Button payMerchantBtn;
    ArrayList<String> userAccountsArray;
    final Context context = this;
    private ConnectivityManager connectivityManager;
    public static final int USSD_CALL_REQUEST = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        //set app toolbar
        Toolbar toolbar =(Toolbar) findViewById(R.id.action_bar);
        toolbar.setTitle(R.string.payment_toolbar_title);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorBackground));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //get data from previous activity
        Bundle bundle = getIntent().getExtras();
        merchantNameExtra = bundle.getString("MERCHANT_NAME");
        String merchantId = bundle.getString("MERCHANT_ID");

        accountsSpinner = (Spinner) findViewById(R.id.myAccoountPaymentSpinner);
        TextView merchantNameTv = (TextView) findViewById(R.id.merchantNameTv);
        payMerchantBtn = (Button)findViewById(R.id.payMerchantBtn);
        merchantAccountNumberWrapper = (TextInputLayout) findViewById(R.id.merchantAccountNumberWrapper);
        amountTxtInput = (TextInputLayout) findViewById(R.id.transferAmountWrapper);
        merchantNameTv.setText(merchantNameExtra);

        //change spinner text color when selected
        accountsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

        //set listener to recharge button
        payMerchantBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String account = "";
                String amount = "";

                merchantAccountNumberWrapper.setError(null);
                amountTxtInput.setError(null);
                merchantAccountNumberWrapper.setHint(merchantNameExtra + " Account number");

                //get text from iedit texts
                if(merchantAccountNumberWrapper.getEditText() != null){
                    account = merchantAccountNumberWrapper.getEditText().getText().toString();
                }
                if(amountTxtInput.getEditText() != null){
                    //amountTxtInput.getEditText().setText("0");
                    amount = amountTxtInput.getEditText().getText().toString();
                }

                //validate phone
                if (account.length() < 10){
                    merchantAccountNumberWrapper.setError("Enter a valid account number");
                } else if (amount.isEmpty() || ( !amount.isEmpty() && Double.parseDouble(amount) <= 0.0)){
                    amountTxtInput.setError("Amount must be greather than GHS 0.0");
                } else {

                    merchantAccountNumberWrapper.setErrorEnabled(false);
                    amountTxtInput.setErrorEnabled(false);

                    //check if user selected an account
                    if(accountsSpinner.getSelectedItem().toString().equals("No Account") || accountsSpinner.getSelectedItem().toString().equals("Select Account")){

                    } else if(accountsSpinner.getSelectedItem().toString().equals(account)){
                        showToast("Cannot transfer to self account");
                    }
                    else {
                        //Only display confirm PIN dialog when user has set value to true in GeneralSettings Activity
                        if (SharedPrefManager.getClassinstance(getApplicationContext()).getConfirmPINPreference()) {
                            AlertDialog.Builder enterPinBuilder = new AlertDialog.Builder(context);
                            enterPinBuilder.setTitle("Confirm Transfer");
                            enterPinBuilder.setMessage("Enter PIN to confirm your transfer of GHS " + amount + " to " + account);

                            LinearLayout container = new LinearLayout(context);
                            container.setOrientation(LinearLayout.VERTICAL);
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
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
                                    if (user_PIN.length() >= 4)
                                        callPaymentAPI(accountsSpinner.getSelectedItem().toString(), finalAmount, finalAccount, user_PIN);
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
                        } else{
                            callPaymentAPI(accountsSpinner.getSelectedItem().toString(), amount, account, "7575");

                        }
                    }
                }

            }
        });

    }

    private void getUserAccounts() {
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
                        payMerchantBtn.setEnabled(false);
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
                accountsSpinner.setAdapter(accountArrayAdapter);


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
                                            payMerchantBtn.setEnabled(false);
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                //bind array list to recycler view adapter
                                ArrayAdapter<String> accountArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, userAccountsArray);
                                accountArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                accountsSpinner.setAdapter(accountArrayAdapter);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                progressDialog.dismiss();
                                error.printStackTrace();
                                userAccountsArray.add("Select Account");
                                payMerchantBtn.setEnabled(false);
                                //bind array list to recycler view adapter
                                ArrayAdapter<String> accountArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, userAccountsArray);
                                accountArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                accountsSpinner.setAdapter(accountArrayAdapter);

                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> map = new HashMap<>();
                        map.put("person_id", String.valueOf(SharedPrefManager.getClassinstance(getApplicationContext()).getUserId()));
                        return map;
                    }
                };

                SingletonAPI.getClassinstance(getApplicationContext()).addToRequest(stringRequest);
            }
        }

    public void callPaymentAPI(final String self_account, final String amount, final String other_account, final String pin) {

        //first check for the internet connection
        boolean connectionStatus = isConnectedToInternet();
        //if user is not connected to the internet, check for user's preference
        if (!connectionStatus) {
            final AlertDialog.Builder alertBuilder;
            if (SharedPrefManager.getClassinstance(getApplicationContext()).getOfflineModePreference()) {

                //dispaly dialog to user
                alertBuilder = new AlertDialog.Builder(PaymentActivity.this);
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

                alertBuilder = new AlertDialog.Builder(PaymentActivity.this);
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
                        startActivity(new Intent(PaymentActivity.this, GeneralSettingsActivity.class));
                    }
                });

                AlertDialog dialog = alertBuilder.create();
                dialog.show();
            }
        } else {
            //show progress dialog
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Processing Payment...");
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, API_ENDPOINT.PAY_MERCHANT_URL,
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
                    map.put("account_id", self_account);
                    map.put("other_account_id", other_account);
                    map.put("merchant", merchantNameExtra);

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

    // Determine whether or not the user is connected to the internet
    private boolean isConnectedToInternet() {

        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnectedOrConnecting());
    }

    private void makeUSSDCall() {

        String ussdCode = "*718*3" + Uri.encode("#");

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
