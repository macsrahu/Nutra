package com.sales.numax.printing.textparser;

//import bluetooth.BluetoothPrinterSocketConnection;

import com.sales.numax.printing.bluetooth.BluetoothPrinterSocketConnection;

public interface PrinterTextParserElement {
    int length();
    PrinterTextParserElement print(BluetoothPrinterSocketConnection printerSocket);
}
