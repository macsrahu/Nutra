package com.sales.numax.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.jaredrummler.materialspinner.MaterialSpinnerAdapter;
import com.sales.numax.R;
import com.sales.numax.adapters.DealerAdapter;
import com.sales.numax.adapters.ProductAdapter;
import com.sales.numax.common.FirebaseData;
import com.sales.numax.common.FirebaseTables;
import com.sales.numax.model.Category;
import com.sales.numax.model.Dealer;
import com.sales.numax.model.Product;
import com.sales.numax.model.Route;
import com.sales.numax.utility.Global;
import com.sales.numax.utility.GridSpacingItemDecoration;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DealersActivity extends AppCompatActivity {

    private ArrayList<Dealer> mDealer = new ArrayList<Dealer>();
    @BindView(R.id.rvDealers)
    RecyclerView rvDealers;

    @BindView(R.id.spinnerRoute)
    MaterialSpinner spinnerRoute;

    @BindView(R.id.tvNoRecordFound)
    TextView tvNoRecordFound;

    DealerAdapter adapter;
    Toolbar mToolbarView = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dealer_list);
        ButterKnife.bind(this);

        mToolbarView = Global.PrepareToolBar(this, true, "Dealers");
        setSupportActionBar(mToolbarView);

        InitControls();

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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
        Intent iMain = new Intent(DealersActivity.this, MainActivity.class);
        iMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(iMain);
        finish();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_dealer, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_add_dealer:

                Global.SELECTED_DEALER=null;
                Global.DEALER_KEY="";

                Intent iDealerEntry = new Intent(DealersActivity.this, DealerEntry.class);
                iDealerEntry.putExtra("FROM", "DEALER");
                iDealerEntry.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(iDealerEntry);
                finish();
                return true;

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


    private void InitControls() {

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        rvDealers.setLayoutManager(mLayoutManager);
        rvDealers.addItemDecoration(new GridSpacingItemDecoration(1, Global.dpToPx(5, getApplicationContext()), false));
        rvDealers.setItemAnimator(new DefaultItemAnimator());
        LoadRoute();
    }

    private void LoadRoute(){
        Global.ROUTES= new ArrayList<Route>();
        FirebaseData.LoadRoutes(getApplicationContext());
        if (Global.ROUTES != null) {
            MaterialSpinnerAdapter spiinerRoute = new MaterialSpinnerAdapter<Route>(getBaseContext(), Global.ROUTES);
            spinnerRoute.setAdapter(spiinerRoute);
            spinnerRoute.setSelected(true);
            spinnerRoute.setText("SELECT ROUTE");
            spinnerRoute.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<Route>() {
                @Override
                public void onItemSelected(MaterialSpinner view, int position, long id, Route item) {
                    view.setSelectedIndex(position);

                    if (view != null && item != null) {
                        if (item.getKey() != null) {
                            LoadDealers(item.getKey());
                        }
                    }
                }
            });
            spinnerRoute.performClick();
        }
    }

    private void LoadDealers(String routekey){

        final ProgressDialog dialog = ProgressDialog.show(this,
                null,
                "Loading dealer",
                true);
        dialog.setInverseBackgroundForced(true);
        dialog.show();
        //Toast.makeText(getApplicationContext(),routekey,Toast.LENGTH_LONG).show();
        FirebaseDatabase.getInstance().getReference().child(FirebaseTables.TBL_DEALERS)
            .orderByChild("routekey").equalTo(routekey).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                dialog.dismiss();
                mDealer = new ArrayList<Dealer>();
                if (task.isSuccessful()) {
                    for (DataSnapshot dealerSnapshot : task.getResult().getChildren()) {
                        Dealer dealer = dealerSnapshot.getValue(Dealer.class);
                        if (dealer != null) {
                            dealer.setKey(dealerSnapshot.getKey());
                            if (dealer.getIsactive() == 1) {
                                dealer.setKey(dealer.getKey());
                                mDealer.add(dealer);
                            }
                        }
                    }
                    if (mDealer.size() > 0) {
                        adapter = new DealerAdapter(DealersActivity.this, mDealer);
                        rvDealers.setAdapter(adapter);
                        tvNoRecordFound.setVisibility(View.GONE);
                        rvDealers.setVisibility(View.VISIBLE);
                    } else {
                        tvNoRecordFound.setText("Dealer(s) not found");
                        tvNoRecordFound.setVisibility(View.VISIBLE);
                        rvDealers.setVisibility(View.GONE);
                    }
                    dialog.dismiss();
                } else {
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    Log.d("firebase", task.getException().getMessage());
                }
            }
        });
    }
}
