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

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.sales.numax.R;
import com.sales.numax.model.Product;
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

    @Override
    public void onClick(View v) {

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {


        private TextView tvProductName, tvUoM, tvPrice;
        public ImageView imgProduct;

        public MyViewHolder(View view) {
            super(view);

            tvProductName = (TextView) view.findViewById(R.id.tvProductName);
            tvUoM = (TextView) view.findViewById(R.id.tvUoM);
            tvPrice = (TextView) view.findViewById(R.id.tvPrice);
            imgProduct=(ImageView)view.findViewById(R.id.imgProduct);

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

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        final MyViewHolder itemHolder = (MyViewHolder) holder;
        final Product product = (Product) productList.get(position);
        itemHolder.tvProductName.setText(product.getProductname());
        itemHolder.tvProductName.setTag(product.getKey());
        itemHolder.tvUoM.setText(product.getUom());
        itemHolder.tvUoM.setTag(product.getCategorykey());
        itemHolder.tvPrice.setText(currencySymbol + " " + product.getPrice());

        if (product.getUrl() != null && !product.getUrl() .isEmpty() && !product.getUrl() .equals("NA")) {

            String mImageUrl = "";
            if (!TextUtils.isEmpty(product.getUrl() )) {
                mImageUrl = product.getUrl();
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
                    String sImageUri = product.getUrl();
                    Picasso.with(mContext).load(sImageUri).placeholder(R.drawable.placeholder).transform(transformation).into(holder.imgProduct);
                }
            });
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