package com.sales.numax.adapters;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.sales.numax.R;
import com.sales.numax.model.OrderLine;
import com.sales.numax.utility.RoundedCornersTransformation;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.List;

public class NewOrderAdapter  extends RecyclerView.Adapter<NewOrderAdapter.MyViewHolder> implements View.OnClickListener {
    private Context mContext;
    private Activity mActivity;
    private List<OrderLine> orderLines;
    public String currencySymbol = "₹ ";
    @Override
    public void onClick(View v) {

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tvProductName, tvQty, tvPrice,tvAmount,tvDescription;
        public ImageView imgDelete,imgProduct;

        public MyViewHolder(View view) {
            super(view);

            tvProductName = (TextView) view.findViewById(R.id.tvProductName);
            tvQty = (TextView) view.findViewById(R.id.tvQty);
            tvPrice = (TextView) view.findViewById(R.id.tvPrice);
            tvAmount = (TextView) view.findViewById(R.id.tvAmount);
            tvDescription = (TextView) view.findViewById(R.id.tvDescription);
            imgDelete=(ImageView)view.findViewById(R.id.imgDelete);
            imgProduct=(ImageView)view.findViewById(R.id.imgProduct);

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
        final OrderLine mOrderLine = (OrderLine)orderLines.get(position);
        if (mOrderLine!=null){
            itemHolder.tvProductName.setText(mOrderLine.getProductname());
            itemHolder.tvProductName.setTag(mOrderLine.getKey());
            itemHolder.tvQty.setText(String.valueOf(mOrderLine.getQty()));
            itemHolder.tvPrice.setText(currencySymbol + " " + mOrderLine.getPrice());
            itemHolder.tvAmount.setText(currencySymbol + " " + mOrderLine.getAmount());
            itemHolder.tvDescription.setText(mOrderLine.getOrderdesc());
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
