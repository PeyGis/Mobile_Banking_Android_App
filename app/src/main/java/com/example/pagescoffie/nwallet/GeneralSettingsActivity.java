package com.example.pagescoffie.nwallet;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.example.pagescoffie.nwallet.model.SharedPrefManager;

public class GeneralSettingsActivity extends AppCompatActivity implements SwitchCompat.OnCheckedChangeListener{

    SwitchCompat confirmPinSwtich, offlineModeSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_settings);

        //set action bar
        Toolbar toolbar = (Toolbar)findViewById(R.id.action_bar);
        toolbar.setTitle(R.string.settings_toolbar_title);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorBackground));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //get reference to view widgets
        confirmPinSwtich = (SwitchCompat)findViewById(R.id.confirmPinSwitch);
        offlineModeSwitch = (SwitchCompat)findViewById(R.id.offlineSwitch);

        //set default values for switch buttons
        confirmPinSwtich.setChecked(SharedPrefManager.getClassinstance(getApplicationContext()).getConfirmPINPreference());
        offlineModeSwitch.setChecked(SharedPrefManager.getClassinstance(getApplicationContext()).getOfflineModePreference());


        //set onclick listener to switch buttons
        confirmPinSwtich.setOnCheckedChangeListener(this);
        offlineModeSwitch.setOnCheckedChangeListener(this);



    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        switch (buttonView.getId()){

            case R.id.confirmPinSwitch:
                SharedPrefManager.getClassinstance(getApplicationContext()).confirmPINPreference(isChecked);
                break;

            case R.id.offlineSwitch:
                SharedPrefManager.getClassinstance(getApplicationContext()).offlineModePreference(isChecked);
                break;

            default:
                break;
        }
    }

    private void showToast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
