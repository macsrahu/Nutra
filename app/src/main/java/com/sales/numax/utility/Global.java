package com.sales.numax.utility;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.snackbar.Snackbar;
import com.sales.numax.R;
import com.sales.numax.model.Category;
import com.sales.numax.model.Dealer;
import com.sales.numax.model.OrderLine;
import com.sales.numax.model.Route;
import com.sales.numax.model.UserDetail;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class Global {
    public static boolean IsHosted=false;
    static Boolean isNetAvailable = false;
    public static int USER_TYPE=0;
    public static String USER_CODE="";
    public static final String _URL_KEY = "UrlKey";
    public static UserDetail LOGIN_USER_DETAIL=null;
    public static Category SELECTED_CATEGORY=new Category();
    public static ArrayList<Category> CATEGORY_LIST = new ArrayList<Category>();
    public static ArrayList<Route> ROUTES = new ArrayList<Route>();
    public static Dealer SELECTED_DEALER=null;
    public static String DEALER_KEY="";
    public  static String SHOP_NAME="";
    public static String MENU_FROM="";

    public static ArrayList<OrderLine> ORDER_LINE=null;

    public static Toolbar PrepareToolBar(final Activity context, boolean isBackButtonVisible, String title) {
        Toolbar toolbar = (Toolbar) context.findViewById(R.id.toolbar);

        LayoutInflater li = LayoutInflater.from(context);
        View inflatedLayout = li.inflate(R.layout.toolbar, null, false);

        if (title.isEmpty() || title.equalsIgnoreCase(null)) {
            title = context.getResources().getString(R.string.app_name);
        }

        if (isBackButtonVisible) {
            toolbar.setNavigationIcon(R.drawable.left_arrow_plain);

        }
        toolbar.setTitle(title);
        toolbar.setTitleTextColor(Color.WHITE);


        return toolbar;
    }

    @SuppressLint("WrongConstant")
    public static void ShowSnackMessage(Activity mActivity, String message){
        View parentLayout =  mActivity.findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(parentLayout, message, Snackbar.LENGTH_LONG);
        snackbar.setDuration(Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    public static boolean CheckInternetConnection(final View layout, final Context context) {

        if (!AppStatus.getInstance(context).isOnline()) {

            Snackbar snackbar = Snackbar.make(layout, "No network/wifi connection found!", Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!AppStatus.getInstance(context).isOnline()) {
                                isNetAvailable = false;
                                //Toast.makeText(context,"No internet connection!",Toast.LENGTH_LONG).show();
                                CheckInternetConnection(layout, context);
                            } else {
                                isNetAvailable = true;
                            }
                        }
                    });

            snackbar.setActionTextColor(Color.RED);
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(R.id.snackbar_text);
            textView.setTextColor(Color.YELLOW);
            snackbar.show();
            isNetAvailable = false;
        } else {
            isNetAvailable = true;
        }
        return isNetAvailable;
    }

    public static int dpToPx(int dp, Context appContext) {
        Resources r = appContext.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    public static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    public static String GetFormatedValue(Double dlbValue) {
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        String sumAmount = decimalFormat.format(dlbValue);
        return sumAmount;
    }
}
