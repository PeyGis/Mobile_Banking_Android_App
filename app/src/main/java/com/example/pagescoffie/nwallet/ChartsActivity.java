package com.example.pagescoffie.nwallet;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ParseException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ChartsActivity extends AppCompatActivity{
    ArrayList<String> labels;
    PieDataSet pieDataSet;
    DatePickerDialog datePickerDialog;
    TextView startDateTv, endDateTv;
    ImageButton endDate, startDate;
    Spinner accountsSpinner;
    PieChart chart;
    Button updateChart;

    ArrayList<String> userAccountsArray;
    final Context context = this;
    String startDateValue = "";
    String endDateValue = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);

        //set app toolbar
        Toolbar toolbar =(Toolbar) findViewById(R.id.action_bar);
        toolbar.setTitle(R.string.chart_toolbar_title);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorBackground));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //get view component
        accountsSpinner = (Spinner) findViewById(R.id.myAccoountTransferSpinner);
         chart = (PieChart) findViewById(R.id.pie_chart);
        startDateTv = (TextView) findViewById(R.id.startDateTv);
        endDateTv = (TextView) findViewById(R.id.endDateTv);
        endDate = (ImageButton) findViewById(R.id.endDateImg);
        startDate = (ImageButton)findViewById(R.id.startDateImg);
        updateChart = (Button)findViewById(R.id.updateChart);

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
        //fetch chart report
        getUserAccounts();

        //set click listener to buuton
        updateChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(accountsSpinner.getSelectedItem().toString().equals("No Account") || accountsSpinner.getSelectedItem().toString().equals("Select Account")){
                    updateChart.setEnabled(false);
                }else{
                    //first check for date value
                    if(!startDateValue.isEmpty() && !endDateValue.isEmpty()){
                        if(compareDate()){
                            getChartDataAPI(accountsSpinner.getSelectedItem().toString(), startDateValue, endDateValue);
                        } else{
                            showToast("Invalid date range");
                        }
                    } else if (!startDateValue.isEmpty() && endDateValue.isEmpty()){
                        getChartDataAPI(accountsSpinner.getSelectedItem().toString(), startDateValue, endDateValue);
                    } else if(startDateValue.isEmpty() && !endDateValue.isEmpty()){
                        showToast("Please select start date");
                    }
                    else{
                        getChartDataAPI(accountsSpinner.getSelectedItem().toString(), startDateValue, endDateValue);
                    }
                }
            }
        });

        //onclick listener to start date button
        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year =calendar.get(Calendar.YEAR);

                datePickerDialog = new DatePickerDialog(ChartsActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                startDateTv.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                                startDateValue = year + "-" + (month + 1) + "-" + dayOfMonth;
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });

        //onclick listener to end date button
        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year =calendar.get(Calendar.YEAR);

                datePickerDialog = new DatePickerDialog(ChartsActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                endDateTv.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                                endDateValue = year + "-" + (month + 1) + "-" + dayOfMonth;
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });


        ArrayList<PieEntry> pieEntryArrayList = new ArrayList<>();
        pieEntryArrayList.add(new PieEntry(21f,"Report shows here"));


        pieDataSet = new PieDataSet(pieEntryArrayList, "");

        PieData data = new PieData(pieDataSet);
        data.setValueFormatter(new PercentFormatter());
        pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        pieDataSet.setSliceSpace(3);
        pieDataSet.setSelectionShift(5);
        chart.setData(data);
        chart.setUsePercentValues(true);
        chart.setHoleRadius(20);
        chart.setDrawHoleEnabled(true);
        chart.setContentDescription("Favorite Fruits");
        chart.setTransparentCircleRadius(10);
        chart.getDescription().setEnabled(false);

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
                    //
                } else{
                    for (int i = 0; i < accountsArray.length(); i++) {
                        JSONObject account = accountsArray.getJSONObject(i);
                        userAccountsArray.add(account.getString("account_id"));
                    }
                    progressDialog.dismiss();
                    //call display chart data
                    getChartDataAPI(userAccountsArray.get(0), "", "");
                }



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
                                        //call display chart api
                                        getChartDataAPI(userAccountsArray.get(0), "", "");


                                    } else {
                                        userAccountsArray.add("No Account");
                                        //fundTransferButton.setEnabled(false);
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
                            //fundTransferButton.setEnabled(false);
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

            //requestQueue.add(stringRequest);
            SingletonAPI.getClassinstance(getApplicationContext()).addToRequest(stringRequest);
        }
    }

    /**
     * A function to show toast
     * @param msg message
     */
    private void showToast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public void getChartDataAPI(final String account_id, final String from_date, final String end_date){

        //show progress dialog
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching Report...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, API_ENDPOINT.GENERIC_CHART_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean("error")) {

                                //get chart data and add to arrayList
                                JSONArray accountsArray = jsonObject.getJSONArray("chart_obj");

                                if (accountsArray.length() > 0) {

                                    ArrayList<PieEntry> pieEntryArrayList = new ArrayList<>();

                                    for (int i = 0; i < accountsArray.length(); i++) {
                                        JSONObject account = accountsArray.getJSONObject(i);
                                        pieEntryArrayList.add(new PieEntry(account.getInt("total"), account.getString("transaction")));
                                    }
                                        pieDataSet = new PieDataSet(pieEntryArrayList, "");

                                        PieData data = new PieData(pieDataSet);
                                        data.setValueFormatter(new PercentFormatter());
                                        pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                                        pieDataSet.setSliceSpace(3);
                                        pieDataSet.setSelectionShift(5);
                                        chart.setData(data);
                                        chart.setUsePercentValues(true);
                                        chart.setHoleRadius(20);
                                        chart.setDrawHoleEnabled(true);
                                        chart.getDescription().setEnabled(false);
                                        chart.setTransparentCircleRadius(10);
                                        chart.getDescription().setEnabled(false);
                                        chart.invalidate();

                                }
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
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("account_id", account_id);
                map.put("from_date", from_date);
                map.put("end_date", end_date);
                return map;
            }
        };

        //requestQueue.add(stringRequest);
        SingletonAPI.getClassinstance(getApplicationContext()).addToRequest(stringRequest);
    }

    public boolean compareDate(){
        Date sDate;
        Date eDate;

        // parse String datetime to Date
        try {
            sDate = new SimpleDateFormat("yyyy-MM-dd", Locale.UK ).parse(startDateValue);
            eDate = new SimpleDateFormat("yyyy-MM-dd", Locale.UK ).parse(endDateValue);
            if(sDate.after(eDate)){

                return false;
            } else{

                return true;
            }
        } catch (java.text.ParseException e) { e.printStackTrace(); }
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
    }
}
