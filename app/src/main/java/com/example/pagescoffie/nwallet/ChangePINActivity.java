package com.example.pagescoffie.nwallet;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
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

public class ChangePINActivity extends AppCompatActivity {

    private TextInputLayout oldPINWrapper;
    private TextInputLayout newPINWrapper;
    private TextInputLayout confirmPINWrapper;
    private Button savePinBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pin);

        //set action bar
       Toolbar toolbar = (Toolbar)findViewById(R.id.action_bar);
        toolbar.setTitle(R.string.changePIN_toolbar_title);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorBackground));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //reference view components
        oldPINWrapper = (TextInputLayout) findViewById(R.id.oldPinWrapper);
        newPINWrapper = (TextInputLayout) findViewById(R.id.newPinWrapper);
        confirmPINWrapper = (TextInputLayout) findViewById(R.id.confirmPinWrapper);
        savePinBtn = (Button) findViewById(R.id.savePINBtn);

        //set onclick listener to button
        savePinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String old_pin = "";
                String new_pin = "";
                String confirm_pin = "";

                //set error messages to textinput layour
                oldPINWrapper.setError(null);
                newPINWrapper.setError(null);
                confirmPINWrapper.setError(null);

                //get edit text in textinput layouts
                if(oldPINWrapper.getEditText() != null){
                    old_pin = oldPINWrapper.getEditText().getText().toString();
                }
                if(newPINWrapper.getEditText() != null){
                    new_pin = newPINWrapper.getEditText().getText().toString();
                }
                if(confirmPINWrapper.getEditText() != null){
                    confirm_pin = confirmPINWrapper.getEditText().getText().toString();
                }

                //validate input data
                if (old_pin.length() <4){
                    oldPINWrapper.setError("PIN must be 4 digits");
                } else if(new_pin.length() <4){
                    newPINWrapper.setError("PIN must be 4 digits");
                } else if(confirm_pin.length() <4 || !confirm_pin.equals(new_pin)){
                    confirmPINWrapper.setError("PIN does not match");
                } else{
                    oldPINWrapper.setEnabled(false);
                    newPINWrapper.setErrorEnabled(false);
                    confirmPINWrapper.setErrorEnabled(false);

                    //call change pin API
                    changePIN(old_pin, new_pin);
                }
            }
        });
    }

    private void changePIN(final String old_pin, final String new_pin) {
        //show progress dialog
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Processing your request...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        //prepare string request to make API call
        StringRequest stringRequest = new StringRequest(Request.Method.POST, API_ENDPOINT.CHANGE_PIN_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();

                try{
                    JSONObject jsonObject = new JSONObject(response);  //parse response to JSON object

                    //open login activity once registration is successful
                    if(!jsonObject.getBoolean("error")){
                        showToast(jsonObject.getString("message"));
                        Intent goToLogin = new Intent(ChangePINActivity.this, DashboardActivity.class);
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

                stringRequestMap.put("person_id", String.valueOf(SharedPrefManager.getClassinstance(getApplicationContext()).getUserId()));
                stringRequestMap.put("old_pin", old_pin);
                stringRequestMap.put("new_pin", new_pin);

                return stringRequestMap;
            }
        };
        //add request to generic queue
        SingletonAPI.getClassinstance(getApplicationContext()).addToRequest(stringRequest);

    }

    /**
     * A function to show toast
     * @param msg message
     */
    private void showToast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
