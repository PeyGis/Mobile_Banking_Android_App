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
import com.example.pagescoffie.nwallet.model.SingletonAPI;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private Button signUpBtn;
    private LinearLayout loginLink;
    private TextInputLayout firstNameTxtInput;
    private TextInputLayout lastNameTxtInput;
    private TextInputLayout phoneTxtInput;
    private TextInputLayout emailTxtInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);


        // get view components by id
        signUpBtn = (Button) findViewById(R.id.signupBtn);
        loginLink = (LinearLayout) findViewById(R.id.loginLink);
        firstNameTxtInput = (TextInputLayout) findViewById(R.id.firstNameWrapper);
        lastNameTxtInput = (TextInputLayout) findViewById(R.id.lastNameWrapper);
        phoneTxtInput = (TextInputLayout) findViewById(R.id.phoneNumberWrapper);
        emailTxtInput = (TextInputLayout) findViewById(R.id.emailWrapper);

        //sign up button get data from edit texts
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String fname = "", lname="", phone="", email = "";

                //clear error messages
                firstNameTxtInput.setError(null);
                lastNameTxtInput.setError(null);
                phoneTxtInput.setError(null);
                emailTxtInput.setError(null);

                //get edit text data
                if(firstNameTxtInput.getEditText() != null){
                    fname = firstNameTxtInput.getEditText().getText().toString();
                }
                if(lastNameTxtInput.getEditText() != null){
                    lname = lastNameTxtInput.getEditText().getText().toString();
                }
                if(phoneTxtInput.getEditText() != null){
                    phone = phoneTxtInput.getEditText().getText().toString();
                }
                if(emailTxtInput.getEditText() != null){
                    email = emailTxtInput.getEditText().getText().toString();
                }

                //validate data
                if(fname.length() < 4){
                    firstNameTxtInput.setError("Name must be at least 4 chars long");
                } else if (lname.length() < 4){
                    lastNameTxtInput.setError("Name must be at least 4 chars long");
                } else if (phone.length() < 10){
                    phoneTxtInput.setError("Invalid phone number");
                } else if (! email.contains("@")){
                    emailTxtInput.setError("Invalid email address");
                } else{

                    firstNameTxtInput.setErrorEnabled(false);
                    lastNameTxtInput.setErrorEnabled(false);
                    phoneTxtInput.setErrorEnabled(false);
                    emailTxtInput.setErrorEnabled(false);

                    callSignupAPI(fname, lname, phone, email);
                }
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToLogin = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(goToLogin);
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

    /**
     * A function to call sign up API
     * @param fname user first name
     * @param lname user last name
     * @param phone user phone number
     * @param email user password
     */
    private void callSignupAPI(final String fname, final String lname, final String phone, final String email){

        //show progress dialog
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Signing up...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        //prepare string request to make API call
        StringRequest stringRequest = new StringRequest(Request.Method.POST, API_ENDPOINT.REGISTER_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();

                try{
                    JSONObject jsonObject = new JSONObject(response);  //parse response to JSON object

                    //open login activity once registration is successful
                    if(!jsonObject.getBoolean("error")){
                        showToast(jsonObject.getString("message"));
                        Intent goToLogin = new Intent(SignupActivity.this, LoginActivity.class);
                        startActivity(goToLogin);
                        finish();
                    }
                    showToast(jsonObject.getString("message"));

                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        error.printStackTrace();
                        showToast("Error occured Please check internet connection");
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> stringRequestMap = new HashMap<>();

                stringRequestMap.put("first_name", fname);
                stringRequestMap.put("last_name", lname);
                stringRequestMap.put("phone", phone);
                stringRequestMap.put("email", email);

                return stringRequestMap;
            }
        };
        //add request to generic queue
        SingletonAPI.getClassinstance(getApplicationContext()).addToRequest(stringRequest);

    }
}
