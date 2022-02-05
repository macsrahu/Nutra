package com.sales.numax.adapters;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.sales.numax.R;
import com.sales.numax.model.OrderLine;
import com.sales.numax.utility.Global;
import com.sales.numax.utility.Messages;
import com.sales.numax.utility.RoundedCornersTransformation;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NewOrderAdapter extends RecyclerView.Adapter<NewOrderAdapter.MyViewHolder> implements View.OnClickListener {
    private Context mContext;
    private Activity mActivity;
    private List<OrderLine> orderLines;
    public String currencySymbol = "₹ ";

    @Override
    public void onClick(View v) {

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tvProductName, tvQty, tvPrice, tvAmount, tvDescription;
        public ImageView imgDelete, imgProduct;

        public MyViewHolder(View view) {
            super(view);

            tvProductName = (TextView) view.findViewById(R.id.tvProductName);
            tvQty = (TextView) view.findViewById(R.id.tvQty);
            tvPrice = (TextView) view.findViewById(R.id.tvPrice);
            tvAmount = (TextView) view.findViewById(R.id.tvAmount);
            tvDescription = (TextView) view.findViewById(R.id.tvDescription);
            imgDelete = (ImageView) view.findViewById(R.id.imgDelete);
            imgProduct = (ImageView) view.findViewById(R.id.imgProduct);

        }
    }

    @Override
    public NewOrderAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_row, parent, false);


        return new NewOrderAdapter.MyViewHolder(itemView);
    }

    public NewOrderAdapter(Context mContext, Activity _activity, ArrayList<OrderLine> _orderLine) {
        this.mContext = mContext;
        this.orderLines = _orderLine;
        this.mActivity = _activity;
    }

    @Override
    public void onBindViewHolder(final NewOrderAdapter.MyViewHolder holder, final int position) {

        final NewOrderAdapter.MyViewHolder itemHolder = (NewOrderAdapter.MyViewHolder) holder;
        final OrderLine mOrderLine = (OrderLine) orderLines.get(position);
        if (mOrderLine != null) {
            itemHolder.tvProductName.setText(mOrderLine.getProductname());
            itemHolder.tvProductName.setTag(mOrderLine.getKey());
            itemHolder.tvQty.setText(String.valueOf(mOrderLine.getQty()));
            itemHolder.tvPrice.setText(currencySymbol + " " + mOrderLine.getPrice());
            itemHolder.tvAmount.setText(currencySymbol + " " + mOrderLine.getAmount());
            itemHolder.tvDescription.setText(mOrderLine.getOrderdesc());

            itemHolder.imgDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new MaterialDialog.Builder(mActivity)
                            .icon(mContext.getResources().getDrawable(R.mipmap.ic_launcher))
                            .title("Remove Cart Item")
                            .content("Do you want to Remove this product?")
                            .positiveText("Yes")
                            .negativeText("No")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull @NotNull MaterialDialog dialog, @NonNull @NotNull DialogAction which) {
                                    RemoveFromCart(mOrderLine.getProductkey());
                                    notifyDataSetChanged();
                                }
                            }).onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull @NotNull MaterialDialog dialog, @NonNull @NotNull DialogAction which) {
                            dialog.dismiss();
                        }
                    }).show();
                }
            });
            if (mOrderLine.getUrl() != null && !mOrderLine.getUrl().isEmpty() && !mOrderLine.getUrl().equals("NA")) {

                String mImageUrl = "";
                if (!TextUtils.isEmpty(mOrderLine.getUrl())) {
                    mImageUrl = mOrderLine.getUrl();
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
                        String sImageUri = mOrderLine.getUrl();
                        Picasso.with(mContext).load(sImageUri).placeholder(R.drawable.placeholder).transform(transformation).into(holder.imgProduct);
                    }
                });
            }
        }

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
        return orderLines.size();
    }


}
