package com.example.pagescoffie.nwallet;

import android.graphics.PorterDuff;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

public class FundsTransferActivity extends AppCompatActivity {

    private Button sendTransfer;
    private Spinner spinnerAccounts;
    private Spinner channels;
    private TextInputLayout userEmail;
    private TextInputLayout userPin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_funds_transfer);

        //set app toolbar
        Toolbar toolbar =(Toolbar) findViewById(R.id.action_bar);
        toolbar.setTitle(R.string.funds_transfer_toolbar_title);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorBackground));
        //toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorBackground), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //init view components
        sendTransfer = (Button)findViewById(R.id.sendFundBtn);
        spinnerAccounts = (Spinner) findViewById(R.id.accountspinner);
        channels = (Spinner) findViewById(R.id.channelspinner);
        userEmail = (TextInputLayout) findViewById(R.id.emailWrapper);
        userPin = (TextInputLayout) findViewById(R.id.pinWrapper);


        //populate accounts dropdown with data
        List<String> useraccounts = new ArrayList<>();
        useraccounts.add("12345SDFDSFSDF");
        useraccounts.add("7895ASDDSFDSSD");
        useraccounts.add("4578ASD1245445");

        //populate accounts dropdown with data
        List<String> userchannels = new ArrayList<>();
        userchannels.add("Channel 1");
        userchannels.add("Channel 2");

        // Spinner Adapter
        ArrayAdapter<String> spinnerAccountsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, useraccounts);
        spinnerAccountsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAccounts.setAdapter(spinnerAccountsAdapter);

//        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this, R.array.user_accounts, android.R.layout.simple_spinner_item);
//        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        accounts.setAdapter(arrayAdapter);

        ArrayAdapter<CharSequence> arrayAdapter2 = ArrayAdapter.createFromResource(this, R.array.user_channels, android.R.layout.simple_spinner_item);
        arrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        channels.setAdapter(arrayAdapter2);

    }
}
