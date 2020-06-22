package com.example.pagescoffie.nwallet;

import android.content.res.Configuration;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.pagescoffie.nwallet.adapter.EmptyRecyclerViewAdapter;
import com.example.pagescoffie.nwallet.adapter.GenericRecyclerViewAdaper;
import com.example.pagescoffie.nwallet.adapter.MerchantAdapter;
import com.example.pagescoffie.nwallet.model.API_ENDPOINT;
import com.example.pagescoffie.nwallet.model.MerchantModel;
import com.example.pagescoffie.nwallet.model.MyAccountModel;
import com.example.pagescoffie.nwallet.model.SharedPrefManager;
import com.example.pagescoffie.nwallet.model.SingletonAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SelectMerchantActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView recyclerView;
    private ArrayList<MerchantModel> merchantModelArrayList;
    private RecyclerView.Adapter adapter;
    private MerchantAdapter merchantAdapter;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_merchant);

        //set app toolbar
        Toolbar toolbar =(Toolbar) findViewById(R.id.action_bar);
        toolbar.setTitle(R.string.merchants_toolbar_title);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorBackground));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //init array list
        merchantModelArrayList = new ArrayList<>();

        //get view components
        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.merchant_swipe_container);
        recyclerView = (RecyclerView) findViewById(R.id.merchant_recycler_view);
        searchView = (SearchView) findViewById(R.id.search_bar);
        searchView.setQueryHint("search merchant");

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
                getMerchants();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                merchantAdapter.setFilter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                merchantAdapter.setFilter(newText);
                return false;
            }
        });
    }

    public void getMerchants(){
        mSwipeRefreshLayout.setRefreshing(true);  //start swipe refresh loading dialog

        //first try getting merchants from sharedpreference
        if (SharedPrefManager.getClassinstance(this).getMerchants() != null) {
            String preferenceResponse = SharedPrefManager.getClassinstance(this).getMerchants();
            try {
                JSONArray merchantsArray = new JSONArray(preferenceResponse);
                if(merchantsArray.length() > 0) {

                    for(int i=0; i < merchantsArray.length(); i++){
                        JSONObject merchant = merchantsArray.getJSONObject(i);

                        //create an object of MyAccount Model first
                        MerchantModel merchantModel = new MerchantModel(
                                merchant.getInt("merchant_id"),
                                merchant.getString("merchant_name"));
                        //add to array list only when its not in list... this is done to prevent duplicates when user swipes to refressh
                        if(!isInList(merchant.getInt("merchant_id"))){
                            merchantModelArrayList.add(merchantModel);
                        }
                    }
                    mSwipeRefreshLayout.setRefreshing(false);
                    //bind array list to recycler view adapter
                    merchantAdapter = new MerchantAdapter(merchantModelArrayList, getApplicationContext());
                    recyclerView.setAdapter(merchantAdapter);

                } else{
                    searchView.setVisibility(View.GONE);
                    searchView.setInputType(InputType.TYPE_NULL);
                    adapter = new EmptyRecyclerViewAdapter("No merchant available");
                    recyclerView.setAdapter(adapter);
                    mSwipeRefreshLayout.setRefreshing(false);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        } else {

            StringRequest stringRequest = new StringRequest(Request.Method.GET, API_ENDPOINT.MERCHANTS_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            mSwipeRefreshLayout.setRefreshing(false);

                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (!jsonObject.getBoolean("error")) {

                                    //get user accounts and add to arrayList
                                    JSONArray merchantsArray = jsonObject.getJSONArray("merchants");

                                    if (merchantsArray.length() > 0) {
                                        //save merchants to sharedpreference
                                        SharedPrefManager.getClassinstance(getApplicationContext()).saveMerchants(merchantsArray.toString());

                                        for(int i=0; i < merchantsArray.length(); i++){
                                            JSONObject merchant = merchantsArray.getJSONObject(i);

                                            //create an object of MyAccount Model first
                                            MerchantModel merchantModel = new MerchantModel(
                                                    merchant.getInt("merchant_id"),
                                                    merchant.getString("merchant_name"));
                                            //add to array list only when its not in list... this is done to prevent duplicates when user swipes to refressh
                                            if(!isInList(merchant.getInt("merchant_id"))){
                                                merchantModelArrayList.add(merchantModel);
                                            }
                                        }
                                        showToast(jsonObject.getString("message"));
                                    } else {
                                        searchView.setVisibility(View.GONE);
                                        searchView.setInputType(InputType.TYPE_NULL);
                                        adapter = new EmptyRecyclerViewAdapter("No Merchant Available");
                                        recyclerView.setAdapter(adapter);
                                    }

                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            //bind array list to recycler view adapter
                            merchantAdapter = new MerchantAdapter(merchantModelArrayList, getApplicationContext());
                            recyclerView.setAdapter(merchantAdapter);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            mSwipeRefreshLayout.setRefreshing(false);
                            error.printStackTrace();
                            searchView.setVisibility(View.GONE);
                            searchView.setInputType(InputType.TYPE_NULL);
                            adapter = new EmptyRecyclerViewAdapter("Error Occurred. Check interent and pull to refresh");
                            recyclerView.setAdapter(adapter);

                        }
                    });

            //requestQueue.add(stringRequest);
            SingletonAPI.getClassinstance(getApplicationContext()).addToRequest(stringRequest);
        }

    }
    @Override
    public void onRefresh() {
getMerchants();
    }

    public void showToast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private boolean isInList(int key){

        for(MerchantModel merchantModel : merchantModelArrayList){
            if(merchantModel.getMerchantId() == key){
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
