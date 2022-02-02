package com.sales.numax.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.sales.numax.R;
import com.sales.numax.activities.DealerEntry;
import com.sales.numax.activities.DealersActivity;
import com.sales.numax.activities.NewOrderActivity;
import com.sales.numax.activities.ProductsActivity;
import com.sales.numax.model.ApplicationMenu;
import com.sales.numax.model.Category;
import com.sales.numax.model.SalesAbstract;

import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MyViewHolder> {
    private Context mContext;
    private Activity mActivity;
    private List<ApplicationMenu> applicationMenuList;


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView tvTitle, tvDescription;
        public ImageView imgMenu;
        public MaterialCardView carMenuItem;

        public MyViewHolder(View view) {
            super(view);
            tvTitle = (TextView) view.findViewById(R.id.tvTitle);
            tvDescription = (TextView) view.findViewById(R.id.tvDescription);
            imgMenu = (ImageView) view.findViewById(R.id.imgMenu);
            carMenuItem = (MaterialCardView) view.findViewById(R.id.cardMenu);
            carMenuItem.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            //Toast.makeText(mContext, "Clicked", Toast.LENGTH_LONG).show();
            final ApplicationMenu applicationMenu = applicationMenuList.get(getAdapterPosition());
            if (applicationMenu != null) {
                if (applicationMenu.getCode().equals("001")) {
                    Intent iDealers = new Intent(mActivity, DealersActivity.class);
                    iDealers.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    mContext.startActivity(iDealers);
                } else if (applicationMenu.getCode().equals("002")) {
                    Intent iProducts = new Intent(mActivity, ProductsActivity.class);
                    iProducts.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    mContext.startActivity(iProducts);
                } else if (applicationMenu.getCode().equals("003")) {
                    Intent iDealerEntry = new Intent(mActivity, DealerEntry.class);
                    iDealerEntry.putExtra("FROM", "MAIN");
                    iDealerEntry.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    mContext.startActivity(iDealerEntry);

                } else if (applicationMenu.getCode().equals("004")) {

                } else if (applicationMenu.getCode().equals("005")) {
                }
            }
        }

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.menu_row, parent, false);

        return new MyViewHolder(itemView);
    }

    public MenuAdapter(Context mContext, Activity activity, List<ApplicationMenu> _applicationMenu) {
        this.mContext = mContext;
        this.mActivity = activity;
        this.applicationMenuList = _applicationMenu;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final ApplicationMenu applicationMenu = applicationMenuList.get(position);
        if (applicationMenu != null) {
            holder.tvTitle.setText(applicationMenu.getTitle());
            holder.tvDescription.setText(applicationMenu.getDescription());
            if (applicationMenu.getImage()!=null){
                holder.imgMenu.setImageDrawable(applicationMenu.getImage());
            }

        }
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return applicationMenuList.size();
    }

}