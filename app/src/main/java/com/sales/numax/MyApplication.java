package com.sales.numax;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;


import androidx.multidex.MultiDex;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

import io.paperdb.Paper;


//import com.crashlytics.android.Crashlytics;
//import io.fabric.sdk.android.Fabric;


public class MyApplication extends Application {

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        MultiDex.install(this);
        Paper.init(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Paper.init(getApplicationContext());

//        Fabric.with(this, new Crashlytics());s
//        StrictMode.VmPolicy.Builder builder = null;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
//            builder = new StrictMode.VmPolicy.Builder();
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
//            StrictMode.setVmPolicy(builder.build());
//        }
        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

}