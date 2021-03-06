package com.sales.numax.adapters;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.sales.numax.R;
import com.sales.numax.model.OrderLine;
import com.sales.numax.model.Product;
import com.sales.numax.utility.Global;
import com.sales.numax.utility.Messages;
import com.sales.numax.utility.RoundedCornersTransformation;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.MyViewHolder> implements OnClickListener {
    private Context mContext;
    private List<Product> productList;
    private ArrayList<Product> searchList;
    public String currencySymbol = "₹ ";
    private Activity mActivity;

    TextInputEditText input_quantity;
    View positiveAction;

    @Override
    public void onClick(View v) {

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {


        private TextView tvProductName, tvUoM, tvPrice, text_pockets;
        public ImageView imgProduct, imgDelete;
        public LinearLayout layItemMain;

        public MyViewHolder(View view) {
            super(view);

            tvProductName = (TextView) view.findViewById(R.id.tvProductName);
            tvUoM = (TextView) view.findViewById(R.id.tvUoM);
            tvPrice = (TextView) view.findViewById(R.id.tvPrice);
            text_pockets = (TextView) view.findViewById(R.id.text_pockets);
            imgProduct = (ImageView) view.findViewById(R.id.imgProduct);
            imgDelete = (ImageView) view.findViewById(R.id.imgDelete);
            layItemMain = (LinearLayout) view.findViewById(R.id.layItemMain);

        }
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.product_row, parent, false);


        return new MyViewHolder(itemView);
    }

    public ProductAdapter(Context mContext, Activity _activity, ArrayList<Product> _products) {
        this.mContext = mContext;
        this.productList = _products;
        this.searchList = new ArrayList<Product>();
        this.searchList.addAll(_products);
        this.mActivity = _activity;
    }

    private String GetFormatedValue(Double dlbValue) {
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        String sumAmount = decimalFormat.format(dlbValue);
        return sumAmount;
    }

    private String GetFormatedQty(Double dlbValue) {
        DecimalFormat decimalFormat = new DecimalFormat("###");
        String qty = decimalFormat.format(dlbValue);
        return qty;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {


        final MyViewHolder itemHolder = (MyViewHolder) holder;
        final Product mProduct = (Product) productList.get(position);
        itemHolder.imgDelete.setVisibility(View.GONE);
        if (Global.MENU_FROM == "ORDER") {
            itemHolder.text_pockets.setVisibility(View.VISIBLE);
            //itemHolder.imgDelete.setVisibility(View.VISIBLE);
            itemHolder.text_pockets.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ShowQtyModal(itemHolder, position, mProduct);
                }
            });
            itemHolder.layItemMain.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ShowQtyModal(itemHolder, position, mProduct);
                }
            });

        } else {
            itemHolder.text_pockets.setVisibility(View.GONE);
            itemHolder.imgDelete.setVisibility(View.GONE);
        }


        itemHolder.tvProductName.setText(mProduct.getProductname());
        itemHolder.tvProductName.setTag(mProduct.getKey());
        itemHolder.tvUoM.setText(mProduct.getUom());
        itemHolder.tvUoM.setTag(mProduct.getCategorykey());
        itemHolder.tvPrice.setText(currencySymbol + " " + mProduct.getPrice());

        if (Global.ORDER_LINE != null) {
            itemHolder.text_pockets.setText(GetQuantity(mProduct.getKey()));
        }


//        itemHolder.layItemMain.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ShowQtyModal(itemHolder, position, mProduct);
//            }
//        });


        if (mProduct.getUrl() != null && !mProduct.getUrl().isEmpty() && !mProduct.getUrl().equals("NA")) {
            String mImageUrl = "";
            if (!TextUtils.isEmpty(mProduct.getUrl())) {
                mImageUrl = mProduct.getUrl();
            }

            final int radius = 5;
            final int margin = 5;
            final Transformation transformation = new RoundedCornersTransformation(radius, margin);
            Picasso.with(mContext).load(mImageUrl).placeholder(R.drawable.placeholder).transform(transformation).networkPolicy(NetworkPolicy.OFFLINE).into(holder.imgProduct, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    String sImageUri = mProduct.getUrl();
                    Picasso.with(mContext).load(sImageUri).placeholder(R.drawable.placeholder).transform(transformation).into(holder.imgProduct);
                }
            });
        }
    }

    private String GetQuantity(String mProdKey) {
        for (int i = 0; i < Global.ORDER_LINE.size(); i++) {
            if (Global.ORDER_LINE.get(i).getProductkey().equals(mProdKey)) {
                return GetFormatedQty(Global.ORDER_LINE.get(i).getQty());
            }
        }
        return "0";
    }

    private void ShowQtyModal(MyViewHolder itemHolder, int position, final Product mProduct) {

        final MaterialDialog dialogQty = new MaterialDialog.Builder(mActivity)
                .title("Enter Quantity")
                .autoDismiss(true)
                .customView(R.layout.dialog_quantity, true)
                .positiveText("OK")
                .negativeText(android.R.string.cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        notifyItemChanged(position);
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (!input_quantity.getText().toString().isEmpty()) {
                            if (!input_quantity.getText().toString().equalsIgnoreCase("0")) {
                                int Quantity = 1;
                                double dblQty = Double.parseDouble(input_quantity.getText().toString());
                                double dblAmt = dblQty * mProduct.getPrice();
                                itemHolder.text_pockets.setText(input_quantity.getText().toString());

                                OrderLine mOrderLine = new OrderLine();
                                mOrderLine.setProductkey(mProduct.getKey());
                                mOrderLine.setProductname(mProduct.getProductname());
                                mOrderLine.setPrice(mProduct.getPrice());
                                mOrderLine.setAmount(dblAmt);
                                mOrderLine.setUrl(mProduct.getUrl());
                                mOrderLine.setQty(dblQty);
                                mOrderLine.setUom(mProduct.getUom());
                                mOrderLine.setOrderkey("NA");
                                mOrderLine.setKey("NA");
                                String sAmount = Global.GetFormatedValue(mProduct.getPrice() * dblQty);
                                String sDesc = String.valueOf(Integer.parseInt(input_quantity.getText().toString())) + " pocket(s) of " + mProduct.getUom() + " and amount is " + sAmount;
                                mOrderLine.setOrderdesc(sDesc);

                                mOrderLine.setAmount(mProduct.getPrice() * dblQty);
                                AddToCart(mOrderLine);
                                notifyDataSetChanged();
                            } else {
                                RemoveFromCart(mProduct.getKey());
                                notifyDataSetChanged();
                            }
                        } else {
                            RemoveFromCart(mProduct.getKey());
                            notifyDataSetChanged();
                        }

                    }
                }).build();
        dialogQty.show();

        positiveAction = dialogQty.getActionButton(DialogAction.POSITIVE);
        input_quantity = (TextInputEditText) dialogQty.findViewById(R.id.input_quantity);
        if (!itemHolder.text_pockets.getText().toString().isEmpty()) {
            input_quantity.setText(itemHolder.text_pockets.getText());
        }else{
            input_quantity.setText("");
        }
        input_quantity.selectAll();
        input_quantity.requestFocus();


    }

    private void AddToCart(OrderLine mOrderLine) {

        if (mOrderLine != null) {
            if (Global.ORDER_LINE == null) {
                Global.ORDER_LINE = new ArrayList<OrderLine>();
            }
            boolean isFound = false;
            for (int i = 0; i < Global.ORDER_LINE.size(); i++) {
                if (Global.ORDER_LINE.get(i).getProductkey().equals(mOrderLine.getProductkey())) {
                    isFound = true;
                    Global.ORDER_LINE.get(i).setQty(mOrderLine.getQty());
                    Global.ORDER_LINE.get(i).setUom(mOrderLine.getUom());
                    Global.ORDER_LINE.get(i).setAmount(mOrderLine.getAmount());
                    break;
                }
            }
            if (!isFound) {
                Global.ORDER_LINE.add(mOrderLine);
            }
        }
    }

    private void RemoveFromCart(String sProductKey) {
        if (Global.ORDER_LINE != null && Global.ORDER_LINE.size() > 0) {
            for (int i = 0; i < Global.ORDER_LINE.size(); i++) {
                if (Global.ORDER_LINE.get(i).getProductkey().equals(sProductKey)) {
                    Global.ORDER_LINE.remove(i);
                }
            }
        }
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void filter(String charText) {
        if (charText != null) {
            charText = charText.toLowerCase(Locale.getDefault());
            //Toast.makeText(mContext, charText, Toast.LENGTH_SHORT).show();
            productList.clear();
            if (charText.length() == 0) {
                productList.addAll(searchList);
            } else {
                int textLength = charText.length();
                for (Product s : searchList) {
                    if (!TextUtils.isEmpty(s.getProductname())) {
                        if (s.getProductname().toLowerCase(Locale.getDefault()).contains(charText)) {
                            productList.add(s);
                        }
                    }
                }
            }
            notifyDataSetChanged();
        }
    }
}