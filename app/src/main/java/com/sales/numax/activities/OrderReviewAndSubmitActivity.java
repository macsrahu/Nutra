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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sales.numax.R;
import com.sales.numax.adapters.NewOrderAdapter;
import com.sales.numax.adapters.NewOrderReviewAdapter;
import com.sales.numax.common.FirebaseTables;
import com.sales.numax.model.Dealer;
import com.sales.numax.model.OrderLine;
import com.sales.numax.model.OrderMain;
import com.sales.numax.utility.Global;
import com.sales.numax.utility.GridSpacingItemDecoration;
import com.sales.numax.utility.Messages;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OrderReviewAndSubmitActivity extends AppCompatActivity {

    private ArrayList<Dealer> mDealer = new ArrayList<Dealer>();
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

    @BindView(R.id.tvShopName)
    TextView tvShopName;


    @BindView(R.id.layTotal)
    LinearLayout layTotal;

    NewOrderReviewAdapter adapter;
    Toolbar mToolbarView = null;
    Double dblAmount = 0d;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_review_submit);
        ButterKnife.bind(this);

        Global.MENU_FROM = "ORDER";
        mToolbarView = Global.PrepareToolBar(this, true, "Place Order");
        setSupportActionBar(mToolbarView);

        InitControls();

        BindGrid();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void InitControls() {

        if (Global.SELECTED_DEALER != null) {
            String sAddress = Global.GetDealerAddress(Global.SELECTED_DEALER);
            tvShopName.setText(Global.SELECTED_DEALER.getShop());
            tvDealer.setText(sAddress);
        }
        layTotal.setVisibility(View.GONE);
        bottomNavigation.setVisibility(View.VISIBLE);
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {

                case R.id.btnSubmit:
                    Messages.ShowToast(getApplicationContext(),"First");
                    if (Global.ORDER_LINE.size() > 0) {
                        Messages.ShowToast(getApplicationContext(),"Second");
                        SubmitOrder();
                    } else {
                        Messages.ShowToast(getApplicationContext(), "No order item found to submit");
                    }
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
                "Loading orders",
                true);
        dialog.setInverseBackgroundForced(true);
        dialog.show();
        if (Global.ORDER_LINE != null) {
            if (Global.ORDER_LINE.size() > 0) {
                adapter = new NewOrderReviewAdapter(getApplicationContext(), OrderReviewAndSubmitActivity.this, Global.ORDER_LINE);
                rvOrderItems.setAdapter(adapter);
                rvOrderItems.setVisibility(View.VISIBLE);
                CalculateTotal();
                layTotal.setVisibility(View.VISIBLE);
            }
        } else {
            rvOrderItems.setVisibility(View.GONE);
            tvNoRecordFound.setText("No order found");
            tvNoRecordFound.setVisibility(View.VISIBLE);
            layTotal.setVisibility(View.GONE);

        }
        dialog.dismiss();
    }

    public void CalculateTotal() {
        dblAmount = 0d;
        for (int i = 0; i < Global.ORDER_LINE.size(); i++) {
            dblAmount = dblAmount + Global.ORDER_LINE.get(i).getAmount();
        }
        text_total.setText(Global.GetFormatedValue(dblAmount));
    }

    private void SubmitOrder() {

        final ProgressDialog dialog = ProgressDialog.show(this,
                null,
                "Placing your order..",
                true);

        dialog.show();
        Messages.ShowToast(getApplicationContext(),"Third");
        OrderMain mOrderMain = new OrderMain();
        String mOrderNo = "ORD-" + String.valueOf(new Date().getTime());
        String mOrderDate = SimpleDateFormat.getDateTimeInstance().format(new Date());

        mOrderMain.setOrderno(mOrderNo);
        mOrderMain.setAddress(Global.GetDealerAddress(Global.SELECTED_DEALER));
        mOrderMain.setOrderdate(mOrderDate);
        mOrderMain.setOrderdatestamp(new Date().getTime());
        mOrderMain.setDealer(Global.SELECTED_DEALER.getShop() + "\n" + Global.SELECTED_DEALER.getDealername());
        mOrderMain.setDealerkey(Global.SELECTED_DEALER.getKey());
        mOrderMain.setIscancelled(0);
        mOrderMain.setIsdelivered(0);
        mOrderMain.setOrderamount(dblAmount);

        DatabaseReference mDataRefMain = FirebaseDatabase.getInstance().getReference().child(FirebaseTables.TBL_ORDERS_MAIN);
        final String sKey = mDataRefMain.push().getKey();
        mOrderMain.setKey(sKey);
        mDataRefMain.child(sKey).setValue(mOrderMain).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(@NonNull @NotNull Void unused) {

                DatabaseReference mDataOrderLineItem = FirebaseDatabase.getInstance().getReference().child(FirebaseTables.TBL_ORDERS_LINE_ITEMS);
                int OrderLineId = 1;
                for (OrderLine ordersLineItems : Global.ORDER_LINE) {
                    ordersLineItems.setOrderkey(sKey);
                    ordersLineItems.setLineno(OrderLineId);
                    OrderLineId++;
                    String mLineKey = mDataOrderLineItem.push().getKey();
                    mDataOrderLineItem.child(sKey).child(mLineKey).setValue(ordersLineItems).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    });
                }
            }
        }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Global.ORDER_LINE = new ArrayList<OrderLine>();
                Global.SELECTED_DEALER = null;
                dialog.dismiss();
                if (task.isSuccessful()) {
                    new MaterialDialog.Builder(OrderReviewAndSubmitActivity.this)
                            .title("New Order")
                            .content("Your order has been placed success fully")
                            .positiveText("Ok")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull @NotNull MaterialDialog dialog, @NonNull @NotNull DialogAction which) {

                                    Intent iDone = new Intent(OrderReviewAndSubmitActivity.this, DealersActivity.class);
                                    iDone.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(iDone);
                                    finish();
                                }
                            });

                } else {
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Unable to place the order", Toast.LENGTH_LONG).show();
                    Intent iDone = new Intent(OrderReviewAndSubmitActivity.this, MainActivity.class);
                    iDone.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(iDone);
                    finish();
                }
            }
        });

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
        Intent iMain = new Intent(OrderReviewAndSubmitActivity.this, MainActivity.class);
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
