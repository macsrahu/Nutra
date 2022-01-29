package com.sales.numax.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.sales.numax.R;
import com.sales.numax.activities.ProductsActivity;
import com.sales.numax.model.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.MyViewHolder> {
    private Context mContext;
    private Activity mActivity;
    private List<Category> categoryList;


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView tvCategoryName, tvBrands;
        public LinearLayout layCategoryItem;
        public CardView cardCategoryItem;
        public MyViewHolder(View view) {
            super(view);
            tvCategoryName = (TextView) view.findViewById(R.id.tvCategoryName);
            tvBrands = (TextView) view.findViewById(R.id.tvBrands);
            //layCategoryItem = (LinearLayout) view.findViewById(R.id.layCategoryItem);
            cardCategoryItem = (CardView) view.findViewById(R.id.cardCategoryItem);
            cardCategoryItem.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            final Category category = categoryList.get(getAdapterPosition());
            if (category != null) {
                Intent iProductCate = new Intent(mActivity, ProductsActivity.class);
                iProductCate.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mContext.startActivity(iProductCate);
            }
        }

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.product_category_items, parent, false);

        return new MyViewHolder(itemView);
    }

    public CategoryAdapter(Context mContext, Activity activity, List<Category> _CategoryList) {
        this.mContext = mContext;
        this.mActivity = activity;
        this.categoryList = _CategoryList;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final Category category = categoryList.get(position);
        if (category != null) {
            holder.tvCategoryName.setText(category.getCategoryname());
        }
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

}