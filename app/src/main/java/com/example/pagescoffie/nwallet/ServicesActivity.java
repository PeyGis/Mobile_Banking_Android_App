package com.example.pagescoffie.nwallet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

public class ServicesActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);

        Toolbar toolbar;
        LinearLayout atmLocatorBtn;
        LinearLayout branchesBtn;
        LinearLayout savingsBtn;
        LinearLayout moreServicesBtn;

        //set action bar
        toolbar = (Toolbar)findViewById(R.id.action_bar);
        toolbar.setTitle(R.string.services_toolbar_title);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorBackground));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //init view components
        atmLocatorBtn = (LinearLayout)findViewById(R.id.atmLocator);
        branchesBtn = (LinearLayout)findViewById(R.id.branchLocator);
        savingsBtn = (LinearLayout)findViewById(R.id.savings);
        moreServicesBtn = (LinearLayout)findViewById(R.id.moreServices);

        //set click listeners to buttons
        atmLocatorBtn.setOnClickListener(this);
        branchesBtn.setOnClickListener(this);
        savingsBtn.setOnClickListener(this);
        moreServicesBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        switch (id){
            case R.id.atmLocator:
                startActivity(new Intent(ServicesActivity.this, ATMLocatorActivity.class));
                finish();
                break;
            case R.id.branchLocator:
                //
                break;
            case R.id.savings:
                //
                break;
            case R.id.moreServices:
                //
                break;
            default:
                //
                break;
        }
    }
}
