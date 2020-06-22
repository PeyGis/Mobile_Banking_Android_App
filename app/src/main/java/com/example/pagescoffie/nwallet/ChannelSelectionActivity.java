package com.example.pagescoffie.nwallet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

public class ChannelSelectionActivity extends AppCompatActivity implements View.OnClickListener{

    private LinearLayout accountToWallet;
    private LinearLayout accountToAccount;
    private LinearLayout walletToAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_selection);

        //set app toolbar
        Toolbar toolbar =(Toolbar) findViewById(R.id.action_bar);
        toolbar.setTitle(R.string.channel_selection_toolbar_title);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorBackground));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //get view components
        accountToAccount = (LinearLayout) findViewById(R.id.accountToAccountBtn);
        accountToWallet = (LinearLayout) findViewById(R.id.accountToWalletBtn);
        walletToAccount = (LinearLayout) findViewById(R.id.walletToAccountBtn);

        //set click listener to layouts (aka button)
        accountToAccount.setOnClickListener(this);
        accountToWallet.setOnClickListener(this);
        walletToAccount.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.accountToAccountBtn:
                //open activity to transfer
                startActivity(new Intent(ChannelSelectionActivity.this, AccountToAccountActivity.class));
                finish();
                break;
            case R.id.accountToWalletBtn:
                startActivity(new Intent(ChannelSelectionActivity.this, AccountToWalletActivity.class));
                finish();
                break;
            case R.id.walletToAccountBtn:
                startActivity(new Intent(ChannelSelectionActivity.this, WalletToAccountActivity.class));
                finish();
                break;
            default:
                //
                break;
        }
    }
}
