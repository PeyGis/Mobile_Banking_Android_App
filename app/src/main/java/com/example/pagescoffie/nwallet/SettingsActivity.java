package com.example.pagescoffie.nwallet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar;
        LinearLayout changePIN;
        LinearLayout changeContact;
        LinearLayout genSettings;
        LinearLayout goBack;

        //set action bar
        toolbar = (Toolbar)findViewById(R.id.action_bar);
        toolbar.setTitle(R.string.settings_toolbar_title);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorBackground));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //reference view components
        changePIN = (LinearLayout)findViewById(R.id.changePin);
        changeContact = (LinearLayout)findViewById(R.id.changeContact);
        genSettings = (LinearLayout)findViewById(R.id.genSettings);
        goBack = (LinearLayout)findViewById(R.id.goBack);

        //set onclick listeners to button
        changePIN.setOnClickListener(this);
        changeContact.setOnClickListener(this);
        genSettings.setOnClickListener(this);
        goBack.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.changePin:
                startActivity(new Intent(SettingsActivity.this, ChangePINActivity.class));

                break;
            case R.id.changeContact:
                startActivity(new Intent(SettingsActivity.this, ChangePhoneActivity.class));

                break;
            case R.id.genSettings:
                startActivity(new Intent(SettingsActivity.this, GeneralSettingsActivity.class));

                break;
            case R.id.goBack:
                startActivity(new Intent(SettingsActivity.this, DashboardActivity.class));
                finish();
                //
                break;
            default:
                //
                break;
        }
    }
}
