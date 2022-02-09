package com.sales.numax.printing.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.Set;

public class BluetoothDevices {
    protected BluetoothAdapter bluetoothAdapter;
    
    /**
     * Create a new instance of BluetoothDevices
     */
    public BluetoothDevices() {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }
    
    /**
     * Get a list of bluetooth devices available.
     * @return Return an array of BluetoothDeviceSocketConnection instance
     */
    public BluetoothDeviceSocketConnection[] getList() {
        if (this.bluetoothAdapter == null) {
            return null;
        }
    
        if(!this.bluetoothAdapter.isEnabled()) {
            return null;
        }
        
        Set<BluetoothDevice> bluetoothDevicesList = this.bluetoothAdapter.getBondedDevices();
        BluetoothDeviceSocketConnection[] bluetoothDevices = new BluetoothDeviceSocketConnection[bluetoothDevicesList.size()];
    
        if (bluetoothDevicesList.size() > 0) {
            int i = 0;
            for (BluetoothDevice device : bluetoothDevicesList) {
                bluetoothDevices[i++] = new BluetoothDeviceSocketConnection(device);
            }
        }
        
        return bluetoothDevices;
    }
}
