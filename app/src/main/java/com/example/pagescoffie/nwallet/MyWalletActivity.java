package com.example.pagescoffie.nwallet;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.pagescoffie.nwallet.adapter.EmptyRecyclerViewAdapter;
import com.example.pagescoffie.nwallet.adapter.GenericRecyclerViewAdaper;
import com.example.pagescoffie.nwallet.model.API_ENDPOINT;
import com.example.pagescoffie.nwallet.model.MyAccountModel;
import com.example.pagescoffie.nwallet.model.MyWalletModel;
import com.example.pagescoffie.nwallet.model.SharedPrefManager;
import com.example.pagescoffie.nwallet.model.SingletonAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyWalletActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    private FloatingActionButton fab;
    final Context context = this;
    AlertDialog addWalletAlertDialog;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView recyclerView;
    private ArrayList<MyWalletModel> walletModelArrayList;
    private RecyclerView.Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_wallet);

        //set app toolbar
        Toolbar toolbar =(Toolbar) findViewById(R.id.action_bar);
        toolbar.setTitle(R.string.my_wallet_toolbar_title);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorBackground));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //init array list
        walletModelArrayList = new ArrayList<>();

        //get view components
        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.wallet_swipe_container);
        recyclerView = (RecyclerView) findViewById(R.id.wallet_recycler_view);

        //prepare recycler view settings
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        //prepare swipe to refresh settings
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        mSwipeRefreshLayout.post(new Runnable() {

            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                //fetch clients accounts data from server
                getUserWallets();
            }
        });

        //set floating action button
        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //on fab click, inflate the add wallet dialog created in the xml file
                LayoutInflater addWalletInflater = LayoutInflater.from(context);
                View walletView = addWalletInflater.inflate(R.layout.add_wallet_dailog, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(walletView);

                //get View components
                final Spinner networkSpinner = (Spinner) walletView.findViewById(R.id.networkspinner);
                final TextInputLayout momoNumberWrapper = (TextInputLayout) walletView.findViewById(R.id.momoNumberWrapper);
                Button addWalletBtn = (Button) walletView.findViewById(R.id.addWalletBtn);

                //populate network dropdown with data
                List<String> networks = new ArrayList<>();
                networks.add("MTN MOBILE MONEY");
                networks.add("VODAFONE CASH");
                networks.add("AIRTEL MONEY");
                networks.add("TIGO CASH");
                networks.add("OTHER");

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, networks);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                networkSpinner.setAdapter(arrayAdapter);

                //set onclick action to button
                addWalletBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String momoNumber = "";
                        String networkOperator = "";
                        boolean canSend = true;

                        //get momo number from edit text
                        if(momoNumberWrapper.getEditText() != null){
                            momoNumber = momoNumberWrapper.getEditText().getText().toString();

                            if(momoNumber.length() < 10){
                                momoNumberWrapper.setError("Invalid number");
                                canSend = false;
                            }
                        } else{
                            canSend = false;
                        }
                        //make sure user selects network
                        if(networkSpinner.getSelectedItem().toString().equals("")){
                            canSend = false;
                        } else{
                            networkOperator = networkSpinner.getSelectedItem().toString();
                        }

                        if(canSend){
                            momoNumberWrapper.setErrorEnabled(false);
                            callAddWalletAPI(momoNumber, networkOperator);
                        }
                    }
                });
                // create and show add wallet alert dialog
                 addWalletAlertDialog = builder.create();
                addWalletAlertDialog.show();
            }
        });

    }

    public void getUserWallets(){
        mSwipeRefreshLayout.setRefreshing(true);  //start swipe refresh loading dialog
        //first try getting user data from sharedpreference
        if (SharedPrefManager.getClassinstance(this).getUserWallets() != null) {
            String preferenceResponse = SharedPrefManager.getClassinstance(this).getUserWallets();
            try {
                JSONArray walletsArray = new JSONArray(preferenceResponse);
                if (walletsArray.length() > 0) {

                    for (int i = 0; i < walletsArray.length(); i++) {
                        JSONObject wallet = walletsArray.getJSONObject(i);

                        //create an object of MyAccount Model first
                        MyWalletModel walletModel = new MyWalletModel(
                                wallet.getString("wallet_id"),
                                wallet.getDouble("wallet_balance"),
                                wallet.getString("network"));
                        //add to array list only when its not in list... this is done to prevent duplicates when user swipes to refressh
                        if (!isInList(wallet.getString("wallet_id"))) {
                            walletModelArrayList.add(walletModel);
                        }
                    }
                    mSwipeRefreshLayout.setRefreshing(false);
                    adapter = new GenericRecyclerViewAdaper(null, walletModelArrayList, getApplicationContext(), 2);
                    recyclerView.setAdapter(adapter);

                } else {
                    adapter = new EmptyRecyclerViewAdapter("You haven't synced any wallet yet");
                    recyclerView.setAdapter(adapter);
                    mSwipeRefreshLayout.setRefreshing(false);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }

        else{
            StringRequest stringRequest = new StringRequest(Request.Method.POST, API_ENDPOINT.USER_WALLETS_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            mSwipeRefreshLayout.setRefreshing(false);

                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (!jsonObject.getBoolean("error")) {

                                    //get user accounts and add to arrayList
                                    JSONArray walletsArray = jsonObject.getJSONArray("my_wallets");

                                    if (walletsArray.length() > 0) {
                                        //save wallet to sharedpreference
                                        SharedPrefManager.getClassinstance(getApplicationContext()).saveUserWallets(walletsArray.toString());

                                        for (int i = 0; i < walletsArray.length(); i++) {
                                            JSONObject wallet = walletsArray.getJSONObject(i);

                                            //create an object of MyAccount Model first
                                            MyWalletModel walletModel = new MyWalletModel(
                                                    wallet.getString("wallet_id"),
                                                    wallet.getDouble("wallet_balance"),
                                                    wallet.getString("network"));
                                            //add to array list only when its not in list... this is done to prevent duplicates when user swipes to refressh
                                            if (!isInList(wallet.getString("wallet_id"))) {
                                                walletModelArrayList.add(walletModel);
                                            }
                                        }
                                        showToast(jsonObject.getString("message"));
                                    } else {
                                        adapter = new EmptyRecyclerViewAdapter("You haven't synced any wallet yet");
                                        recyclerView.setAdapter(adapter);
                                    }

                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            //bind array list to recycler view adapter
//                        adapter = new MerchantAdapter(accountModelArrayList, getApplicationContext());
//                        recyclerView.setAdapter(adapter);
                            adapter = new GenericRecyclerViewAdaper(null, walletModelArrayList, getApplicationContext(), 2);
                            recyclerView.setAdapter(adapter);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            mSwipeRefreshLayout.setRefreshing(false);
                            error.printStackTrace();
                            adapter = new EmptyRecyclerViewAdapter("Error Occurred. Check interent and refresh");
                            recyclerView.setAdapter(adapter);

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
     * A method that calls the add to wallet API
     * @param momoNumber phone number
     * @param networkOperator mobile network operator
     */
    private void callAddWalletAPI(final String momoNumber, final String networkOperator) {
        //show progress dialog
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding Wallet...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        //prepare string request to make API call
        StringRequest stringRequest = new StringRequest(Request.Method.POST, API_ENDPOINT.ADD_WALLET_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();

                try{
                    JSONObject jsonObject = new JSONObject(response);  //parse response to JSON object

                    //open login activity once registration is successful
                    if(!jsonObject.getBoolean("error")){
                        addWalletAlertDialog.dismiss();
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

                stringRequestMap.put("phone", momoNumber);
                stringRequestMap.put("network", networkOperator);
                stringRequestMap.put("person_id", String.valueOf(SharedPrefManager.getClassinstance(getApplicationContext()).getUserId()));
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

    @Override
    public void onRefresh() {
        getUserWallets();

    }

    private boolean isInList(String key){

        for(MyWalletModel walletModel : walletModelArrayList){
            if(walletModel.getWalletId().equals(key)){
                return true;
            }
        }
        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
    }
}
