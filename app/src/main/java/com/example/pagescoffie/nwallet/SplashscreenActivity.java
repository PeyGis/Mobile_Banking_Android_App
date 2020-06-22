package com.example.pagescoffie.nwallet;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.pagescoffie.nwallet.model.SharedPrefManager;

public class SplashscreenActivity extends AppCompatActivity {

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                    Intent intent=new Intent(SplashscreenActivity.this,LoginActivity.class);
                    startActivity(intent);
                    finish();
            }
        },3000);
    }
}
