package com.sales.numax.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sales.numax.R;
import com.sales.numax.adapters.OrdersAdapter;
import com.sales.numax.adapters.ProductAdapter;
import com.sales.numax.common.FirebaseData;
import com.sales.numax.common.FirebaseTables;
import com.sales.numax.model.DeviceInfoModel;
import com.sales.numax.model.OrderMain;
import com.sales.numax.model.Product;
import com.sales.numax.utility.Global;
import com.sales.numax.utility.GridSpacingItemDecoration;
import com.sales.numax.utility.Messages;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MyOrdersListActivity extends AppCompatActivity {

    private ArrayList<OrderMain> mOrders = new ArrayList<OrderMain>();

    @BindView(R.id.rvOrders)
    RecyclerView rvOrders;

    @BindView(R.id.tvNoRecordFound)
    TextView tvNoRecordFound;

    OrdersAdapter adapter;
    Toolbar mToolbarView = null;

    private static final int ENABLE_BT_REQUEST_CODE = 1;

    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket bluetoothSocket;
    BluetoothDevice bluetoothDevice;

    public OutputStream outputStream;
    public InputStream inputStream;
    Thread thread;

    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    ArrayList<BluetoothDevice> mBluetoothDevices = new ArrayList<BluetoothDevice>();
    ArrayList<DeviceInfoModel> mDeviceInfo = new ArrayList<DeviceInfoModel>();

    public boolean IsPrinterConnected = false;
    Spinner spinner_printer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.orders_list);
        ButterKnife.bind(this);
        FirebaseData.LoadCompany(getApplicationContext());
        mToolbarView = Global.PrepareToolBar(this, true, "Orders");
        setSupportActionBar(mToolbarView);

        FirebaseData.LoadCompany(getApplicationContext());
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
        Intent iMain = new Intent(MyOrdersListActivity.this, MainActivity.class);
        iMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(iMain);
        finish();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.filter_menu, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_filter:
                if (mOrders != null && mOrders.size() > 0) {
                    //sFilterCondition();
                } else {
                    Toast.makeText(getApplicationContext(), "No order(s) found to filter", Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.action_printer_settings:
                if (mBluetoothDevices.size() > 0) {
                    ShowPrinterSettings();
                } else {
                    Toast.makeText(getApplicationContext(), "No bluetoth printer found to setup", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                onBackPressed();
                break;
        }
        return true;
    }

    private void ShowPrinterSettings() {

        final MaterialDialog dialogPrinter = new MaterialDialog.Builder(MyOrdersListActivity.this)
                .autoDismiss(true)
                .title("Printer Setup")
                .customView(R.layout.dialog_printer_settings, true)
                .contentGravity(GravityEnum.CENTER)
                .positiveText("OK")
                .negativeText("CANCEL")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        try {
                            openBluetoothPrinter();
                        } catch (Exception ex) {
                            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .build();
        dialogPrinter.show();
        spinner_printer = (Spinner) dialogPrinter.getCustomView().findViewById(R.id.spinner_printer);


        ArrayAdapter spinnerCategoryAdapter =
                new ArrayAdapter<DeviceInfoModel>(getBaseContext(),
                        android.R.layout.simple_spinner_item, mDeviceInfo);

        spinnerCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_printer.setAdapter(spinnerCategoryAdapter);
        spinner_printer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                spinner_printer.setSelection(position);
                if (parent != null) {
                    DeviceInfoModel selectedBluetoothDevice = (DeviceInfoModel) parent.getItemAtPosition(position);
                    if (selectedBluetoothDevice != null) {

                        Toast.makeText(getApplicationContext(), "select device is :" + selectedBluetoothDevice.getName(), Toast.LENGTH_LONG).show();
                        try {

                            for (BluetoothDevice pairedDev : mBluetoothDevices) {
                                if (pairedDev.getAddress().equalsIgnoreCase(pairedDev.getAddress())) {
                                    bluetoothDevice = pairedDev;
                                    //lblPrinterName.setText("Bluetooth Printer Attached: " + pairedDev.getName());
                                    //lblPrinterName.setText(selectedBluetoothDevice.getName());
                                }
                            }

                        } catch (Exception ex) {
                            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                        }

                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
    }

    private void openBluetoothPrinter() throws IOException {
        try {

            //Standard uuid from string //
            UUID uuidSting = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuidSting);
            bluetoothSocket.connect();

            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();
            beginListenData();
            IsPrinterConnected = true;
            Toast.makeText(getApplicationContext(), "Bluetooth Connected", Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            //button_connect.setText("Connect");
            IsPrinterConnected = false;
            Toast.makeText(getApplicationContext(), "Unable to open Bluetooth", Toast.LENGTH_LONG).show();
        }
    }

    private void beginListenData() {
        try {

            final Handler handler = new Handler();
            final byte delimiter = 10;
            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];

            thread = new Thread(new Runnable() {
                @Override
                public void run() {

                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                        try {
                            int byteAvailable = inputStream.available();
                            if (byteAvailable > 0) {
                                byte[] packetByte = new byte[byteAvailable];
                                inputStream.read(packetByte);

                                for (int i = 0; i < byteAvailable; i++) {
                                    byte b = packetByte[i];
                                    if (b == delimiter) {
                                        byte[] encodedByte = new byte[readBufferPosition];
                                        System.arraycopy(
                                                readBuffer, 0,
                                                encodedByte, 0,
                                                encodedByte.length
                                        );
                                        final String data = new String(encodedByte, "US-ASCII");
                                        readBufferPosition = 0;
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getApplicationContext(), data, Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }

                            }
                        } catch (Exception ex) {
                            stopWorker = true;
                            IsPrinterConnected = false;
                        }
                    }

                }
            });

            thread.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            IsPrinterConnected = false;
        }
    }

    public void closeBT() throws IOException {
        try {

            if (outputStream != null) {
                if (bluetoothSocket.isConnected()) {
                    stopWorker = true;
                    outputStream.close();
                    inputStream.close();
                    bluetoothSocket.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ENABLE_BT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Bluetooth has been enabled.",
                        Toast.LENGTH_SHORT).show();
                LoadPrinter();
            } else { // RESULT_CANCELED as user refuse or failed
                Toast.makeText(getApplicationContext(), "Bluetooth is not enabled.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void LoadPrinter() {

        final ProgressDialog dialog = ProgressDialog.show(this,
                null,
                "Loading devices",
                true);

        dialog.show();
        try {


            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                Toast.makeText(getApplicationContext(), "No Bluetooth Adapter found", Toast.LENGTH_LONG).show();
            }
            if (bluetoothAdapter.isEnabled()) {
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBT, 0);
            }

            Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();
            mBluetoothDevices.clear();
            mDeviceInfo.clear();


            if (pairedDevice.size() > 0) {

                for (BluetoothDevice pairedDev : pairedDevice) {
                    //Toast.makeText(getApplicationContext(), pairedDev.getName(),Toast.LENGTH_LONG).show();
                    // My Bluetoth printer name is BTP_F09F1A
                    mBluetoothDevices.add(pairedDev);

                    DeviceInfoModel deviceInfoModel = new DeviceInfoModel();
                    deviceInfoModel.setAddress(pairedDev.getAddress());
                    deviceInfoModel.setBondState(pairedDev.getBondState());
                    deviceInfoModel.setName(pairedDev.getName());
                    //deviceInfoModel.setType(pairedDev.getType());

                    mDeviceInfo.add(deviceInfoModel);

                    if (pairedDev.getName().equals("HOP-H58")) {
                        bluetoothDevice = pairedDev;
                        Toast.makeText(getApplicationContext(), "Bluetooth Printer Attached: " + pairedDev.getName(), Toast.LENGTH_LONG).show();
                        break;
                    }
                }

                dialog.dismiss();

            }
            //lblPrinterName.setText("Bluetooth Printer Attached");
        } catch (Exception ex) {
            if (dialog != null) {
                dialog.dismiss();
            }
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }


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

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        rvOrders.setLayoutManager(mLayoutManager);
        rvOrders.addItemDecoration(new GridSpacingItemDecoration(1, Global.dpToPx(5, getApplicationContext()), false));
        rvOrders.setItemAnimator(new DefaultItemAnimator());
        LoadOrders();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled()) {
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBT, 0);
                LoadPrinter();
            } else {
                Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetoothIntent, ENABLE_BT_REQUEST_CODE);

            }
        }

    }


    private void LoadOrders() {

        final ProgressDialog dialog = ProgressDialog.show(this,
                null,
                "Loading order..",
                true);

        dialog.show();

        FirebaseDatabase.getInstance().getReference().child(FirebaseTables.TBL_ORDERS_MAIN)
                .get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                mOrders = new ArrayList<OrderMain>();
                if (task.isSuccessful()) {

                    for (DataSnapshot orderSnapshot : task.getResult().getChildren()) {
                        OrderMain ordersMain = orderSnapshot.getValue(OrderMain.class);
                        if (ordersMain != null) {
                            ordersMain.setKey(orderSnapshot.getKey());
                            mOrders.add(ordersMain);
                        }
                    }
                    if (mOrders.size() > 0) {
                        Collections.sort(mOrders, new Comparator<OrderMain>() {
                            public int compare(OrderMain obj1, OrderMain obj2) {
                                return (obj1.getOrderdatestamp() > obj2.getOrderdatestamp()) ? -1 : (obj1.getOrderdatestamp() > obj2.getOrderdatestamp()) ? 1 : 0;
                            }
                        });
                        adapter = new OrdersAdapter(getApplicationContext(), MyOrdersListActivity.this, mOrders);
                        rvOrders.setAdapter(adapter);
                        tvNoRecordFound.setVisibility(View.GONE);
                        rvOrders.setVisibility(View.VISIBLE);
                    } else {
                        rvOrders.setVisibility(View.GONE);
                        tvNoRecordFound.setVisibility(View.VISIBLE);
                    }
                    dialog.dismiss();
                } else {
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}
