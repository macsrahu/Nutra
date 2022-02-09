package com.sales.numax.common;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sales.numax.activities.LoginActivity;
import com.sales.numax.activities.MainActivity;
import com.sales.numax.model.Category;
import com.sales.numax.model.Company;
import com.sales.numax.model.Route;
import com.sales.numax.model.UserDetail;
import com.sales.numax.utility.Global;
import com.sales.numax.utility.Messages;

import java.util.ArrayList;

import io.paperdb.Paper;

public class FirebaseData {

    public static void LoadCategory(Context mContext) {

        Global.CATEGORY_LIST = Paper.book().read("categorylist");

        //if (Global.CATEGORY_LIST == null || Global.CATEGORY_LIST.size() == 0) {
        Global.CATEGORY_LIST = new ArrayList<Category>();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(FirebaseTables.TBL_CATEGORY).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                //dialog.dismiss();

                if (task.isSuccessful()) {
//                    Category mCategory= new Category();
//                    mCategory.setCategoryname("ALL CATEGORY");
//                    mCategory.setKey("ALL");
//                    mCategory.setIsactive(1);
//                    Global.CATEGORY_LIST.add(0, mCategory);
                    for (DataSnapshot categorySnapshot : task.getResult().getChildren()) {
                        Category category = categorySnapshot.getValue(Category.class);
                        if (category != null) {
                            if (category.getIsactive() == 1) {
                                category.setKey(categorySnapshot.getKey());
                                category.setCategoryname(category.getCategoryname().toUpperCase());
                                Global.CATEGORY_LIST.add(category);
                            }
                        }
                    }
                    Paper.book().write("categorylist", Global.CATEGORY_LIST);
                } else {
                    //dialog.dismiss();
                    Toast.makeText(mContext, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    Log.d("firebase", task.getException().getMessage());
                }
            }
        });
    }


    public static void LoadRoutes(Context mContext) {

        Global.ROUTES = Paper.book().read("routes");
        if (Global.ROUTES == null) {

            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_ROUTE);
            mDatabase.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    //dialog.dismiss();

                    if (task.isSuccessful()) {
                        Global.ROUTES = new ArrayList<Route>();
                        for (DataSnapshot categorySnapshot : task.getResult().getChildren()) {
                            Route route = categorySnapshot.getValue(Route.class);
                            if (route != null) {
                                if (route.getIsactive() == 1) {
                                    route.setKey(categorySnapshot.getKey());
                                    route.setRoute(route.getRoute().toUpperCase());
                                    Global.ROUTES.add(route);
                                }
                            }
                        }
                        if (Global.ROUTES != null) {
                            Paper.book().write("routes", Global.ROUTES);
                        }
                    } else {
                        //dialog.dismiss();
                        Toast.makeText(mContext, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        Log.d("firebase", task.getException().getMessage());
                    }
                }
            });
        }
    }

    public static void LoadCompany(Context mContext) {

        Global.COMPANY = Paper.book().read("company");
        if (Global.COMPANY == null) {

            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_COMPANY);
            mDatabase.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    //dialog.dismiss();

                    if (task.isSuccessful()) {

                        for (DataSnapshot companySnapshot : task.getResult().getChildren()) {
                            Company mCompany = companySnapshot.getValue(Company.class);
                            if (mCompany != null) {
                                Global.COMPANY = mCompany;
                                if (Global.COMPANY != null) {
                                    Paper.book().write("company", Global.COMPANY);
                                }
                                Messages.ShowToast(mContext, "Company info captured");
                            }
                        }

                    } else {
                        //dialog.dismiss();
                        Toast.makeText(mContext, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        Log.d("firebase", task.getException().getMessage());
                    }
                }
            });
        }
    }
}