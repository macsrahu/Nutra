package com.sales.numax.activities;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sales.numax.R;
import com.sales.numax.common.FirebaseTables;
import com.sales.numax.model.OrderLine;
import com.sales.numax.model.OrderLocation;
import com.sales.numax.model.OrderMain;
import com.sales.numax.service.LocationTrack;
import com.sales.numax.utility.Global;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class NewOrderDone extends AppCompatActivity {


    @BindView(R.id.buttonDone)
    Button buttonDone;

    private ArrayList permissionsToRequest;
    private ArrayList permissionsRejected = new ArrayList();
    private ArrayList permissions = new ArrayList();

    private final static int ALL_PERMISSIONS_RESULT = 101;
    LocationTrack locationTrack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_order_done);
        ButterKnife.bind(this);
        //Global.IS_PREVIEW = false;

        Toolbar mToolbarView = Global.PrepareToolBar(this, false, "Order Placed");
        setSupportActionBar(mToolbarView);



        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest != null) {
                if (permissionsToRequest.size() > 0)
                    requestPermissions((String[]) permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
            }
        }
        permissionsToRequest = findUnAskedPermissions(permissions);


        buttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //RegisterOrderLocation();
                Global.ORDER_LINE = new ArrayList<OrderLine>();
                Intent iMyOrder = new Intent(NewOrderDone.this, MyOrdersListActivity.class);
                Global.SELECTED_ORDER_MAIN = null;
                iMyOrder.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(iMyOrder);
                finish();
            }
        });


        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void RegisterOrderLocation() {

        locationTrack = new LocationTrack(NewOrderDone.this);
        if (locationTrack.canGetLocation()) {
            final double longitude = locationTrack.getLongitude();
            final double latitude = locationTrack.getLatitude();


            if (longitude != 0 && latitude != 0) {

                Global.ORDER_LOCATION.setLatitude(latitude);
                Global.ORDER_LOCATION.setLongitude(longitude);

                DatabaseReference mDataRefMain = FirebaseDatabase.getInstance().getReference().child(FirebaseTables.TBL_ORDERS_LOCATION);
                mDataRefMain.child(Global.NEW_ORDER_KEY).setValue(Global.ORDER_LOCATION).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "Order location registered successfully", Toast.LENGTH_SHORT).show();
                        Global.SELECTED_ORDER_MAIN = null;
                        Global.ORDER_LOCATION = new OrderLocation();
                        Intent iMyOrder = new Intent(NewOrderDone.this, MyOrdersListActivity.class);
                        Global.ORDER_LINE = new ArrayList<OrderLine>();
                        iMyOrder.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(iMyOrder);
                        finish();


                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), "Unable to register Order location", Toast.LENGTH_SHORT).show();

                Global.ORDER_LINE = new ArrayList<OrderLine>();
                Intent iMyOrder = new Intent(NewOrderDone.this, MyOrdersListActivity.class);
                Global.SELECTED_ORDER_MAIN = null;
                iMyOrder.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(iMyOrder);
                finish();
            }

        } else {
            locationTrack.showSettingsAlert();

        }
    }

    private ArrayList findUnAskedPermissions(ArrayList wanted) {
        ArrayList result = new ArrayList();
        if (wanted != null) {
            for (Object perm : wanted) {
                if (!hasPermission((String) perm)) {
                    result.add(perm);
                }
            }
        }
        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission((String) permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                for (Object perms : permissionsToRequest) {
                    if (!hasPermission((String) perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale((String) permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions((String[]) permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }

                }

                break;
        }

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(NewOrderDone.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void onBackPressed() {
        Intent iMyOrder = new Intent(NewOrderDone.this, MyOrdersListActivity.class);
        Global.ORDER_LINE = new ArrayList<OrderLine>();
        RegisterOrderLocation();
        iMyOrder.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(iMyOrder);
        finish();
    }
}
