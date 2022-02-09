package com.sales.numax.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.text.format.DateFormat;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.sales.numax.R;
import com.sales.numax.activities.MyOrdersListActivity;
import com.sales.numax.common.FirebaseTables;
import com.sales.numax.model.OrderLine;
import com.sales.numax.model.OrderMain;
import com.sales.numax.printing.Printer;
import com.sales.numax.printing.bluetooth.BluetoothPrinters;
import com.sales.numax.printing.textparser.PrinterTextParserImg;
import com.sales.numax.utility.Global;


import java.util.ArrayList;
import java.util.List;


public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.MyViewHolder> {
    private Context mContext;
    Activity mActivity;
    LayoutInflater inflater;
    private List<OrderMain> orderList;
    private ArrayList<OrderMain> arrayList;
    public StringBuilder stringBuilder = new StringBuilder();
    public TextView text_view_preview;
    public Button button_new_print;
    private ArrayList<OrderLine> mOrderLineItems = new ArrayList<OrderLine>();

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView tvOrderNo, tvOrderDate, tvOrderStatus, tvOrderAddress, tvOrderAmount;
        public MaterialCardView cardOrderRow;

        public ImageView btnPrinter;
        public View lineView;

        public MyViewHolder(View view) {
            super(view);

            tvOrderNo = (TextView) view.findViewById(R.id.tvOrderNo);
            tvOrderDate = (TextView) view.findViewById(R.id.tvOrderDate);

            tvOrderAddress = (TextView) view.findViewById(R.id.tvOrderAddress);
            //tvOrderStatus = (TextView) view.findViewById(R.id.tvOrderStatus);
            tvOrderAmount = (TextView) view.findViewById(R.id.tvOrderAmount);
            cardOrderRow = (MaterialCardView) view.findViewById(R.id.cardOrderRow);
            lineView = (View) view.findViewById(R.id.lineView);

            btnPrinter = (ImageView) view.findViewById(R.id.btnPrinter);
            cardOrderRow.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            OrderMain mOrderMain = orderList.get(getAdapterPosition());
            if (mOrderMain != null) {
                Global.SELECTED_ORDER_MAIN = mOrderMain;
                //Intent iOrderItem = new Intent(mContext, MyOrderDetail.class);
                //iOrderItem.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //mContext.startActivity(iOrderItem);
            }
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.orders_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    public OrdersAdapter(Context mContext, Activity activity, List<OrderMain> _ordersList) {
        this.mContext = mContext;
        this.orderList = _ordersList;
        this.arrayList = new ArrayList<OrderMain>();
        this.arrayList.addAll(orderList);
        this.mActivity = activity;
    }


    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final OrderMain ordersMain = orderList.get(position);
        holder.tvOrderAddress.setText(ordersMain.getDealer() + "\n" + ordersMain.getAddress());

        holder.tvOrderNo.setText(ordersMain.getOrderno());
        holder.tvOrderDate.setText("Date: " + DateFormat.format("dd/MM/yyyy (HH:mm aa)",
                ordersMain.getOrderdatestamp()));
        holder.tvOrderAmount.setText("Amount : " + Global.GetFormatedValueWithoutDecimal(ordersMain.getOrderamount()));
        holder.btnPrinter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Global.SELECTED_ORDER_MAIN = ordersMain;
                if (ordersMain != null) {
                    LoadPrintPreview(ordersMain);
                }
//                if (Global.SELECTED_ORDER_MAIN != null) {
//                    Intent iPrintView = new Intent(mContext, PrintOrder.class);
//                    iPrintView.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    mContext.startActivity(iPrintView);
//                } else {
//                    Toast.makeText(mActivity, "Order detail not selected", Toast.LENGTH_LONG).show();
//                }
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }


    private String GetCenteredText(String str, int size) {
        int left = (size - str.length()) / 2;
        int right = size - left - str.length();
        String repeatedChar = " ";
        StringBuffer buff = new StringBuffer();
        for (int i = 0; i < left; i++) {
            buff.append(repeatedChar);
        }
        buff.append(str);
        for (int i = 0; i < right; i++) {
            buff.append(repeatedChar);
        }
        // to see the end (and debug) if using spaces as repeatedChar
        //buff.append("$");
        return buff.toString(); //System.out.println(buff.toString());
    }


    private void LoadPrintPreview(final OrderMain ordersMain) {

        final MaterialDialog dialogPrintPreview = new MaterialDialog.Builder(mActivity)
                .autoDismiss(true)
                .title("Print Preview")
                .customView(R.layout.dialog_print_preview, true)
                .contentGravity(GravityEnum.CENTER)
                .positiveText("PRINT").onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        try {
                            String msg = stringBuilder.toString();
                            if (!msg.isEmpty()) {
                                if (mActivity instanceof MyOrdersListActivity) {

                                    double totalQty = 0;
                                    String sLineItems = "";
                                    for (int i = 0; i < mOrderLineItems.size(); i++) {
                                        sLineItems = sLineItems + "[L]" + mOrderLineItems.get(i).getProductname() + "     [R]" + String.valueOf(mOrderLineItems.get(i).getQty()) +
                                                "[L]\n" +
                                                "[L]+" + mOrderLineItems.get(i).getUom().substring(0, 10).toString() + "   [R]Rs." + mOrderLineItems.get(i).getPrice() + " Amt:" + mOrderLineItems.get(i).getAmount() +
                                                "[L]\n" +
                                                "[L]\n";
                                        totalQty = totalQty + mOrderLineItems.get(i).getQty();
                                    }

                                    Printer printer = new Printer(BluetoothPrinters.selectFirstPairedBluetoothPrinter(), 203, 48f, 32);
//                                    printer.printFormattedText(
//                                                     //"[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, mActivity.getApplicationContext().getResources().getDrawableForDensity(R.drawable.logo, DisplayMetrics.DENSITY_DEFAULT)) + "</img>\n" +
//                                                    "[L]\n" +
//                                                    "[C]" + Global.COMPANY.getName().toUpperCase() +
//                                                    "[L]\n" +
//                                                    "[C]" + Global.COMPANY.getGst() + "\n" +
//                                                    "[L]\n" +
//                                                    "[L]\n" +
//                                                    "[L]" + ordersMain.getDealer() +
//                                                    "[L]\n" +
//                                                    "[L]" + ordersMain.getAddress() +
//                                                    "[L]\n" +
//                                                    "[L]<b>" + ordersMain.getOrderno() + "         [R]" + ordersMain.getOrderdate() + "</b>" +
//                                                    "[L]\n" +
//                                                    "[C]================================\n" +
//                                                    "[L]" + sLineItems +
//                                                    "[C]================================" +
//                                                    "[L]\n" +
//                                                    "[R]<b>ORDER AMOUNT:</b> [R]" + String.valueOf(totalQty) +
//                                                    "[L]\n" +
//                                                    "[C]================================"
//                                    );

                                    //dialog.dismiss();
                                }
                            } else {
                                Toast.makeText(mContext, "Detail not found to print!!!", Toast.LENGTH_LONG).show();
                            }

                        } catch (Exception ex) {
                            Toast.makeText(mContext, ex.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .negativeText("CLOSE")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .build();
        dialogPrintPreview.show();


        text_view_preview = (TextView) dialogPrintPreview.getCustomView().findViewById(R.id.text_view_preview);
        text_view_preview.setMovementMethod(new ScrollingMovementMethod());
        button_new_print = (Button) dialogPrintPreview.getCustomView().findViewById(R.id.button_new_print);
        button_new_print.setVisibility(View.GONE);
        button_new_print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        DatabaseReference mDataReference = FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_ORDERS_LINE_ITEMS);
        mDataReference.child(ordersMain.getKey()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                if (dataSnapshot.exists()) {
                    mOrderLineItems = new ArrayList<OrderLine>();
                    for (DataSnapshot orderItemSnapshot : dataSnapshot.getChildren()) {
                        OrderLine ordersLineItems = orderItemSnapshot.getValue(OrderLine.class);
                        if (ordersLineItems != null) {
                            mOrderLineItems.add(ordersLineItems);
                        }
                    }

                    if (mOrderLineItems.size() > 0) {
                        int iCharCount = 32;
                        double totalQty = 0;
                        stringBuilder = new StringBuilder();

                        if (Global.COMPANY != null) {
                            stringBuilder.append("[L]\n" + GetCenteredText(Global.COMPANY.getName().toUpperCase(), iCharCount));
                            stringBuilder.append(GetCenteredText(Global.COMPANY.getGst().toUpperCase(), iCharCount));
                        }
                        stringBuilder.append("[C]================================\n");
                        stringBuilder.append("[C]<u><font size='big'>ORDER " + GetCenteredText("ORDER", iCharCount) + "</font></u>\n");
                        stringBuilder.append("[L]\n");
                        stringBuilder.append("[L]<b>" + ordersMain.getOrderno() + "         " + ordersMain.getOrderdate() + "</b>s\n"); //10 Space between order no and date
                        stringBuilder.append("[L]\n");
                        stringBuilder.append("[L]<b>Dealer:" + ordersMain.getDealer() + "\n");
                        stringBuilder.append("[L]" + ordersMain.getAddress() + "\n");
                        stringBuilder.append("[C]================================\n");
                        stringBuilder.append("\n");
                        for (int i = 0; i < mOrderLineItems.size(); i++) {
                            stringBuilder.append("[L]" + mOrderLineItems.get(i).getProductname() + "[R]" + String.valueOf(mOrderLineItems.get(i).getQty()) + "\n");
                            stringBuilder.append("[L]   + " + String.valueOf(mOrderLineItems.get(i).getUom()) + "[R]" + mOrderLineItems.get(i).getPrice() + "\n\n");
                            totalQty = totalQty + mOrderLineItems.get(i).getQty();
                        }
                        stringBuilder.append("[C]================================\n");
                        stringBuilder.append("\n");
                        stringBuilder.append("[L]<b>NET TOTAL</b> [R]" + String.valueOf(totalQty) + "\n");
                        stringBuilder.append(String.valueOf(totalQty));
                        text_view_preview.setText(stringBuilder.toString());
                        /// LoadReturnItems(ordersMain);

                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

//    private void LoadReturnItems(OrderMain ordersMain) {
//
//
//        DatabaseReference mOrderReturn = FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_ORDERS_RETURN_LINE_ITEMS);
//        mOrderReturn.child(ordersMain.getKey()).addValueEventListener(new ValueEventListener() {
//            @Overrides
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                ArrayList<OrdersLineItems> mOrderLineItems = new ArrayList<OrdersLineItems>();
//
//                if (dataSnapshot.exists()) {
//
//                    for (DataSnapshot orderItemSnapshot : dataSnapshot.getChildren()) {
//                        OrdersLineItems ordersLineItems = orderItemSnapshot.getValue(OrdersLineItems.class);
//                        if (ordersLineItems != null) {
//                            mOrderLineItems.add(ordersLineItems);
//                        }
//                    }
//                    if (mOrderLineItems.size() > 0) {
//                        int iCharCount = 32;
//                        double totalQty = 0;
//
//                        stringBuilder.append("\n");
//                        stringBuilder.append(GetCenteredText("RETURN ITEM", iCharCount));
//                        stringBuilder.append("\n");
//                        for (int j = 0; j < iCharCount; j++) {
//                            stringBuilder.append("-");
//                        }
//                        stringBuilder.append("\n");
//                        stringBuilder.append("Product                     Qty");
//                        stringBuilder.append("\n");
//                        for (int j = 0; j < iCharCount; j++) {
//                            stringBuilder.append("-");
//                        }
//                        stringBuilder.append("\n");
//                        for (int i = 0; i < mOrderLineItems.size(); i++) {
//                            stringBuilder.append(mOrderLineItems.get(i).getProductname() + "\n");
//                            stringBuilder.append(String.valueOf(mOrderLineItems.get(i).getUom()));
//                            if (String.valueOf(mOrderLineItems.get(i).getUom()).length() <= 28) {
//                                for (int l = 0; l < (28 - String.valueOf(mOrderLineItems.get(i).getUom()).length()); l++) {
//                                    stringBuilder.append(" ");
//                                }
//                            }
//                            if (String.valueOf(mOrderLineItems.get(i).getQty()).length() <= 4) {
//                                for (int l = 0; l < (4 - String.valueOf(mOrderLineItems.get(i).getUom()).length()); l++) {
//                                    stringBuilder.append(" ");
//                                }
//                            }
//                            stringBuilder.append(String.valueOf(mOrderLineItems.get(i).getQty()));
//                            stringBuilder.append("\n");
//
//                            totalQty = totalQty + mOrderLineItems.get(i).getQty();
//
//                            totalQty = totalQty + mOrderLineItems.get(i).getQty();
//                        }
//                        for (int j = 0; j < iCharCount; j++) {
//                            stringBuilder.append("-");
//                        }
//                        stringBuilder.append("\n");
//                        stringBuilder.append("NET TOTAL                  ");
//                        stringBuilder.append(String.valueOf(totalQty));
//                        // stringBuilder.append(Global.GetFormatedAmount(String.valueOf(Global.SELECTED_ORDER.getNetamount())));
//                        stringBuilder.append("\n");
//                        for (int j = 0; j < iCharCount; j++) {
//                            stringBuilder.append("-");
//                        }
//                        stringBuilder.append("\n");
//                        // stringBuilder.append(GetCenteredText("Thank you!!", iCharCount));
//                        for (int j = 0; j < 5; j++) {
//                            stringBuilder.append("\n");
//                        }
//                        text_view_preview.setText(stringBuilder.toString());
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
//
//    }
}





