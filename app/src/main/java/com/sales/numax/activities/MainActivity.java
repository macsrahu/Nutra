package com.sales.numax.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.sales.numax.R;
import com.sales.numax.Service.TrackingService;
import com.sales.numax.adapters.MenuAdapter;
import com.sales.numax.adapters.ProductAdapter;
import com.sales.numax.adapters.SalesAdapter;
import com.sales.numax.common.FirebaseData;
import com.sales.numax.model.ApplicationMenu;
import com.sales.numax.model.SalesAbstract;
import com.sales.numax.utility.Global;
import com.sales.numax.utility.GridSpacingItemDecoration;
import com.sales.numax.utility.InternalStorage;
import com.takusemba.multisnaprecyclerview.MultiSnapRecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    Toolbar mToolbar;
    Bundle mSavedInstanceState;
    private Drawer result = null;
    private boolean doubleBackToExitPressedOnce = false;
    private static final int PERMISSIONS_REQUEST = 100;

    MultiSnapRecyclerView rvSlide=null;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        mSavedInstanceState = savedInstanceState;


        mToolbar = Global.PrepareToolBar(this, true, getResources().getString(R.string.app_name));
        mToolbar.setTitle(getResources().getString(R.string.app_name));
        InitDrawerMenu(mToolbar);
        SetupMainMenu();

        BindSalesAbstract();


        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private  void BindSalesAbstract() {

        ArrayList<SalesAbstract> salesAbstractArrayList = new ArrayList<SalesAbstract>();
        String sHTMLContent ="";
        SalesAbstract salesAbstract = new SalesAbstract();
        salesAbstract.setSalesdate("21.DEC.2021");

        sHTMLContent="<table style='width:100%;font-size:12px;color:#288f07'>";
        sHTMLContent += "<tr><td style='width:60%'>TM 120</td><td style='width:40%;text-align:right'>150(pk)</td></tr>";
        sHTMLContent += "<tr><td>TM 250</td><td style='width:40%;text-align:right'>250(pk)</td></tr>";
        sHTMLContent += "<tr><td>TM 500</td><td style='width:40%;text-align:right'>140(pk)</td></tr>";
        sHTMLContent += "<tr><td>TM 1000</td><td style='width:40%;text-align:right'>100(pk)</td></tr>";
        sHTMLContent += "<tr><td>SM 120</td><td style='width:40%;text-align:right'>300(pk)</td></tr>";
        sHTMLContent += "<tr><td>SM 250</td><td style='width:40%;text-align:right'>200(pk)</td></tr>";
        sHTMLContent += "<tr><td>SM 500</td><td style='width:40%;text-align:right'>102(pk)</td></tr>";
        sHTMLContent += "<tr><td>SM 1000</td><td style='width:40%;text-align:right'>80(pk)</td></tr>";
        sHTMLContent+= "</table>";

        salesAbstract.setProductname(sHTMLContent);
        salesAbstractArrayList.add(salesAbstract);

        salesAbstract = new SalesAbstract();
        salesAbstract.setSalesdate("19.DEC.2021");
        salesAbstract.setProductname(sHTMLContent);
        salesAbstractArrayList.add(salesAbstract);

        salesAbstract = new SalesAbstract();
        salesAbstract.setSalesdate("18.DEC.2021");
        salesAbstract.setProductname(sHTMLContent);
        salesAbstractArrayList.add(salesAbstract);


        salesAbstract = new SalesAbstract();
        salesAbstract.setSalesdate("17.DEC.2021");
        salesAbstract.setProductname(sHTMLContent);
        salesAbstractArrayList.add(salesAbstract);


        salesAbstract = new SalesAbstract();
        salesAbstract.setSalesdate("16.DEC.2021");
        salesAbstract.setProductname(sHTMLContent);
        salesAbstractArrayList.add(salesAbstract);

        rvSlide = (MultiSnapRecyclerView) findViewById(R.id.rvSlide);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvSlide.setLayoutManager(mLayoutManager);

        rvSlide.addItemDecoration(new GridSpacingItemDecoration(1, Global.dpToPx(5, getApplicationContext()), false));
        rvSlide.setItemAnimator(new DefaultItemAnimator());

        SalesAdapter salesAdapter = new SalesAdapter(getApplicationContext(), MainActivity.this, salesAbstractArrayList);
        rvSlide.setAdapter(salesAdapter);


    }


    private void InitDrawerMenu(Toolbar toolbar) {

        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .withTranslucentStatusBar(true)
                .addProfiles(
                        new ProfileDrawerItem()
                                .withTextColor(getResources().getColor(R.color.black))
                                .withName(Global.LOGIN_USER_DETAIL != null ? Global.LOGIN_USER_DETAIL.getName() : "Numax-Sales")
                                .withEmail(Global.LOGIN_USER_DETAIL != null ? Global.LOGIN_USER_DETAIL.getEmailid() : "NA")
                                .withIcon(getResources().getDrawable(R.drawable.profile))
                )
                .build();

        result = new DrawerBuilder(this)
                .withAccountHeader(headerResult)
                .withTranslucentStatusBar(true)
                .withRootView(R.id.main)
                .withToolbar(toolbar)
                .withDisplayBelowStatusBar(true)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .addDrawerItems(
                        new SecondaryDrawerItem().withIconTintingEnabled(true).withName(R.string.drawer_item_profile).withIcon(FontAwesome.Icon.faw_user).withTextColor(getResources().getColor(R.color.purple_500)),
                        new SecondaryDrawerItem().withIconTintingEnabled(true).withName(R.string.drawer_item_reset).withIcon(FontAwesome.Icon.faw_unlock).withTextColor(getResources().getColor(R.color.purple_500)),
                        new SecondaryDrawerItem().withIconTintingEnabled(true).withName(R.string.drawer_item_logout).withIcon(FontAwesome.Icon.faw_power_off).withTextColor(getResources().getColor(R.color.purple_500)
                    )
                )
                .withSavedInstance(mSavedInstanceState)
                .build();

        result.setOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                if (position == 1) {

                } else if (position == 2) {

//                    Intent iResetPassword = new Intent(MainActivity.this, ChangePassword.class);
//                    iResetPassword.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(iResetPassword);
                } else if (position == 9) {
//                    Intent iPrinter = new Intent(MainActivity.this, PrinterSetup.class);
//                    iPrinter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(iPrinter);
                } else if (position == 3) {
                    SignOut(true);
                }
                return false;
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void SetupMainMenu(){

        ArrayList<ApplicationMenu> applicationMenus= new ArrayList<ApplicationMenu>();

        ApplicationMenu applicationMenu= new ApplicationMenu();
        applicationMenu.setCode("001");
        applicationMenu.setTitle(("Dealers"));
        applicationMenu.setImage(getApplicationContext().getDrawable(R.drawable.customer));
        applicationMenu.setDescription("To view dealers list and his sales activities");
        applicationMenus.add(applicationMenu);

        applicationMenu= new ApplicationMenu();
        applicationMenu.setCode("002");
        applicationMenu.setTitle(("Products"));
        applicationMenu.setDescription("To view products list and its rate");
        applicationMenu.setImage(getApplicationContext().getDrawable(R.drawable.milk));
        applicationMenus.add(applicationMenu);

        applicationMenu= new ApplicationMenu();
        applicationMenu.setCode("003");
        applicationMenu.setTitle(("New Dealer Enrolment"));
        applicationMenu.setImage(getApplicationContext().getDrawable(R.drawable.new_customer));
        applicationMenu.setDescription("To add new dealer information");
        applicationMenus.add(applicationMenu);

        applicationMenu= new ApplicationMenu();
        applicationMenu.setCode("004");
        applicationMenu.setImage(getApplicationContext().getDrawable(R.drawable.ic_baseline_credit_card_24));
        applicationMenu.setTitle(("Orders"));
        applicationMenu.setDescription("To view all orders by user login");
        applicationMenus.add(applicationMenu);
        applicationMenu= new ApplicationMenu();

        applicationMenu.setCode("005");
        applicationMenu.setImage(getApplicationContext().getDrawable(R.drawable.note_bell));
        applicationMenu.setTitle(("Notifications"));
        applicationMenu.setDescription("To view notifications and messages from Nutra Milk");
        applicationMenus.add(applicationMenu);

        applicationMenu= new ApplicationMenu();
        applicationMenu.setCode("006");
        applicationMenu.setImage(getApplicationContext().getDrawable(R.drawable.logout));
        applicationMenu.setTitle(("Sign Out"));
        applicationMenu.setDescription("Sign out from the app");
        applicationMenus.add(applicationMenu);

        RecyclerView rvMenu = (RecyclerView) findViewById(R.id.rvMenu);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        rvMenu.setLayoutManager(mLayoutManager);
        rvMenu.addItemDecoration(new GridSpacingItemDecoration(1, Global.dpToPx(2, getApplicationContext()), false));
        rvMenu.setItemAnimator(new DefaultItemAnimator());

        MenuAdapter menuAdapter = new MenuAdapter(getApplicationContext(), MainActivity.this, applicationMenus);
        rvMenu.setAdapter(menuAdapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {


            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (result != null && result.isDrawerOpen()) {
            result.closeDrawer();
        }
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 1000);
    }

    private void SignOut(final boolean isLogout) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setIcon(getResources().getDrawable(R.drawable.logo));
        builder.setMessage("Do you want to log out?");
        builder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                                        final int which) {
                        Global.USER_CODE = null;
                        Global.LOGIN_USER_DETAIL = null;
                        try {
                            if (isLogout) {
                                InternalStorage.resetObject(getApplicationContext(), "USER_INFO");
                                SharedPreferences loginPreferences = getSharedPreferences("NUMAX_REMEMBER_ME", MODE_PRIVATE);
                                SharedPreferences.Editor loginPrefsEditor = loginPreferences.edit();
                                loginPrefsEditor.remove("saveLogin");
                                loginPrefsEditor.remove("loginid");
                                loginPrefsEditor.apply();
                                loginPrefsEditor.commit();
                            }
                        } catch (Exception ex) {

                        }
                        finishAffinity();
                    }
                });
        builder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                                        final int which) {
                        dialog.dismiss();
                    }
                });
        final AlertDialog dialog = builder.create();
        dialog.show();

    }



    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {
        if (requestCode == PERMISSIONS_REQUEST && grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startTrackerService();
        } else {
            Toast.makeText(this, "Please enable location services to allow GPS tracking", Toast.LENGTH_SHORT).show();
        }
    }
    private void startTrackerService() {
        if (!isMyServiceRunning(TrackingService.class)) {
            Intent myIntent = new Intent(MainActivity.this, TrackingService.class);
            startService(myIntent);
            Toast.makeText(this, "Tracking Service started..", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}