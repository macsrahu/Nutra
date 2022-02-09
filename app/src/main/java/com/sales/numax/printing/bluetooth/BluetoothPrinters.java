package com.sales.numax.printing.bluetooth;

import android.bluetooth.BluetoothClass;



public class BluetoothPrinters extends BluetoothDevices {
    
    /**
     * Easy way to get the first bluetooth printer paired / connected.
     *
     * @return a BluetoothPrinterSocketConnection instance
     */
    public static BluetoothPrinterSocketConnection selectFirstPairedBluetoothPrinter() {
        BluetoothPrinters printers = new BluetoothPrinters();
        BluetoothPrinterSocketConnection[] bluetoothPrinters = printers.getList();
        
        if (bluetoothPrinters != null && bluetoothPrinters.length > 0) {
            for (BluetoothPrinterSocketConnection printer : bluetoothPrinters) {
                if (printer.connect()) {
                    return printer;
                }
            }
        }
        return null;
    }
    
    
    /**
     * Get a list of bluetooth printers.
     *
     * @return an array of BluetoothPrinterSocketConnection
     */
    public BluetoothPrinterSocketConnection[] getList() {
        BluetoothDeviceSocketConnection[] bluetoothDevicesList = super.getList();
    
        if(bluetoothDevicesList == null) {
            return null;
        }
    
        int i = 0, j = 0;
        BluetoothPrinterSocketConnection[] bluetoothPrintersTmp = new BluetoothPrinterSocketConnection[bluetoothDevicesList.length];
        for (BluetoothDeviceSocketConnection device : bluetoothDevicesList) {
            if (device.getDevice().getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.IMAGING && device.getDevice().getBluetoothClass().getDeviceClass() == 1664) {
                bluetoothPrintersTmp[i++] = new BluetoothPrinterSocketConnection(device.getDevice());
            }
        }
        BluetoothPrinterSocketConnection[] bluetoothPrinters = new BluetoothPrinterSocketConnection[i];
        for (BluetoothPrinterSocketConnection device : bluetoothPrintersTmp) {
            if (device != null) {
                bluetoothPrinters[j++] = device;
            }
        }
        return bluetoothPrinters;
    }
    
}
