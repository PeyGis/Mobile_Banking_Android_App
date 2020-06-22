package com.example.pagescoffie.nwallet;

import android.content.res.Configuration;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import com.example.pagescoffie.nwallet.model.SharedPrefManager;
import com.example.pagescoffie.nwallet.model.SingletonAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MyAccountActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView recyclerView;
    private ArrayList<MyAccountModel> accountModelArrayList;
    private RecyclerView.Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        //set app toolbar
        Toolbar toolbar =(Toolbar) findViewById(R.id.action_bar);
        toolbar.setTitle(R.string.my_account_toolbar_title);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorBackground));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //init array list
        accountModelArrayList = new ArrayList<>();

        //get view components
        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_container);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

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
                getUserAccounts();
            }
        });

    }

    public void getUserAccounts(){

        mSwipeRefreshLayout.setRefreshing(true);  //start swipe refresh loading dialog

        //first try getting user data from sharedpreference
        if (SharedPrefManager.getClassinstance(this).getUserAccounts() != null) {
            String preferenceResponse = SharedPrefManager.getClassinstance(this).getUserAccounts();
            try {
                JSONArray accountsArray = new JSONArray(preferenceResponse);
                if(accountsArray.length() > 0) {

                    for(int i=0; i < accountsArray.length(); i++){
                        JSONObject account = accountsArray.getJSONObject(i);

                        //create an object of MyAccount Model first
                        MyAccountModel accountModel = new MyAccountModel(
                                account.getString("account_id"),
                                account.getString("account_category"),
                                account.getDouble("account_balance"));
                        //add to array list only when its not in list... this is done to prevent duplicates when user swipes to refressh
                        if(!isInList(account.getString("account_id"))){
                            accountModelArrayList.add(accountModel);
                        }
                    }
                    mSwipeRefreshLayout.setRefreshing(false);
                    //bind array list to recycler view adapter
                    adapter = new GenericRecyclerViewAdaper(accountModelArrayList, null, getApplicationContext(), 1);
                    recyclerView.setAdapter(adapter);

                } else{
                    adapter = new EmptyRecyclerViewAdapter("You haven't synced any account yet");
                    recyclerView.setAdapter(adapter);
                    mSwipeRefreshLayout.setRefreshing(false);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        } else {

            StringRequest stringRequest = new StringRequest(Request.Method.POST, API_ENDPOINT.USER_ACCOUNTS_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            mSwipeRefreshLayout.setRefreshing(false);

                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (!jsonObject.getBoolean("error")) {

                                    //get user accounts and add to arrayList
                                    JSONArray accountsArray = jsonObject.getJSONArray("my_accounts");

                                    if (accountsArray.length() > 0) {
                                        //save accounts to sharedpreference
                                        SharedPrefManager.getClassinstance(getApplicationContext()).saveUserAccounts(accountsArray.toString());

                                        for (int i = 0; i < accountsArray.length(); i++) {
                                            JSONObject account = accountsArray.getJSONObject(i);

                                            //create an object of MyAccount Model first
                                            MyAccountModel accountModel = new MyAccountModel(
                                                    account.getString("account_id"),
                                                    account.getString("account_category"),
                                                    account.getDouble("account_balance"));
                                            //add to array list only when its not in list... this is done to prevent duplicates when user swipes to refressh
                                            if (!isInList(account.getString("account_id"))) {
                                                accountModelArrayList.add(accountModel);
                                            }
                                        }
                                        showToast(jsonObject.getString("message"));
                                    } else {
                                        adapter = new EmptyRecyclerViewAdapter("You haven't synced any account yet");
                                        recyclerView.setAdapter(adapter);
                                    }

                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            //bind array list to recycler view adapter
                            adapter = new GenericRecyclerViewAdaper(accountModelArrayList, null, getApplicationContext(), 1);
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

            SingletonAPI.getClassinstance(getApplicationContext()).addToRequest(stringRequest);
        }
    }

    @Override
    public void onRefresh() {
        getUserAccounts();
    }

    public void showToast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private boolean isInList(String key){

        for(MyAccountModel userAccountModel : accountModelArrayList){
            if(userAccountModel.getAccountNumber().equals(key)){
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
