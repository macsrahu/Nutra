package com.sales.numax.activities;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.jaredrummler.materialspinner.MaterialSpinnerAdapter;
import com.sales.numax.R;
import com.sales.numax.adapters.ProductAdapter;
import com.sales.numax.common.FirebaseData;
import com.sales.numax.common.FirebaseTables;
import com.sales.numax.model.Category;
import com.sales.numax.model.Product;
import com.sales.numax.model.SubCategory;
import com.sales.numax.utility.Global;
import com.sales.numax.utility.GridSpacingItemDecoration;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.paperdb.Paper;

public class ProductsActivity extends AppCompatActivity {

    private ArrayList<Product> mProducts = new ArrayList<Product>();
    @BindView(R.id.rvProductList)
    RecyclerView rvProductList;

    @BindView(R.id.spinnerCategory)
    MaterialSpinner spinnerCategory;

    @BindView(R.id.tvNoRecordFound)
    TextView tvNoRecordFound;

    @BindView(R.id.bottomNavigation)
    BottomNavigationView bottomNavigation;

    ProductAdapter adapter;
    Toolbar mToolbarView = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.products_list);
        ButterKnife.bind(this);

        mToolbarView = Global.PrepareToolBar(this, true, "Products");
        setSupportActionBar(mToolbarView);

        InitControls();

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        Intent iMain = new Intent(ProductsActivity.this, MainActivity.class);
        iMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(iMain);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    private void InitControls() {

        if (Global.MENU_FROM.equals("ORDER")) {
            bottomNavigation.setVisibility(View.VISIBLE);
            bottomNavigation.setOnNavigationItemSelectedListener(item -> {
                switch (item.getItemId()) {

                    case R.id.btnSubmit:
                        Intent iMain = new Intent(ProductsActivity.this, NewOrderActivity.class);
                        iMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(iMain);
                        finish();
                        break;

                    case R.id.btnCancel:
                        onBackPressed();
                        break;
                }
                return true;
            });
        } else {
            bottomNavigation.setVisibility(View.GONE);
        }
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        rvProductList.setLayoutManager(mLayoutManager);
        rvProductList.addItemDecoration(new GridSpacingItemDecoration(1, Global.dpToPx(5, getApplicationContext()), false));
        rvProductList.setItemAnimator(new DefaultItemAnimator());
        LoadSubCategory();

    }

    private void LoadSubCategory() {
        FirebaseData.LoadCategory(getApplicationContext());
        if (Global.CATEGORY_LIST != null) {
            MaterialSpinnerAdapter spinnerCategoryAdapter = new MaterialSpinnerAdapter<Category>(getBaseContext(), Global.CATEGORY_LIST);
            spinnerCategory.setAdapter(spinnerCategoryAdapter);
            //spinnerCategory.setSelectedIndex(1);
            spinnerCategory.setSelected(true);
            spinnerCategory.setText("SELECT CATEGORY");
            spinnerCategory.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<Category>() {
                @Override
                public void onItemSelected(MaterialSpinner view, int position, long id, Category item) {
                    view.setSelectedIndex(position);

                    if (view != null && item != null) {
                        if (item.getKey() != null) {
                            LoadProductsByCategory(item.getKey());
                        }
                    }
                }
            });
            spinnerCategory.performClick();
        }

    }


    private void LoadProductsByCategory(final String categoryKey) {

        final ProgressDialog dialog = ProgressDialog.show(this,
                null,
                "Loading data",
                true);
        dialog.setInverseBackgroundForced(true);
        dialog.show();
        FirebaseDatabase.getInstance().getReference().child(FirebaseTables.TBL_PRODUCTS).child(categoryKey)
                .get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                dialog.dismiss();
                mProducts = new ArrayList<Product>();
                if (task.isSuccessful()) {
                    for (DataSnapshot productsSnapshot : task.getResult().getChildren()) {
                        Product product = productsSnapshot.getValue(Product.class);
                        if (product != null) {
                            if (product.getIsactive() == 1) {
                                product.setCategorykey(categoryKey);
                                product.setKey(product.getKey());
                                mProducts.add(product);
                            }
                        }
                    }
                    if (mProducts.size() > 0) {
                        adapter = new ProductAdapter(getApplicationContext(), ProductsActivity.this, mProducts);
                        rvProductList.setAdapter(adapter);
                        tvNoRecordFound.setVisibility(View.GONE);
                        rvProductList.setVisibility(View.VISIBLE);
                    } else {
                        tvNoRecordFound.setText("Product(s) not found");
                        tvNoRecordFound.setVisibility(View.VISIBLE);
                        rvProductList.setVisibility(View.GONE);
                    }
                    dialog.dismiss();
                } else {
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    Log.d("firebase", task.getException().getMessage());
                }
            }
        });
    }
}
