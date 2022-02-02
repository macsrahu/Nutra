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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.sales.numax.R;
import com.sales.numax.adapters.DealerAdapter;
import com.sales.numax.adapters.NewOrderAdapter;
import com.sales.numax.common.FirebaseTables;
import com.sales.numax.model.Dealer;
import com.sales.numax.utility.Global;
import com.sales.numax.utility.GridSpacingItemDecoration;
import com.sales.numax.utility.Messages;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewOrderActivity extends AppCompatActivity {

    private ArrayList<Dealer> mDealer = new ArrayList<Dealer>();
    @BindView(R.id.rvOrderItems)
    RecyclerView rvOrderItems;

    @BindView(R.id.tvNoRecordFound)
    TextView tvNoRecordFound;

    @BindView(R.id.bottomNavigation)
    BottomNavigationView bottomNavigation;

    NewOrderAdapter adapter;
    Toolbar mToolbarView = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_order);
        ButterKnife.bind(this);

        Global.MENU_FROM = "ORDER";
        mToolbarView = Global.PrepareToolBar(this, true, "New Order");
        setSupportActionBar(mToolbarView);

        InitControls();

        BindGrid();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void InitControls() {
        bottomNavigation.setVisibility(View.VISIBLE);
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {

                case R.id.btnNext:

                    break;

                case R.id.btnAdd:

                    Intent iDealerEntry = new Intent(NewOrderActivity.this, ProductsActivity.class);
                    iDealerEntry.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(iDealerEntry);
                    finish();
                    break;

                case R.id.btnCancel:
                    onBackPressed();
                    break;
            }
            return true;
        });


    }

    private void BindGrid() {
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        rvOrderItems.setLayoutManager(mLayoutManager);
        rvOrderItems.addItemDecoration(new GridSpacingItemDecoration(1, Global.dpToPx(5, getApplicationContext()), false));
        rvOrderItems.setItemAnimator(new DefaultItemAnimator());
        final ProgressDialog dialog = ProgressDialog.show(this,
                null,
                "Loading dealer",
                true);
        dialog.setInverseBackgroundForced(true);
        dialog.show();
        if (Global.ORDER_LINE!=null) {
            if (Global.ORDER_LINE.size()>0) {
                adapter = new NewOrderAdapter(getApplicationContext(), NewOrderActivity.this, Global.ORDER_LINE);
                rvOrderItems.setAdapter(adapter);
                rvOrderItems.setVisibility(View.VISIBLE);
            }
        }else{
            rvOrderItems.setVisibility(View.GONE);
            tvNoRecordFound.setText("No order found");
            tvNoRecordFound.setVisibility(View.VISIBLE);
        }
        dialog.dismiss();
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
        Global.MENU_FROM = "";
        Intent iMain = new Intent(NewOrderActivity.this, MainActivity.class);
        iMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(iMain);
        finish();
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

    private void SaveOrder() {

    }
}
