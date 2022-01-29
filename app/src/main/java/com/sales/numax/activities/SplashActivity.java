package com.sales.numax.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.sales.numax.R;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);
        Thread splashThread = new Thread() {
            public void run() {
                try {
                    sleep(5 * 1000);
                    Intent mainIntent = new Intent(SplashActivity.this, LoginActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(mainIntent);
                    finish();

//                    SharedPreferences sharedPref = getSharedPreferences("NUTRA_REMEMBER_ME", MODE_PRIVATE);
//                    boolean saveLogin = sharedPref.getBoolean("saveLogin", false);
//                    if (!saveLogin) {
//                        Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
//                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(mainIntent);
//                        finish();
//                    } else {
//                       // Global.USER_TYPE = sharedPref.getInt("usertype", 0);
//                       // Global.USER_CODE = sharedPref.getString("loginid", "");
////                        if (Global.USER_CODE != null) {
////                            Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
////                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////                            startActivity(mainIntent);
////                            finish();
////                        } else {
////                            Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
////                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////                            startActivity(mainIntent);
////                            finish();
////                        }
//                        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
//                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(mainIntent);
//                        finish();
//                    }

                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        };
        splashThread.start();
    }
}
