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
import com.sales.numax.utility.RoundedCornersTransformation;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NewOrderReviewAdapter extends RecyclerView.Adapter<NewOrderReviewAdapter.MyViewHolder> implements View.OnClickListener {
    private Context mContext;
    private Activity mActivity;
    private List<OrderLine> orderLines;
    public String currencySymbol = "₹ ";

    @Override
    public void onClick(View v) {

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tvProductName, tvQty, tvPrice, tvAmount;
        public MyViewHolder(View view) {
            super(view);

            tvProductName = (TextView) view.findViewById(R.id.tvProductName);
            tvQty = (TextView) view.findViewById(R.id.tvQty);
            tvPrice = (TextView) view.findViewById(R.id.tvPrice);
            tvAmount = (TextView) view.findViewById(R.id.tvAmount);
        }
    }

    @Override
    public NewOrderReviewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_review_row, parent, false);


        return new NewOrderReviewAdapter.MyViewHolder(itemView);
    }

    public NewOrderReviewAdapter(Context mContext, Activity _activity, ArrayList<OrderLine> _orderLine) {
        this.mContext = mContext;
        this.orderLines = _orderLine;
        this.mActivity = _activity;
    }

    @Override
    public void onBindViewHolder(final NewOrderReviewAdapter.MyViewHolder holder, final int position) {

        final NewOrderReviewAdapter.MyViewHolder itemHolder = (NewOrderReviewAdapter.MyViewHolder) holder;
        final OrderLine mOrderLine = (OrderLine) orderLines.get(position);
        if (mOrderLine != null) {
            itemHolder.tvProductName.setText(mOrderLine.getProductname());
            itemHolder.tvProductName.setTag(mOrderLine.getKey());
            itemHolder.tvQty.setText(Global.GetFormatedValueWithoutDecimal(mOrderLine.getQty()));
            itemHolder.tvPrice.setText(currencySymbol + " " + mOrderLine.getPrice());
            itemHolder.tvAmount.setText(currencySymbol + " " + mOrderLine.getAmount());
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
