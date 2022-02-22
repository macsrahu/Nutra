package com.sales.numax.adapters;

import android.content.Context;
import android.content.Intent;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sales.numax.R;
import com.sales.numax.activities.DealerEntry;
import com.sales.numax.activities.NewOrderActivity;
import com.sales.numax.model.Dealer;
import com.sales.numax.model.OrderLine;
import com.sales.numax.utility.Global;
import com.sales.numax.utility.RoundedCornersTransformation;
import com.skydoves.powermenu.CustomPowerMenu;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.viethoa.RecyclerViewFastScroller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class DealerAdapter extends RecyclerView.Adapter<DealerAdapter.MyViewHolder>{
    private Context mContext;
    private List<Dealer> dealerList;
    private ArrayList<Dealer> searchList;
    CustomPowerMenu customPowerMenu;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvShop, tvContact, tvAddress, tvCode, tvEmail, tvDealerName;
        public info.androidhive.fontawesome.FontTextView tvMenu;
        public AppCompatImageView imgPicture;
        public LinearLayout layDefault;
        public CardView cardView;
        public ImageButton menu_edit;


        public MyViewHolder(View view) {
            super(view);
            layDefault = (LinearLayout) view.findViewById(R.id.layDefaultMore);
            tvShop = (TextView) view.findViewById(R.id.tvShop);
            tvEmail = (TextView) view.findViewById(R.id.tvEmail);
            tvDealerName = (TextView) view.findViewById(R.id.tvDealerName);
            tvContact = (TextView) view.findViewById(R.id.tvContact);
            tvAddress = (TextView) view.findViewById(R.id.tvAddress);
            cardView = (CardView) view.findViewById(R.id.card_viewMore);
            imgPicture = (AppCompatImageView) view.findViewById(R.id.imgPicture);
            menu_edit = (ImageButton) view.findViewById(R.id.menu_edit);
        }

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dealer_row, parent, false);

        return new MyViewHolder(itemView);
    }

    public DealerAdapter(Context mContext, List<Dealer> _dealerList) {
        this.mContext = mContext;
        this.dealerList = _dealerList;
        this.searchList = new ArrayList<Dealer>();
        this.searchList.addAll(_dealerList);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final Dealer dealer = dealerList.get(position);

        if (!dealer.getShop().isEmpty()) {
            holder.tvShop.setText(dealer.getShop());
        }
        if (!dealer.getDealername().isEmpty()) {
            holder.tvDealerName.setText(dealer.getDealername());
        }
        String sAddress = "";
        if (dealer.getAddressline1() != null) {
            sAddress = dealer.getAddressline1() + "\n";
        }
        if (dealer.getAddressline2() != null) {
            sAddress = sAddress + (!TextUtils.isEmpty(sAddress) ? dealer.getAddressline2() + "\n" : "");
        }
        if (dealer.getTown() != null) {
            sAddress = sAddress + (!TextUtils.isEmpty(sAddress) ? dealer.getTown() + "\n" : "");
        }
        if (dealer.getCity() != null) {
            sAddress = sAddress + (!TextUtils.isEmpty(sAddress) ? dealer.getCity() : "");
        }
        if (dealer.getPincode() != null) {
            sAddress = sAddress + "-" + (!TextUtils.isEmpty(sAddress) ? dealer.getPincode() : "");
        }

        holder.tvAddress.setText(sAddress);


        if (dealer.getMobile() != null && !dealer.getMobile().isEmpty()) {
            holder.tvContact.setText("☎: " + dealer.getMobile());
        } else {
            holder.tvContact.setText("☎: NA");
        }
        if (dealer.getEmail() != null && !dealer.getEmail().isEmpty()) {
            holder.tvEmail.setText("✉ : " + dealer.getEmail());
        } else {
            holder.tvEmail.setText("✉ : NA");
        }

        customPowerMenu = new CustomPowerMenu.Builder<>(mContext, new IconMenuAdapter())
                .addItem(new IconPowerMenuItem(ContextCompat.getDrawable(mContext, R.drawable.save), "Update Profile"))
                .addItem(new IconPowerMenuItem(ContextCompat.getDrawable(mContext, R.drawable.list), "My Orders"))
                .addItem(new IconPowerMenuItem(ContextCompat.getDrawable(mContext, R.drawable.invoice), "New Order"))
                .setOnMenuItemClickListener(onIconMenuItemClickListener)
                .setAnimation(MenuAnimation.SHOWUP_TOP_RIGHT)
                .setMenuRadius(10f)
                .setMenuShadow(10f)
                .build();
        holder.menu_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Global.SELECTED_DEALER = dealer;
                customPowerMenu.showAsAnchorRightBottom(v);
            }
        });

        if (dealer.getUrl() != null && !dealer.getUrl().isEmpty() && !dealer.getUrl().equals("NA")) {
            String mImageUrl = "";
            if (!TextUtils.isEmpty(dealer.getUrl())) {
                mImageUrl = dealer.getUrl();
            }

            final int radius = 5;
            final int margin = 5;
            final Transformation transformation = new RoundedCornersTransformation(radius, margin);
            Picasso.with(mContext).load(mImageUrl).placeholder(R.drawable.booth).transform(transformation).networkPolicy(NetworkPolicy.OFFLINE).into(holder.imgPicture, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    String sImageUri = dealer.getUrl();
                    Picasso.with(mContext).load(sImageUri).placeholder(R.drawable.booth).transform(transformation).into(holder.imgPicture);
                }
            });
        }

    }

    private OnMenuItemClickListener<IconPowerMenuItem> onIconMenuItemClickListener = new OnMenuItemClickListener<IconPowerMenuItem>() {
        @Override
        public void onItemClick(int position, IconPowerMenuItem item) {
            customPowerMenu.dismiss();

            if (item.getTitle() == "Update Profile") {
                if (Global.SELECTED_DEALER != null) {
                    Intent iDeliveryEntry = new Intent(mContext, DealerEntry.class);
                    iDeliveryEntry.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(iDeliveryEntry);
                }
            }
            else if (item.getTitle().equals("New Order")) {
                if (Global.SELECTED_DEALER != null) {
                    Global.ORDER_LINE=new ArrayList<OrderLine>();
                    Global.ROUTE_KEY = Global.SELECTED_DEALER.getRoutekey();
                    Intent iDep = new Intent(mContext, NewOrderActivity.class);
                    iDep.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(iDep);
                }
            }
//            } else if (item.getTitle().equals("My Receipts")) {
//                if (Global.SELECTED_DONOR_MODEL != null) {
//                    Global.DONOR_KEY = Global.SELECTED_DONOR_MODEL.getKey();
//                    Intent iDep = new Intent(mContext, ReceiptsList.class);
//                    iDep.putExtra("FROM", "DONOR_LIST");
//                    iDep.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    mContext.startActivity(iDep);
//                }
//            }
        }
    };

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return dealerList.size();
    }

    public void filter(String charText) {
        if (charText != null) {
            charText = charText.toLowerCase(Locale.getDefault());
            dealerList.clear();
            if (charText.length() == 0) {
                dealerList.addAll(searchList);
            } else {
                for (Dealer s : searchList) {
                    if (!TextUtils.isEmpty(s.getShop())) {
                        if (s.getShop().toLowerCase(Locale.getDefault()).contains(charText)
                                || s.getMobile().toLowerCase(Locale.getDefault()).contains(charText)
                                || s.getTown().toLowerCase(Locale.getDefault()).contains(charText)
                        ) {
                            dealerList.add(s);
                        }
                    }
                }
            }
            notifyDataSetChanged();
        }
    }
}


