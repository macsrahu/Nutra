package com.sales.numax.printing.textparser;



import com.sales.numax.printing.Printer;
import com.sales.numax.printing.PrinterCommands;
import com.sales.numax.printing.bluetooth.BluetoothPrinterSocketConnection;

import java.util.Hashtable;

public class PrinterTextParserBarcode implements PrinterTextParserElement {
    
    private int length;
    private int height;
    private byte[] align;
    private String code;
    private int barcodeType;
    
    public PrinterTextParserBarcode(PrinterTextParserColumn printerTextParserColumn, String textAlign, Hashtable<String, String> barcodeAttributes, String code) {
        Printer printer = printerTextParserColumn.getLine().getTextParser().getPrinter();
        code = code.trim();
        
        this.align = PrinterCommands.TEXT_ALIGN_LEFT;
        switch (textAlign) {
            case PrinterTextParser.TAGS_ALIGN_CENTER:
                this.align = PrinterCommands.TEXT_ALIGN_CENTER;
                break;
            case PrinterTextParser.TAGS_ALIGN_RIGHT:
                this.align = PrinterCommands.TEXT_ALIGN_RIGHT;
                break;
        }
        
        this.barcodeType = PrinterCommands.BARCODE_EAN13;
        try {
            if (barcodeAttributes.containsKey(PrinterTextParser.ATTR_BARCODE_TYPE)) {
                switch (barcodeAttributes.get(PrinterTextParser.ATTR_BARCODE_TYPE)) {
                    case PrinterTextParser.ATTR_BARCODE_TYPE_EAN8:
                        this.barcodeType = PrinterCommands.BARCODE_EAN8;
                        break;
                    case PrinterTextParser.ATTR_BARCODE_TYPE_UPCA:
                        this.barcodeType = PrinterCommands.BARCODE_UPCA;
                        break;
                    case PrinterTextParser.ATTR_BARCODE_TYPE_UPCE:
                        this.barcodeType = PrinterCommands.BARCODE_UPCE;
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.length = printer.getNbrCharactersPerLine();

        this.height = printer.mmToPx(10f);
        try {
            if (barcodeAttributes.containsKey(PrinterTextParser.ATTR_BARCODE_HEIGHT)) {
                this.height = printer.mmToPx(Float.parseFloat(barcodeAttributes.get(PrinterTextParser.ATTR_BARCODE_HEIGHT)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        this.code = code;
    }

    /**
     * Get the barcode width in char length.
     *
     * @return int
     */
    @Override
    public int length() {
        return this.length;
    }

    /**
     * Print barcode
     *
     * @param printerSocket Bluetooth printer socket connection
     * @return this Fluent method
     */
    @Override
    public PrinterTextParserBarcode print(BluetoothPrinterSocketConnection printerSocket) {
        printerSocket
                .setAlign(this.align)
                .printBarcode(this.barcodeType, this.code, this.height);
        return this;
    }
}
