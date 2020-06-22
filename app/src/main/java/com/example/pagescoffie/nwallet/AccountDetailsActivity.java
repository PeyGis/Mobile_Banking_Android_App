package com.example.pagescoffie.nwallet;

import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.pagescoffie.nwallet.adapter.EmptyRecyclerViewAdapter;
import com.example.pagescoffie.nwallet.adapter.GenericRecyclerViewAdaper;
import com.example.pagescoffie.nwallet.adapter.GenericTransactionHistoryAdapter;
import com.example.pagescoffie.nwallet.model.API_ENDPOINT;
import com.example.pagescoffie.nwallet.model.MyAccountModel;
import com.example.pagescoffie.nwallet.model.SharedPrefManager;
import com.example.pagescoffie.nwallet.model.SingletonAPI;
import com.example.pagescoffie.nwallet.model.TransactionHistory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AccountDetailsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    String accountNumberExtra;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView recyclerView;
    private ArrayList<TransactionHistory> transactionHistoryArrayList;
    private RecyclerView.Adapter adapter;
    private GenericTransactionHistoryAdapter transactionHistoryAdapter;
    private SearchView searchView;
    private ConnectivityManager connectivityManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details);

        //set app toolbar
        Toolbar toolbar =(Toolbar) findViewById(R.id.action_bar);
        toolbar.setTitle(R.string.my_account_toolbar_title);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorBackground));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //get data from previous activity
        Bundle bundle = getIntent().getExtras();
        accountNumberExtra = bundle.getString("ACCOUNT_NUMBER");
        String accountBalanceExtra = bundle.getString("ACCOUNT_BALANCE");
        String accountTypeExtra = bundle.getString("ACCOUNT_TYPE");
        String typeExtra = bundle.getString("TYPE");

        //get view components
        TextView accountType = (TextView) findViewById(R.id.actType);
        TextView accountBalance = (TextView) findViewById(R.id.actBalance);
        TextView accountNumber = (TextView) findViewById(R.id.actNumber);
        searchView = (SearchView) findViewById(R.id.search_bar);
        searchView.setQueryHint("Filter transaction");

        //Append bundle data to text view
        accountType.setText(accountTypeExtra);
        accountBalance.append(accountBalanceExtra);

        if (typeExtra != null) {
            if(typeExtra.equals("ACCOUNT")){
                accountNumber.append(accountNumberExtra);
            } else{
                accountNumber.setText("Wallet ID: " + accountNumberExtra);

            }
        }

        //init array list
        transactionHistoryArrayList = new ArrayList<>();

        //get view components
        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.history_swipe_container);
        recyclerView = (RecyclerView) findViewById(R.id.history_recycler_view);

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
                getTransactionHistory();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                transactionHistoryAdapter.setFilter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                transactionHistoryAdapter.setFilter(newText);
                return false;
            }
        });
    }

    public void getTransactionHistory(){
        mSwipeRefreshLayout.setRefreshing(true);  //start swipe refresh loading dialog
        //get account number
        StringRequest stringRequest = new StringRequest(Request.Method.GET, API_ENDPOINT.TRANSACTION_HISTORY_URL + accountNumberExtra,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mSwipeRefreshLayout.setRefreshing(false);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if(!jsonObject.getBoolean("error")){

                                //get transaction history and add to arrayList
                                JSONArray historyArray = jsonObject.getJSONArray("history");

                                if( historyArray != null && historyArray.length() > 0) {
                                    for(int i=0; i < historyArray.length(); i++){
                                        JSONObject history = historyArray.getJSONObject(i);

                                        //create an object of transaction history Model first
                                        TransactionHistory accountModel = new TransactionHistory(
                                                history.getInt("transaction_id"),
                                                history.getString("details"),
                                                history.getString("debit_credit"),
                                                history.getString("date"),
                                                history.getDouble("amount"));
                                        //add to array list only when its not in list... this is done to prevent duplicates when user swipes to refressh
                                        if(!isInList(history.getInt("transaction_id"))){
                                            transactionHistoryArrayList.add(accountModel);
                                        }
                                    }
                                    //showToast(jsonObject.getString("message"));
                                } else{
                                    searchView.setVisibility(View.GONE);
                                    searchView.setInputType(InputType.TYPE_NULL);
                                    adapter = new EmptyRecyclerViewAdapter(jsonObject.getString("message"));
                                    recyclerView.setAdapter(adapter);
                                }
                            } else{
                                searchView.setVisibility(View.GONE);
                                searchView.setInputType(InputType.TYPE_NULL);
                                adapter = new EmptyRecyclerViewAdapter(jsonObject.getString("message"));
                                recyclerView.setAdapter(adapter);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //bind array list to recycler view adapter

                        transactionHistoryAdapter = new GenericTransactionHistoryAdapter(transactionHistoryArrayList, getApplicationContext());
                        recyclerView.setAdapter(transactionHistoryAdapter);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        error.printStackTrace();

                        searchView.setVisibility(View.GONE);
                        searchView.setInputType(InputType.TYPE_NULL);
                        adapter = new EmptyRecyclerViewAdapter("Error Occurred. Check interent and refresh");
                        recyclerView.setAdapter(adapter);

                    }
                });
//                }) {
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                Map<String, String> map = new HashMap<>();
//                map.put("person_id", String.valueOf(SharedPrefManager.getClassinstance(getApplicationContext()).getUserId()));
//                return map;
//            }
//        };

        //requestQueue.add(stringRequest);
        SingletonAPI.getClassinstance(getApplicationContext()).addToRequest(stringRequest);
    }


    @Override
    public void onRefresh() {
        getTransactionHistory();
    }

    public void showToast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }


    private boolean isInList(int key){
        for(TransactionHistory transactionHistory : transactionHistoryArrayList){
            if(transactionHistory.getTransactionId() == key){
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

    public boolean isConnectedToNetwork(){

        return true;
    }
}
