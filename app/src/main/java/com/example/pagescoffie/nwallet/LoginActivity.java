package com.example.pagescoffie.nwallet;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private LinearLayout signUpLink;
    private Button loginBtn;
    private TextInputLayout phoneTxtInput;
    private TextInputLayout pinTxtInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //check is session is still valid .. if true redirect user to dashboard
        if(SharedPrefManager.getClassinstance(LoginActivity.this).isLoggedIn()){
            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
            finish();
        }

        loginBtn = (Button) findViewById(R.id.loginBtn);
        signUpLink = (LinearLayout)findViewById(R.id.signupLink);
        phoneTxtInput = (TextInputLayout) findViewById(R.id.loginPhoneNumberWrapper);
        pinTxtInput = (TextInputLayout) findViewById(R.id.loginPasswordWrapper);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone="", pin = "";

                //clear error messages
                phoneTxtInput.setError(null);
                pinTxtInput.setError(null);

                //get edit text input data
                if(phoneTxtInput.getEditText() != null){
                    phone = phoneTxtInput.getEditText().getText().toString();
                }
                if(pinTxtInput.getEditText() != null){
                    pin = pinTxtInput.getEditText().getText().toString();
                }

                //validate data
                if(phone.length() < 10){
                    phoneTxtInput.setError("Invalid phone number");
                } else if (pin.length() != 4){
                    pinTxtInput.setError("PIN must be 4 digits long");
                } else{

                    phoneTxtInput.setErrorEnabled(false);
                    pinTxtInput.setErrorEnabled(false);

                    //call login API
                    callLoginAPI(phone, pin);
                }
            }
        });

        signUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToHome = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(goToHome);
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

    private void callLoginAPI(final String phone, final String pin){

        //show progress dialog
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Signing in...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        //prepare string request to make API call
        StringRequest stringRequest = new StringRequest(Request.Method.POST, API_ENDPOINT.LOGIN_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();

                try{
                    JSONObject jsonObject = new JSONObject(response);  //parse response to JSON object

                    //open dashboard activity once login is successful
                    if(!jsonObject.getBoolean("error")){
                        showToast(jsonObject.getString("message"));

                        //save user details
                        if(SharedPrefManager.getClassinstance(LoginActivity.this).saveUserDetails(
                                jsonObject.getInt("user_id"),
                                jsonObject.getString("fname"),
                                jsonObject.getString("email"),
                                jsonObject.getString("phone")
                        ));

                        Intent goToLogin = new Intent(LoginActivity.this, DashboardActivity.class);
                        startActivity(goToLogin);
                        finish();
                    }
                    showToast(jsonObject.getString("message"));

                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        },
                //on error response, show toast
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        error.printStackTrace();
                        showToast("Error occured. Check internet connection");
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> stringRequestMap = new HashMap<>();
                stringRequestMap.put("phone", phone);
                stringRequestMap.put("pin", pin);

                return stringRequestMap;
            }
        };
        //add request to generic queue
        SingletonAPI.getClassinstance(getApplicationContext()).addToRequest(stringRequest);

    }
}
