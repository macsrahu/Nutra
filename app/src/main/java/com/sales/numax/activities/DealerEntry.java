package com.sales.numax.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.chivorn.smartmaterialspinner.SmartMaterialSpinner;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.jaredrummler.materialspinner.MaterialSpinnerAdapter;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.sales.numax.BuildConfig;
import com.sales.numax.R;
import com.sales.numax.common.FirebaseData;
import com.sales.numax.common.FirebaseTables;
import com.sales.numax.model.Dealer;
import com.sales.numax.model.Route;
import com.sales.numax.utility.Global;
import com.sales.numax.utility.KeyboardUtil;
import com.sales.numax.utility.Messages;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DealerEntry extends AppCompatActivity {

    private static final int INITIAL_REQUEST = 100;
    @BindView(R.id.input_donor_shop)
    TextInputEditText input_donor_shop;

    @BindView(R.id.input_dealer_name)
    TextInputEditText input_donor_name;

    @BindView(R.id.input_dealer_gst)
    TextInputEditText input_dealer_gst;


    @BindView(R.id.input_address_line1)
    TextInputEditText input_address_line1;

    @BindView(R.id.input_address_line2)
    TextInputEditText input_address_line2;

    @BindView(R.id.input_town)
    TextInputEditText input_town;

    @BindView(R.id.input_city)
    TextInputEditText input_city;

    @BindView(R.id.input_district)
    TextInputEditText input_district;

    @BindView(R.id.input_pincode)
    TextInputEditText input_pincode;

    @BindView(R.id.input_mobile_no)
    TextInputEditText input_mobile_no;

    @BindView(R.id.input_whatsapp_no)
    TextInputEditText input_whatsapp_no;

    @BindView(R.id.input_email)
    TextInputEditText input_email;

    @BindView(R.id.sp_routes)
    SmartMaterialSpinner sp_routes;

    @BindView(R.id.button_location)
    TextView button_location;

    @BindView(R.id.bottomNavigation)
    BottomNavigationView bottonNavigationView;

    Toolbar mToolbarView = null;
    Dealer mDealer = new Dealer();
    boolean ENTRY_MODE_NEW = true;
    Boolean isValid = false;

    String mShopName, mGST, mDealerName, mEmail, mMobile, mWhatsAppNo, mAddressLine1, mAddressLine2, mCity, mPincode;
    String mDistrict, mState;
    String _NAVIGATE_FROM = "";
    String mPrimaryKey = "";
    String mSelectedRouteKey = "";

    //Location Check
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    private static final int REQUEST_CHECK_SETTINGS = 100;

    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private Boolean mRequestingLocationUpdates;
    private String mLastUpdateTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dealer_entry);
        ButterKnife.bind(this);
        try {
            Toolbar mToolbarView = Global.PrepareToolBar(this, true, "Dealer Entry");

            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                _NAVIGATE_FROM = extras.getString("FROM");
            }

            mDealer = Global.SELECTED_DEALER;

            if (mDealer != null) {
                Global.DEALER_KEY = mDealer.getKey();
                ENTRY_MODE_NEW = false;
                mToolbarView = Global.PrepareToolBar(this, true, "Update Dealer");
                InitControls();
                BindControls();
            } else {
                InitControls();
                mDealer = new Dealer();
                Global.DEALER_KEY = "";
            }
            setSupportActionBar(mToolbarView);

            InitLocation();
            button_location.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startLocationButtonClick();
                }
            });
            KeyboardUtil.hideKeyboard(this);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

        } catch (Exception ex) {
            Messages.ShowToast(getApplicationContext(), ex.getMessage());
        }
    }

    private void InitControls() {

        if (!ENTRY_MODE_NEW) {
            bottonNavigationView.getMenu().findItem(R.id.btnSave).setTitle("Update");
        }

        button_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        bottonNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {

                case R.id.btnSave:
                    boolean isValid = Validate();
                    if (Validate()) {
                        SaveRecord();
                    }
                    break;

                case R.id.btnPhoto:

                    if (Global.DEALER_KEY.isEmpty()) {
                        Messages.ShowToast(getApplicationContext(), "Before upload picture,please add dealer information");
                    } else {
                        Intent iUpload = new Intent(DealerEntry.this, UploadPhoto.class);
                        iUpload.putExtra("FROM", "DEALER");
                        iUpload.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(iUpload);
                    }
                    break;

                case R.id.btnCancel:
                    onBackPressed();
                    break;
            }
            return true;
        });

        BindRoutes();
    }

    private void BindRoutes() {
        Global.ROUTES = new ArrayList<Route>();
        FirebaseData.LoadRoutes(getApplicationContext());
        if (Global.ROUTES != null) {
            sp_routes.setItem(Global.ROUTES);
            if (Global.SELECTED_DEALER != null) {
                int i = 0;
                for (Route mRoute : Global.ROUTES) {
                    if (mRoute.getKey().equals(Global.SELECTED_DEALER.getRoutekey())) {
                        sp_routes.setSelection(i);
                        break;
                    }
                    i++;
                }
            }
            sp_routes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    //Toast.makeText(DealerEntry.this, Global.ROUTES.get(position).getRoute(), Toast.LENGTH_SHORT).show();
                    mSelectedRouteKey = Global.ROUTES.get(position).getKey();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    mSelectedRouteKey = "";
                }
            });
        }


    }

    private void BindControls() {


        input_donor_shop.setText(mDealer.getShop());
        input_donor_name.setText(mDealer.getDealername());
        if (mSelectedRouteKey.isEmpty() || mSelectedRouteKey == "") {
            sp_routes.setEnableErrorLabel(true);
            sp_routes.setErrorText("Select Route");
        } else {
            sp_routes.setEnableErrorLabel(false);
        }
        if (mDealer.getGst() != null) {
            input_dealer_gst.setText(mDealer.getGst());
        }
        if (mDealer.getAddressline1() != null) {
            input_address_line1.setText(mDealer.getAddressline1());
        }
        if (mDealer.getAddressline2() != null) {
            input_address_line2.setText(mDealer.getAddressline2());
        }
        if (mDealer.getTown() != null) {
            input_town.setText(mDealer.getTown());
        }
        if (mDealer.getCity() != null) {
            input_city.setText(mDealer.getCity());
        }
        if (mDealer.getPincode() != null) {
            input_pincode.setText(mDealer.getPincode());
        }
        if (mDealer.getMobile() != null) {
            input_mobile_no.setText(mDealer.getMobile());
        }
        if (mDealer.getWhatsappno() != null) {
            input_whatsapp_no.setText(mDealer.getWhatsappno());
        }
        if (mDealer.getEmail() != null) {
            input_email.setText(mDealer.getEmail());
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        Global.SELECTED_DEALER = null;
        if (_NAVIGATE_FROM=="DEALER") {
            Intent iDealer = new Intent(DealerEntry.this, DealersActivity.class);
            iDealer.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(iDealer);
            finish();
        }else{
            Intent iMain = new Intent(DealerEntry.this, MainActivity.class);
            iMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(iMain);
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void SaveRecord() {

        final ProgressDialog dialog = ProgressDialog.show(DealerEntry.this, null, "Updating data..", true);
        dialog.show();

        DatabaseReference mDataRef = FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_DEALERS);
        mDealer = new Dealer();
        if (ENTRY_MODE_NEW) {
            mPrimaryKey = mDataRef.push().getKey();
        } else {
            mDealer = Global.SELECTED_DEALER;
            mPrimaryKey = Global.SELECTED_DEALER.getKey();
        }
        Global.DEALER_KEY = mPrimaryKey;
        mDealer.setRoutekey(mSelectedRouteKey);
        mDealer.setDealername(mDealerName);
        mDealer.setShop(mShopName);
        mDealer.setGst(mGST);
        mDealer.setAddressline1(mAddressLine1);
        mDealer.setAddressline2(mAddressLine2);
        mDealer.setCity(mCity);
        mDealer.setPincode(mPincode);
        mDealer.setEmail(mEmail);
        mDealer.setMobile(mMobile);
        mDealer.setWhatsappno(mWhatsAppNo);
        mDealer.setDescription("NA");
        if (ENTRY_MODE_NEW) {
            mDealer.setIsactive(1);
        }
        mDealer.setKey(mPrimaryKey);

        mDataRef.child(mPrimaryKey).setValue(mDealer).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                if (ENTRY_MODE_NEW) {
                    dialog.dismiss();
                    Messages.ShowToast(getApplicationContext(), "New dealer has been added successfully");
                    Global.SELECTED_DEALER = mDealer;
                    Intent iMain = new Intent(DealerEntry.this, UploadPhoto.class);
                    iMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(iMain);
                } else {
                    dialog.dismiss();
                    Messages.ShowToast(getApplicationContext(), "Dealer detail has been updated successfully");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Messages.ShowToast(getApplicationContext(), "Unable to add new dealer");
                dialog.dismiss();
            }
        });
    }

    private boolean Validate() {

        boolean isvalid = true;
        mShopName = input_donor_shop.getText().toString();
        mDealerName = input_donor_name.getText().toString();

        mAddressLine1 = input_address_line1.getText().toString();
        mAddressLine2 = input_address_line2.getText().toString();
        mCity = input_city.getText().toString();
        mPincode = input_district.getText().toString();
        mDistrict = input_district.getText().toString();

        mEmail = input_email.getText().toString() != null ? input_email.getText().toString() : "NA";
        mMobile = input_mobile_no.getText().toString();
        mWhatsAppNo = input_whatsapp_no.getText().toString() != null ? input_whatsapp_no.getText().toString() : "NA";
        mPincode = input_pincode.getText().toString();

        if (mShopName.isEmpty()) {
            input_donor_shop.setError("Cannot be empty");
            isvalid = false;
        } else {
            input_donor_shop.setError(null);
        }

        if (mDealerName.isEmpty()) {
            input_donor_name.setError("Cannot be empty");
            isvalid = false;
        } else {
            input_donor_name.setError(null);
        }
        if (mAddressLine1.isEmpty()) {
            input_address_line1.setError("Cannot be empty");
            isvalid = false;
        } else {
            input_address_line1.setError(null);
        }

        if (mCity.isEmpty()) {
            input_city.setError("Cannot be empty");
            isvalid = false;
        } else {
            input_city.setError(null);
        }

        if (mPincode.isEmpty()) {
            input_pincode.setError("Cannot be empty");
            isvalid = false;
        } else {
            input_pincode.setError(null);
        }
//        if (mDistrict.isEmpty()) {
//            input_district.setError("Cannot be empty");
//            isvalid = false;
//        } else {
//            input_district.setError(null);
//        }
        if (mMobile.isEmpty() || mMobile.length() != 10) {
            input_mobile_no.setError("Enter valid Mobile No");
            isvalid = false;
        } else {
            input_mobile_no.setError(null);
        }

        return isvalid;
    }

    //Location Capturing
    private void InitLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // location is received
                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

                updateLocationUI();
            }
        };

        mRequestingLocationUpdates = false;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void updateLocationUI() {

        //Messages.ShowToast(getApplicationContext(),"Coming");
        if (mCurrentLocation != null) {
            getCompleteAddressString(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
            stopLocationUpdates();
        }
        //toggleButtons();
    }
    public void stopLocationUpdates() {
        // Removing location updates
        mFusedLocationClient
                .removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "Location captured", Toast.LENGTH_SHORT).show();
                        button_location.setEnabled(true);
                        //toggleButtons();
                    }
                });
    }
    private void getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");
                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                //strAdd = strReturnedAddress.toString();
                //Log.w("My Current loction address", strReturnedAddress.toString());
                new MaterialDialog.Builder(DealerEntry.this)
                        .positiveText("Yes")
                        .negativeText("No")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                if (returnedAddress.getAddressLine(0).split(",").length>3) {
                                    String sFlat=  returnedAddress.getAddressLine(0).split(",")[0] + returnedAddress.getAddressLine(0).split(",")[1];
                                    input_address_line1.setText(sFlat);
                                    input_address_line2.setText(returnedAddress.getAddressLine(0).split(",")[2]);
                                }
                                input_town.setText(returnedAddress.getLocality());
                                input_city.setText(returnedAddress.getSubAdminArea());
                                input_pincode.setText(returnedAddress.getPostalCode());
                                //String knownName = returnedAddress.getFeatureName();
                                //String state = returnedAddress.getAdminArea();
                                //String country = returnedAddress.getCountryName();
                                //
                            }
                        })
                        .title("Captured Address")
                        .content(strReturnedAddress.toString() +"\n\n" +" Do you want to add this to address ?").show();
            } else {
                //Log.w("My Current loction address", "No Address returned!");
                Messages.ShowToast(getApplicationContext(),"No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Log.w("My Current loction address", "Canont get Address!");
        }
    }


    public void startLocationButtonClick() {
        // Requesting ACCESS_FINE_LOCATION using Dexter library
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        mRequestingLocationUpdates = true;
                        startLocationUpdates();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            // open device settings when the permission is
                            // denied permanently
                            openSettings();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void startLocationUpdates() {
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        //Log.i(TAG, "All location settings are satisfied.");

                        Toast.makeText(getApplicationContext(), "Started location updates!", Toast.LENGTH_SHORT).show();

                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        updateLocationUI();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                               // Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                //        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(DealerEntry.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                   // Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                              //s  Log.e(TAG, errorMessage);

                                Toast.makeText(DealerEntry.this, errorMessage, Toast.LENGTH_LONG).show();
                        }

                        updateLocationUI();
                    }
                });
    }


}
