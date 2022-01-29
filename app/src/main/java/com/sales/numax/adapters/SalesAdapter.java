package com.sales.numax.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.sales.numax.R;
import com.sales.numax.activities.ProductsActivity;
import com.sales.numax.model.Category;
import com.sales.numax.model.SalesAbstract;

import java.util.List;

public class SalesAdapter extends RecyclerView.Adapter<SalesAdapter.MyViewHolder> {
    private Context mContext;
    private Activity mActivity;
    private List<SalesAbstract> salesAbstractList;


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView tvSalesDate;
        public WebView wvSummary;
        public LinearLayout layCategoryItem;
        public CardView cardCategoryItem;

        public MyViewHolder(View view) {
            super(view);
            tvSalesDate = (TextView) view.findViewById(R.id.tvSalesDate);
            wvSummary = (WebView) view.findViewById(R.id.wvSummary);
            cardCategoryItem = (CardView) view.findViewById(R.id.cardCategoryItem);
            //cardCategoryItem.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            final SalesAbstract salesAbstract = salesAbstractList.get(getAdapterPosition());
            if (salesAbstract != null) {
                Toast.makeText(mContext, "Clicked", Toast.LENGTH_LONG).show();
//                Intent iProductCate = new Intent(mActivity, ProductsActivity.class);
//                iProductCate.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                mContext.startActivity(iProductCate);
            }
        }

    }

    @Override
    public SalesAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sales_abstract_row, parent, false);

        return new SalesAdapter.MyViewHolder(itemView);
    }

    public SalesAdapter(Context mContext, Activity activity, List<SalesAbstract> _salesAbstractList) {
        this.mContext = mContext;
        this.mActivity = activity;
        this.salesAbstractList = _salesAbstractList;
    }

    @Override
    public void onBindViewHolder(final SalesAdapter.MyViewHolder holder, final int position) {
        final SalesAbstract salesAbstract = salesAbstractList.get(position);
        if (salesAbstract != null) {
            holder.tvSalesDate.setText(salesAbstract.getSalesdate());
            holder.wvSummary.getSettings().setJavaScriptEnabled(true);
            holder.wvSummary.loadData(salesAbstract.getProductname(), "text/html; charset=utf-8", "UTF-8");
        }
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return salesAbstractList.size();

    }
}
