package com.example.pagescoffie.nwallet;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pagescoffie.nwallet.model.SharedPrefManager;

public class DashboardActivity extends AppCompatActivity implements View.OnClickListener {

    private boolean doubleBackToExit = false;
    private Handler   mHandler = new Handler();
    final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            doubleBackToExit = false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Toolbar toolbar;
        LinearLayout fundsBtn;
        LinearLayout rechargeBtn;
        LinearLayout walletBtn;
        LinearLayout servicesBtn;
        LinearLayout paymentBtn;
        LinearLayout myAccountBtn;
        LinearLayout logoutBtn;
        LinearLayout helpBtn;
        LinearLayout settingsBtn;
        TextView usernameTxt;

        //set action bar
        toolbar = (Toolbar)findViewById(R.id.action_bar);
        toolbar.setTitle(R.string.home_toolbar_title);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorBackground));
        setSupportActionBar(toolbar);

        //init buttons
        fundsBtn = (LinearLayout)findViewById(R.id.fundTransferBtn);
        rechargeBtn = (LinearLayout)findViewById(R.id.rechargeBtn);
        walletBtn = (LinearLayout)findViewById(R.id.walletBtn);
        servicesBtn = (LinearLayout)findViewById(R.id.servicesBtn);
        paymentBtn = (LinearLayout)findViewById(R.id.paymentBtn);
        myAccountBtn = (LinearLayout)findViewById(R.id.myAcountsBtn);
        logoutBtn = (LinearLayout)findViewById(R.id.logoutBtn);
        helpBtn = (LinearLayout)findViewById(R.id.helpBtn);
        settingsBtn = (LinearLayout)findViewById(R.id.settingsBtn);

        //set buttons to listen to click events
        fundsBtn.setOnClickListener(this);
        rechargeBtn.setOnClickListener(this);
        walletBtn.setOnClickListener(this);
        servicesBtn.setOnClickListener(this);
        paymentBtn.setOnClickListener(this);
        myAccountBtn.setOnClickListener(this);
        logoutBtn.setOnClickListener(this);
        helpBtn.setOnClickListener(this);
        settingsBtn.setOnClickListener(this);

        //set username label from shared preference
        usernameTxt = (TextView) findViewById(R.id.usernameTxt);
        usernameTxt.append(SharedPrefManager.getClassinstance(DashboardActivity.this).getUserName());

        //check is session is still valid else redirect user to login --for authentication
        if(!SharedPrefManager.getClassinstance(DashboardActivity.this).isLoggedIn()){
            startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
            finish();
        }

    }

    /**
     * This function inflates the menu item defined in the XML layout
     * @param menu menu to be inflated
     * @return true after a successful menu inflation
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.nav_menu, menu);
        return true;
    }

    /**
     * A function to perform action based on menu item seleted
     * @param item menu item
     * @return true
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.item_about:
                showToast("About Us");
                return true;
            case R.id.item_help:
                showToast("Help Option");
                return true;
            case R.id.item_logout:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * A function to show toast
     * @param msg message
     */
    private void showToast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * A function that handles logic upon selecting any of the dashboard buttons
     * @param v the view
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id){
            case R.id.fundTransferBtn:
                startActivity(new Intent(DashboardActivity.this, ChannelSelectionActivity.class));
                break;
            case R.id.walletBtn:
                startActivity(new Intent(DashboardActivity.this, MyWalletActivity.class));
                break;
            case R.id.rechargeBtn:
                startActivity(new Intent(DashboardActivity.this, RechargeActivity.class));
                break;
            case R.id.servicesBtn:
                startActivity(new Intent(DashboardActivity.this, ServicesActivity.class));
                break;
            case R.id.paymentBtn:
                startActivity(new Intent(DashboardActivity.this, SelectMerchantActivity.class));
                break;
            case R.id.myAcountsBtn:
                startActivity(new Intent(DashboardActivity.this, MyAccountActivity.class));
                break;
            case R.id.logoutBtn:
                logout();
                break;
            case R.id.settingsBtn:
                startActivity(new Intent(DashboardActivity.this, SettingsActivity.class));
                break;
            case R.id.helpBtn:
                startActivity(new Intent(DashboardActivity.this, ChartsActivity.class));
                break;

            default:
                showToast("Nothing selected");

        }
    }

    /**
     * A method to log user out
     */
    public void logout(){

        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(DashboardActivity.this);
        alertBuilder.setMessage("Do you want to exit the app?");
        alertBuilder.setCancelable(false);
        alertBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPrefManager.getClassinstance(getApplicationContext()).logout();
                finish();
            }
        });

        AlertDialog dialog = alertBuilder.create();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        if(doubleBackToExit){
            super.onBackPressed();
           return;
        }
        this.doubleBackToExit = true;
        Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
        mHandler.postDelayed(mRunnable, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mHandler !=null){
            mHandler.removeCallbacks(mRunnable);
        }
    }
}
