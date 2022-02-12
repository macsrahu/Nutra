package com.sales.numax.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sales.numax.R;
import com.sales.numax.common.FirebaseTables;
import com.sales.numax.model.DeviceInfoModel;
import com.sales.numax.model.OrderLine;
import com.sales.numax.printing.Printer;
import com.sales.numax.printing.bluetooth.BluetoothPrinters;
import com.sales.numax.printing.textparser.PrinterTextParserImg;
import com.sales.numax.utility.Global;
import com.sales.numax.utility.KeyboardUtil;
import com.sales.numax.utility.Messages;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import crl.android.pdfwriter.PDFWriter;
import crl.android.pdfwriter.PaperSize;
import crl.android.pdfwriter.StandardFonts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

import static java.lang.Float.parseFloat;


public class PrintOrder extends AppCompatActivity {

    String _FOLDER_PATH = "Nutra/pdf";
    private static final int ENABLE_BT_REQUEST_CODE = 1;
    private static final int DISCOVERABLE_BT_REQUEST_CODE = 2;
    private static final int DISCOVERABLE_DURATION = 300;

    ArrayList<BluetoothDevice> mBluetoothDevices = new ArrayList<BluetoothDevice>();
    ArrayList<DeviceInfoModel> mDeviceInfo = new ArrayList<DeviceInfoModel>();


    protected static final String TAG = "TAG";
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    @BindView(R.id.bottomNavigation)
    BottomNavigationView bottomNavigation;

    @BindView(R.id.text_view_preview)
    TextView text_view_preview;

    boolean FirstTime = true;
    StringBuilder stringBuilder = new StringBuilder();
    private ArrayList<OrderLine> mOrderLineItems = new ArrayList<>();
    private ArrayList<OrderLine> mPrintOrderLineItems = new ArrayList<>();
    public static final int STORAGE = 125;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.print_order);
        FirstTime = true;
        ButterKnife.bind(this);

        text_view_preview.setMovementMethod(new ScrollingMovementMethod());

        Toolbar mToolbarView = Global.PrepareToolBar(this, true, "Print Order");
        setSupportActionBar(mToolbarView);

        LoadOrdersItem();
        FolderPermission();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            CreateFolders();
        } else {
            CreateFolders();
        }
        InitControls();
        KeyboardUtil.hideKeyboard(this);

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void InitControls() {

        bottomNavigation.setVisibility(View.VISIBLE);
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {

                case R.id.btnPrint:
                    PrintOrder();
                    break;

                case R.id.btnShare:
                    if (mPrintOrderLineItems != null) {
                        if (mPrintOrderLineItems.size() > 0) {
                            OrderPDFCreation(0);
                        } else {
                            Messages.ShowToast(getApplicationContext(), "No item found to share");
                        }
                    } else {
                        Messages.ShowToast(getApplicationContext(), "No item found to share");
                    }
                    break;

                case R.id.btnBack:
                    onBackPressed();
                    break;
            }
            return true;
        });
    }

    private void PrintOrder() {

        float totalQty = 0;
        double totalAmout = 0;
        String sLineItems = "";
        //mPrintOrderLineItems= Global.ORDER_LINE;
        if (mPrintOrderLineItems.size() > 0) {

            String AddressLine1 = !Global.COMPANY.getAddressline1().isEmpty() ? Global.COMPANY.getAddressline1() + "\n" : "";
            String AddressLine2 = !Global.COMPANY.getAddressline2().isEmpty() ? Global.COMPANY.getAddressline2() + "\n" : "";
            String City = Global.COMPANY.getCity() + "-" + Global.COMPANY.getPincode() + "\n";
            String sAddress = !AddressLine1.isEmpty() ? AddressLine1 + "\n" : !AddressLine2.isEmpty() ? AddressLine2 + "\n" : City;

            String sItemName = "";
            for (int i = 0; i < mPrintOrderLineItems.size(); i++) {
                sItemName = mPrintOrderLineItems.get(i).getProductname();//s  mPrintOrderLineItems.get(i).getProductname().length()>32? mPrintOrderLineItems.get(i).getProductname().substring(0,28) : mPrintOrderLineItems.get(i).getProductname();
                sLineItems = sLineItems + "[L]<b>" + mPrintOrderLineItems.get(i).getProductname() + "</b>[R]" + String.valueOf(mPrintOrderLineItems.get(i).getQty()) + "\n" +
                        "[L]+" + mPrintOrderLineItems.get(i).getUom() + "[C]Rs." + mPrintOrderLineItems.get(i).getPrice() + "[R]Amt:" + mPrintOrderLineItems.get(i).getAmount() + "\n" +
                        "[L]\n";
                //  totalQty = totalQty + mPrintOrderLineItems.get(i).getQty();
                totalAmout = totalAmout + mPrintOrderLineItems.get(i).getAmount();
            }
            //Toast.makeText(getApplicationContext(),sLineItems,Toast.LENGTH_LONG).show();
            //totalAmout =500.0f;

            Printer printer = new Printer(BluetoothPrinters.selectFirstPairedBluetoothPrinter(), 203, 48f, 32);
            printer.printFormattedText(
                    "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, this.getApplicationContext().getResources().getDrawableForDensity(R.drawable.print_logo, DisplayMetrics.DENSITY_DEFAULT)) + "</img>\n" +
                            "[C]bridle\n" +
                            "[L]\n" +
                            "[C]<b>" + Global.COMPANY.getName().trim() + "</b>\n" +
                            "[C]<b>GST-" + Global.COMPANY.getGst() + "</b>\n" +
                            "[C]<b>" + City + "</b>\n" +
                            //"[L]\n" +
                            "[L]\n" +
                            "[L]<b>" + Global.SELECTED_ORDER_MAIN.getOrderno() + "[R]" + Global.SELECTED_ORDER_MAIN.getOrderdate() + "</b>\n" +
                            "[C]--------------------------------\n" + sLineItems +
                            "[C]--------------------------------\n" +
                            "[L]<b>NET TOTAL:[R]" + String.valueOf(totalAmout).toString() + "</b>\n" +
                            "[C]--------------------------------\n" +
                            "[L]<b>Customer:</b>\n" +
                            "[L]" + Global.SELECTED_ORDER_MAIN.getDealer() +
                            "[L]\n" +
                            "[L]" + Global.SELECTED_ORDER_MAIN.getAddress() +
                            "[L]\n"

            );
        } else {
            Toast.makeText(getApplicationContext(), "No item(s) found to print", Toast.LENGTH_LONG).show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ENABLE_BT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Bluetooth has been enabled.",
                        Toast.LENGTH_SHORT).show();
            } else { // RESULT_CANCELED as user refuse or failed
                Toast.makeText(getApplicationContext(), "Bluetooth is not enabled.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String GetCenteredText(String str, int size) {
        int left = (size - str.length()) / 2;
        int right = size - left - str.length();
        String repeatedChar = " ";
        StringBuffer buff = new StringBuffer();
        for (int i = 0; i < left; i++) {
            buff.append(repeatedChar);
        }
        buff.append(str);
        for (int i = 0; i < right; i++) {
            buff.append(repeatedChar);
        }
        // to see the end (and debug) if using spaces as repeatedChar
        //buff.append("$");
        return buff.toString(); //System.out.println(buff.toString());
    }

    private void LoadOrdersItem() {

        if (Global.SELECTED_ORDER_MAIN != null) {

            final ProgressDialog dialog = ProgressDialog.show(this,
                    null,
                    "Loading order..",
                    true);

            dialog.show();
            DatabaseReference mDataReference = FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_ORDERS_LINE_ITEMS);
            mDataReference.child(Global.SELECTED_ORDER_MAIN.getKey()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ArrayList<OrderLine> mOrderLineItems = new ArrayList<OrderLine>();
                    mPrintOrderLineItems = new ArrayList<OrderLine>();
                    if (dataSnapshot.exists()) {

                        for (DataSnapshot orderItemSnapshot : dataSnapshot.getChildren()) {
                            OrderLine ordersLineItems = orderItemSnapshot.getValue(OrderLine.class);
                            if (ordersLineItems != null) {
                                mOrderLineItems.add(ordersLineItems);
                                mPrintOrderLineItems.add(ordersLineItems);
                            }
                        }

                        if (mOrderLineItems.size() > 0) {
                            int iCharCount = 64;
                            double totalQty = 0;

                            if (Global.COMPANY != null) {
                                stringBuilder.append(GetCenteredText(Global.COMPANY.getName().toUpperCase(), iCharCount));
                                stringBuilder.append("\n");
                                stringBuilder.append(GetCenteredText(Global.COMPANY.getGst().toUpperCase(), iCharCount));
                                stringBuilder.append("\n");
                            }

                            stringBuilder.append("\n");
                            stringBuilder.append(GetCenteredText("ORDER", iCharCount));
                            stringBuilder.append("\n");
                            for (int j = 0; j < iCharCount; j++) {
                                stringBuilder.append("-");
                            }
                            stringBuilder.append("\n");
                            stringBuilder.append(Global.SELECTED_ORDER_MAIN.getOrderno() + "         " + Global.SELECTED_ORDER_MAIN.getOrderdate()); //10 Space between order no and date
                            stringBuilder.append("\n");
                            stringBuilder.append("\n");
                            stringBuilder.append(Global.SELECTED_ORDER_MAIN.getDealer());
                            stringBuilder.append("\n");
                            stringBuilder.append(Global.SELECTED_ORDER_MAIN.getAddress());
                            stringBuilder.append("\n");
                            for (int j = 0; j < iCharCount; j++) {
                                stringBuilder.append("-");
                            }
                            stringBuilder.append("\n");
                            stringBuilder.append("Product                                                      Qty/Amount"); //10 Space between order no and date
                            stringBuilder.append("\n");
                            for (int j = 0; j < iCharCount; j++) {
                                stringBuilder.append("-");
                            }
                            stringBuilder.append("\n");
                            for (int i = 0; i < mOrderLineItems.size(); i++) {
                                stringBuilder.append(mOrderLineItems.get(i).getProductname() + "\n");
                                stringBuilder.append(String.valueOf(mOrderLineItems.get(i).getUom()));

                                if (String.valueOf(mOrderLineItems.get(i).getUom()).length() <= 28) {
                                    int uom_length = (62 - String.valueOf(mOrderLineItems.get(i).getUom()).length());
                                    for (int l = 0; l < uom_length; l++) {
                                        stringBuilder.append(" ");
                                    }
                                }
                                stringBuilder.append(Global.GetFormatedValue(mOrderLineItems.get(i).getQty()));
                                for (int j = 0; j < 10; j++) {
                                    stringBuilder.append(" ");
                                }

                                stringBuilder.append(Global.GetFormatedValueWithoutDecimal(mOrderLineItems.get(i).getAmount()));
                                stringBuilder.append("\n");

                                totalQty = totalQty + mOrderLineItems.get(i).getQty();

                   /*         if (String.valueOf(mOrderLineItems.get(i).getPrice()).length() < 7) {
                                for (int l = 0; l < (7 - String.valueOf(mOrderLineItems.get(i).getPrice()).length()); l++) {
                                    stringBuilder.append(" ");
                                }
                            }
                            stringBuilder.append(String.valueOf(mOrderLineItems.get(i).getPrice()) + "   ");
                            if (String.valueOf(mOrderLineItems.get(i).getQty()).length() < 3) {
                                for (int k = 0; k < (3 - String.valueOf(mOrderLineItems.get(i).getQty()).length()); k++) {
                                    stringBuilder.append(" ");
                                }
                            }*/

                                //stringBuilder.append("          " + String.valueOf(mOrderLineItems.get(i).getQty()));
                                //stringBuilder.append(" "); //2
                         /*   float price = Float.valueOf(mOrderLineItems.get(i).getPrice());
                            if (price > 0) {
                                String sAmount = Global.GetFormatedAmount(String.valueOf(mOrderLineItems.get(i).getQty() * price));
                                stringBuilder.append(sAmount);
                            }*/

                            }
                            for (int j = 0; j < iCharCount; j++) {
                                stringBuilder.append("-");
                            }
                            stringBuilder.append("\n");
                            stringBuilder.append("NET AMOUNT                ");
                            stringBuilder.append(String.valueOf(totalQty));
                            // stringBuilder.append(Global.GetFormatedAmount(String.valueOf(Global.SELECTED_ORDER.getNetamount())));
                            stringBuilder.append("\n");
                            for (int j = 0; j < iCharCount; j++) {
                                stringBuilder.append("-");
                            }
                            stringBuilder.append("\n");
                            //stringBuilder.append(GetCenteredText("Thank you!!", iCharCount));
                            stringBuilder.append("\n");
                            stringBuilder.append("\n");

                            text_view_preview.setText(stringBuilder.toString());
                            LoadReturnItems();
                        }
                    }
                    dialog.dismiss();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    dialog.dismiss();
                }
            });
        }

    }

    private void LoadReturnItems() {


        final ProgressDialog dialog = ProgressDialog.show(this,
                null,
                "Loading data",
                true);

        dialog.show();
        DatabaseReference mOrderReturn = FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_ORDERS_RETURN_LINE_ITEMS);
        mOrderReturn.child(Global.SELECTED_ORDER_MAIN.getKey()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<OrderLine> mOrderLineItems = new ArrayList<OrderLine>();

                if (dataSnapshot.exists()) {

                    for (DataSnapshot orderItemSnapshot : dataSnapshot.getChildren()) {
                        OrderLine ordersLineItems = orderItemSnapshot.getValue(OrderLine.class);
                        if (ordersLineItems != null) {
                            mOrderLineItems.add(ordersLineItems);
                        }
                    }
                    if (mOrderLineItems.size() > 0) {
                        int iCharCount = 32;
                        double totalQty = 0;

                        stringBuilder.append("\n");
                        stringBuilder.append(GetCenteredText("RETURN ITEM", iCharCount));
                        stringBuilder.append("\n");
                        for (int j = 0; j < iCharCount; j++) {
                            stringBuilder.append("-");
                        }
                        stringBuilder.append("\n");
                        for (int j = 0; j < iCharCount; j++) {
                            stringBuilder.append("-");
                        }
                        stringBuilder.append("\n");
                        for (int i = 0; i < mOrderLineItems.size(); i++) {
                            stringBuilder.append(String.valueOf(mOrderLineItems.get(i).getUom()));
                            if (String.valueOf(mOrderLineItems.get(i).getUom()).length() <= 28) {
                                for (int l = 0; l < (28 - String.valueOf(mOrderLineItems.get(i).getUom()).length()); l++) {
                                    stringBuilder.append(" ");
                                }
                            }
                            if (String.valueOf(mOrderLineItems.get(i).getQty()).length() <= 4) {
                                for (int l = 0; l < (4 - String.valueOf(mOrderLineItems.get(i).getQty()).length()); l++) {
                                    stringBuilder.append(" ");
                                }
                            }
                            stringBuilder.append(String.valueOf(mOrderLineItems.get(i).getQty()));
                            stringBuilder.append("\n");

                            totalQty = totalQty + mOrderLineItems.get(i).getQty();

                        }
                        for (int j = 0; j < iCharCount; j++) {
                            stringBuilder.append("-");
                        }
                        stringBuilder.append("\n");
                        stringBuilder.append("NET TOTAL                  ");
                        stringBuilder.append(String.valueOf(totalQty));
                        // stringBuilder.append(Global.GetFormatedAmount(String.valueOf(Global.SELECTED_ORDER.getNetamount())));
                        stringBuilder.append("\n");
                        for (int j = 0; j < iCharCount; j++) {
                            stringBuilder.append("-");
                        }
                        stringBuilder.append("\n");
                        // stringBuilder.append(GetCenteredText("Thank you!!", iCharCount));
                        for (int j = 0; j < 5; j++) {
                            stringBuilder.append("\n");
                        }

                        text_view_preview.setText(stringBuilder.toString());
                    }
                }
                dialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                dialog.dismiss();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();


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
    public void onBackPressed() {

        Intent iMain = new Intent(PrintOrder.this, MyOrderDetail.class);
        iMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(iMain);
        finish();
    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

    }

    private void CreateFolders() {

        String MEDIA_MOUNTED = "mounted";
        try {
            File exportDir = new File(getBaseContext().getExternalCacheDir(), _FOLDER_PATH);
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            // String text = "Receipt No: " + mReceipt.getReceiptno() + "\n Receipt Date: " + mReceipt.getReceiptdate() + "\n Amount: " + text_view_amount.getText() + "\n Donated for the month of " + mReceipt.getPaymonth().toUpperCase();
        } catch (Exception e) {
            Messages.ShowToast(getApplicationContext(), "There is a problem to create folder");
        }


    }

    private void FolderPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            permissionCheck += ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permissionCheck != 0) {
                ActivityCompat.requestPermissions(PrintOrder.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void CreatePdf(String sometext) {
        // create a new document
        PdfDocument document = new PdfDocument();
        // crate a page description
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 600, 1).create();
        // start a page
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        canvas.drawCircle(50, 50, 30, paint);
        paint.setColor(Color.BLACK);
        canvas.drawText(sometext, 80, 50, paint);
        //canvas.drawt
        // finish the page
        document.finishPage(page);
// draw text on the graphics object of the page
        // Create Page 2
        pageInfo = new PdfDocument.PageInfo.Builder(300, 600, 2).create();
        page = document.startPage(pageInfo);
        canvas = page.getCanvas();
        paint = new Paint();
        paint.setColor(Color.BLUE);
        canvas.drawCircle(100, 100, 100, paint);
        document.finishPage(page);
        // write the document content
        String directory_path = getBaseContext().getCacheDir().getPath() + _FOLDER_PATH;
        File file = new File(directory_path);
        if (!file.exists()) {
            file.mkdir();
        }
        String targetPdf = directory_path + String.valueOf(new Date().getTime()) + ".pdf";
        File filePath = new File(targetPdf);
        try {
            document.writeTo(new FileOutputStream(filePath));
            Toast.makeText(this, "Done", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("main", "error " + e.toString());
            Toast.makeText(this, "Something wrong: " + e.toString(), Toast.LENGTH_LONG).show();
        }
        // close the document
        document.close();
    }

    ///------------------------------------------------
    ///PDF GENERATION
    //------------------------------------------
    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private void OrderPDFCreation(int menu_type) {


        PDFWriter writer = new PDFWriter(PaperSize.FOLIO_WIDTH, PaperSize.FOLIO_HEIGHT);
        int _START_MARGIN = 890;
        int _FOLIO_WIDTH = 612;
        int _LEFT = 20;
        int _NO_OF_LINES_PER_PAGE = 17;
        Bitmap bitmapLogo = drawableToBitmap(getBaseContext().getResources().getDrawable(R.drawable.print_logo));

        writer.setFont(StandardFonts.SUBTYPE, StandardFonts.MAC_ROMAN_ENCODING);
        String sSubTitle = "Sales Order";
        //writer.addRawContent("1 0 0 rg\n");
        // writer.addImage(_LEFT, _START_MARGIN, bitmapLogo);

        String sTitle = getResources().getString(R.string.app_name).toUpperCase();

        writer.addText(((_FOLIO_WIDTH / 2) - (sTitle.length() / 2)) - 20, _START_MARGIN, 20, sTitle);

        _START_MARGIN = _START_MARGIN - 30;
        writer.addText(((_FOLIO_WIDTH / 2) - (sSubTitle.length() / 2) - 60), _START_MARGIN, 20, sSubTitle)
        ;
        //writer.addRawContent("0 0 0 rg\n");

        _START_MARGIN = _START_MARGIN - 20;
        writer.addLine(_LEFT, _START_MARGIN, PaperSize.FOLIO_WIDTH - 10, _START_MARGIN);

        _START_MARGIN = _START_MARGIN - 30;

        String sProdductDesc = rightpad("Order No:", 8) + rightpad(Global.SELECTED_ORDER_MAIN.getOrderno(), 60) + rightpad("Date:", 7) + rightpad(Global.GetFormatedValue(Global.SELECTED_ORDER_MAIN.getOrderamount()), 10);
        writer.addText(_LEFT, _START_MARGIN, 14, sProdductDesc);

        _START_MARGIN = _START_MARGIN - 20;

        writer.addLine(_LEFT, _START_MARGIN, PaperSize.FOLIO_WIDTH - 10, _START_MARGIN);

        _START_MARGIN = _START_MARGIN - 20;

        String sName = rightpad("Name:", 10) + rightpad(Global.SELECTED_ORDER_MAIN.getDealer(), 100);
        writer.addText(_LEFT, _START_MARGIN, 12, sName);

        _START_MARGIN = _START_MARGIN - 20;
        String sAddress = rightpad("Address:", 10) + rightpad(Global.SELECTED_ORDER_MAIN.getAddress(), 150);
        writer.addText(_LEFT, _START_MARGIN, 12, sAddress);

        DecimalFormat decimalQtyFormat = new DecimalFormat("#.00");
        String receiptAmount = decimalQtyFormat.format(Float.parseFloat((String.valueOf(Global.SELECTED_ORDER_MAIN.getOrderamount()))));

        _START_MARGIN = _START_MARGIN - 20;
        String sAmount = rightpad("Amount:", 10) + rightpad(getStringAtFixedLength(receiptAmount, 8), 10);
        writer.addText(_LEFT, _START_MARGIN, 12, sAmount);
        _START_MARGIN = _START_MARGIN - 20;

        String strProductName = "";
        double dblTotal = 0;
        int Bottom = _START_MARGIN;
        int iSerialNo = 1;
        _START_MARGIN = _START_MARGIN - 20;

        for (OrderLine mOrderLine : mPrintOrderLineItems) {
            if (mOrderLine.getAmount() > 0) {

                strProductName = mOrderLine.getProductname().toUpperCase();
                if (mOrderLine.getProductname().length() > 30) {
                    strProductName = mOrderLine.getProductname().substring(0, 30).trim().toUpperCase();
                }
                String sDesc = rightpad(String.valueOf(iSerialNo) + ".", 7)
                        + strProductName.toUpperCase();
                writer.addText(_LEFT, Bottom, 12, sDesc);

                writer.addText(_LEFT + 120, Bottom, 12, Global.GetFormatedValue(mOrderLine.getPrice()));

                writer.addText(_LEFT + 160, Bottom, 12, Global.GetFormatedValue(mOrderLine.getQty()));

                Bottom = Bottom - 20;
                DecimalFormat decimalFormat = new DecimalFormat("#.00");
                String amount = decimalFormat.format(mOrderLine.getAmount());
                writer.addText(490, Bottom, 12, getStringAtFixedLength(amount, 8));
                iSerialNo = iSerialNo + 1;
            }
        }
//        writer.addRawContent("1 0 0 rg\n");
        Bottom = Bottom - 20;
        writer.addLine(_LEFT, Bottom, PaperSize.FOLIO_WIDTH - 10, Bottom);
        //------------------------------------------------------------------------
        String sThanks = "Thank you for your great generosity.Your support is invaluable to us, thank you again!";

        Bottom = Bottom - 20;
        writer.addText(_LEFT, Bottom, 10, sThanks);

        outputToFile(Global.SELECTED_ORDER_MAIN.getOrderno() + ".pdf", writer.asString(), "ISO-8859-1", menu_type);
    }

    private String rightpad(String text, int length) {
        return String.format("%-" + length + "." + length + "s", text);
    }

    private void outputToFile(String fileName, String pdfContent, String encoding, int menu_type) {

        File newFile = new File(getBaseContext().getExternalCacheDir().getAbsolutePath() + "/" + fileName);
        try {
            newFile.createNewFile();
            try {
                FileOutputStream pdfFile = new FileOutputStream(newFile);
                pdfFile.write(pdfContent.getBytes(encoding));
                pdfFile.close();
                //Toast.makeText(getApplicationContext(), "PDF created at " + newFile.getPath(), Toast.LENGTH_LONG).show();

                Uri path = Uri.fromFile(newFile);

                /*Intent pdfOpenintent = new Intent(Intent.ACTION_VIEW);
                pdfOpenintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                pdfOpenintent.setDataAndType(path, "application/pdf");
                try {
                    startActivity(pdfOpenintent);
                } catch (ActivityNotFoundException e) {
                }
*/
                if (path != null) {
                    if (menu_type == 1) {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        //intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("pdf/*");
                        intent.putExtra(Intent.EXTRA_STREAM, path);
                        startActivity(Intent.createChooser(intent, "Share"));
                    } else {
                        Global.PDF_FILE = newFile.getPath();
                        Toast.makeText(getApplicationContext(), "PDF created at " + Global.PDF_FILE, Toast.LENGTH_LONG).show();
                        //Intent iPDFView = new Intent(PrintOrder.this, PDFViewer.class);
                        //iPDFView.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        //startActivity(iPDFView);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Unable to create PDF file", Toast.LENGTH_LONG).show();
                }

            } catch (FileNotFoundException e) {
                // ...
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            // ...
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String writeAtFixedLength(String pString, int length) {
        if (pString != null && !pString.isEmpty()) {
            return getStringAtFixedLength(pString, length);
        } else {
            return completeWithWhiteSpaces("", length);
        }
    }

    private String getStringAtFixedLength(String pString, int length) {
        if (length < pString.length()) {
            return pString.substring(0, length);
        } else {
            return completeWithWhiteSpaces(pString, length - pString.length());
        }
    }

    private String completeWithWhiteSpaces(String pString, int lenght) {
        for (int i = 0; i < lenght; i++)
            pString += " ";
        return pString;
    }

}
