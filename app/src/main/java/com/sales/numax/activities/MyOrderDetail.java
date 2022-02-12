package com.sales.numax.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.sales.numax.R;
import com.sales.numax.adapters.NewOrderReviewAdapter;
import com.sales.numax.adapters.OrdersAdapter;
import com.sales.numax.common.FirebaseTables;
import com.sales.numax.model.OrderLine;
import com.sales.numax.model.OrderMain;
import com.sales.numax.utility.Global;
import com.sales.numax.utility.GridSpacingItemDecoration;
import com.sales.numax.utility.Messages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MyOrderDetail extends AppCompatActivity {

    @BindView(R.id.rvOrderItems)
    RecyclerView rvOrderItems;

    @BindView(R.id.tvNoRecordFound)
    TextView tvNoRecordFound;

    @BindView(R.id.bottomNavigation)
    BottomNavigationView bottomNavigation;

    @BindView(R.id.text_total)
    TextView text_total;

    @BindView(R.id.tvDealer)
    TextView tvDealer;

    @BindView(R.id.tvOrderNo)
    TextView tvOrderNo;

    @BindView(R.id.tvOrderDate)
    TextView tvOrderDate;

    @BindView(R.id.tvOrderAmount)
    TextView tvOrderAmount;


    @BindView(R.id.layTotal)
    LinearLayout layTotal;

    NewOrderReviewAdapter mOrderAdapter;


    Double dblAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_detail);
        ButterKnife.bind(this);


        Toolbar toolbar = Global.PrepareToolBar(this, true, "Order Detail");
        toolbar.setSubtitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);


        if (Global.SELECTED_ORDER_MAIN != null) {
            InitControls();
        } else {
            Messages.ShowToast(getApplicationContext(), "Order not loaded");
        }

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void InitControls() {

        if (Global.SELECTED_ORDER_MAIN != null) {

            tvDealer.setText(Global.SELECTED_ORDER_MAIN.getDealer() + "\n" + Global.SELECTED_ORDER_MAIN.getAddress());
            tvOrderAmount.setText(Global.GetFormatedValue(Global.SELECTED_ORDER_MAIN.getOrderamount()));
            tvOrderDate.setText(Global.SELECTED_ORDER_MAIN.getOrderdate());
            tvOrderNo.setText(Global.SELECTED_ORDER_MAIN.getOrderno());

            RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
            rvOrderItems.setLayoutManager(mLayoutManager);
            rvOrderItems.addItemDecoration(new GridSpacingItemDecoration(1, Global.dpToPx(5, getApplicationContext()), false));
            rvOrderItems.setItemAnimator(new DefaultItemAnimator());
            final ProgressDialog dialog = ProgressDialog.show(this,
                    null,
                    "Loading orders",
                    true);
            dialog.show();

            FirebaseDatabase.getInstance().getReference().child(FirebaseTables.TBL_ORDERS_LINE_ITEMS)
                    .child(Global.SELECTED_ORDER_MAIN.getKey()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    ArrayList<OrderLine> mOrdersLines = new ArrayList<OrderLine>();
                    if (task.isSuccessful()) {

                        for (DataSnapshot orderSnapshot : task.getResult().getChildren()) {
                            OrderLine mOrdersLine = orderSnapshot.getValue(OrderLine.class);
                            if (mOrdersLine != null) {
                                mOrdersLine.setKey(orderSnapshot.getKey());
                                mOrdersLines.add(mOrdersLine);
                            }
                        }
                        if (mOrdersLines.size() > 0) {
                            Global.ORDER_LINE = mOrdersLines;
                            mOrderAdapter = new NewOrderReviewAdapter(getApplicationContext(), MyOrderDetail.this, mOrdersLines);
                            rvOrderItems.setAdapter(mOrderAdapter);
                            tvNoRecordFound.setVisibility(View.GONE);
                            rvOrderItems.setVisibility(View.VISIBLE);
                            layTotal.setVisibility(View.VISIBLE);
                            CalculateTotal(mOrdersLines);
                        } else {
                            rvOrderItems.setVisibility(View.GONE);
                            tvNoRecordFound.setVisibility(View.VISIBLE);
                            layTotal.setVisibility(View.GONE);
                        }
                        dialog.dismiss();
                    } else {
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });

        }

        bottomNavigation.setVisibility(View.VISIBLE);
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {

                case R.id.btnPrint:
                    Intent iMain = new Intent(MyOrderDetail.this, PrintOrder.class);
                    iMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(iMain);
                    finish();
                    break;

                case R.id.btnReorder:
                    break;

                case R.id.btnBack:
                    onBackPressed();
                    break;
            }
            return true;
        });

    }

    public void CalculateTotal(ArrayList<OrderLine> mOrderLine) {
        dblAmount = 0d;
        for (int i = 0; i < mOrderLine.size(); i++) {
            dblAmount = dblAmount + mOrderLine.get(i).getAmount();
        }
        text_total.setText(Global.GetFormatedValue(dblAmount));
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
        super.onBackPressed();
    }

}
