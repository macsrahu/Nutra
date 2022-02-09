package com.sales.numax.printing.bluetooth;

import android.bluetooth.BluetoothDevice;


import com.sales.numax.printing.PrinterCommands;

import java.io.IOException;
import java.io.OutputStream;



public class BluetoothPrinterSocketConnection extends BluetoothDeviceSocketConnection {
    
    protected OutputStream outputStream = null;
    
    /**
     * Create new instance of BluetoothPrinterSocketConnection.
     *
     * @param device an instance of android.bluetooth.BluetoothDevice
     */
    public BluetoothPrinterSocketConnection(BluetoothDevice device) {
        super(device);
    }
    
    public boolean isOpenedStream() {
        return (this.outputStream != null);
    }
    
    /**
     * Start socket connection and open stream with the bluetooth device.
     *
     * @return return true if success
     */
    @Override
    public boolean connect() {
        try {
            if (super.connect()) {
                this.outputStream = this.bluetoothSocket.getOutputStream();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            this.outputStream = null;
        }
        return false;
    }
    
    /**
     * Close the socket connection and stream with the bluetooth device.
     *
     * @return return true if success
     */
    @Override
    public boolean disconnect() {
        super.disconnect();
        
        if (!this.isOpenedStream()) {
            return true;
        }
        
        try {
            this.outputStream.close();
            this.outputStream = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Flushes the opened stream and forces any buffered bytes to be written out.
     *
     * @return Fluent interface
     */
    public BluetoothPrinterSocketConnection flush() {
        if (this.isOpenedStream()) {
            try {
                this.outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return this;
    }
    
    /**
     * Set the alignment of text and barcodes.
     * Don't works with image.
     *
     * @param align Set the alignment of text and barcodes. Use PrinterCommands.TEXT_ALIGN_... constants
     * @return Fluent interface
     */
    public BluetoothPrinterSocketConnection setAlign(byte[] align) {
        if (!this.isOpenedStream()) {
            return this;
        }
        try {
            this.outputStream.write(align);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }
    
    /**
     * Print text with the connected printer.
     *
     * @param text Text to be printed
     * @return Fluent interface
     */
    public BluetoothPrinterSocketConnection printText(String text) {
        return this.printText(text, 0);
    }
    
    /**
     * Print text with the connected printer.
     *
     * @param text Text to be printed
     * @param maxlength Number of bytes printed
     * @return Fluent interface
     */
    public BluetoothPrinterSocketConnection printText(String text, int maxlength) {
        return this.printText(text, null, null, null, maxlength);
    }
    
    /**
     * Print text with the connected printer.
     *
     * @param text Text to be printed
     * @param textSize Set the text size. Use PrinterCommands.TEXT_SIZE_... constants
     * @return Fluent interface
     */
    public BluetoothPrinterSocketConnection printText(String text, byte[] textSize) {
        return this.printText(text, textSize, 0);
    }
    
    /**
     * Print text with the connected printer.
     *
     * @param text Text to be printed
     * @param textSize Set the text size. Change the text size. Use PrinterCommands.TEXT_SIZE_... constants
     * @param maxlength Number of bytes printed
     * @return Fluent interface
     */
    public BluetoothPrinterSocketConnection printText(String text, byte[] textSize, int maxlength) {
        return this.printText(text, textSize, null, null, maxlength);
    }
    
    /**
     * Print text with the connected printer.
     *
     * @param text Text to be printed
     * @param textSize Set the text size. Use PrinterCommands.TEXT_SIZE_... constants
     * @param textBold Set the text weight. Use PrinterCommands.TEXT_WEIGHT_... constants
     * @return Fluent interface
     */
    public BluetoothPrinterSocketConnection printText(String text, byte[] textSize, byte[] textBold) {
        return this.printText(text, textSize, textBold, 0);
    }
    
    /**
     * Print text with the connected printer.
     *
     * @param text Text to be printed
     * @param textSize Set the text size. Use PrinterCommands.TEXT_SIZE_... constants
     * @param textBold Set the text weight. Use PrinterCommands.TEXT_WEIGHT_... constants
     * @param maxlength Number of bytes printed
     * @return Fluent interface
     */
    public BluetoothPrinterSocketConnection printText(String text, byte[] textSize, byte[] textBold, int maxlength) {
        return this.printText(text, textSize, textBold, null, maxlength);
    }
    
    /**
     * Print text with the connected printer.
     *
     * @param text Text to be printed
     * @param textSize Set the text size. Use PrinterCommands.TEXT_SIZE_... constants
     * @param textBold Set the text weight. Use PrinterCommands.TEXT_WEIGHT_... constants
     * @param textUnderline Set the underlining of the text. Use PrinterCommands.TEXT_UNDERLINE_... constants
     * @return Fluent interface
     */
    public BluetoothPrinterSocketConnection printText(String text, byte[] textSize, byte[] textBold, byte[] textUnderline) {
        return this.printText(text, textSize, textBold, textUnderline, 0);
    }
    
    /**
     * Print text with the connected printer.
     *
     * @param text Text to be printed
     * @param textSize Set the text size. Use PrinterCommands.TEXT_SIZE_... constants
     * @param textBold Set the text weight. Use PrinterCommands.TEXT_WEIGHT_... constants
     * @param textUnderline Set the underlining of the text. Use PrinterCommands.TEXT_UNDERLINE_... constants
     * @param maxlength Number of bytes printed
     * @return Fluent interface
     */
    public BluetoothPrinterSocketConnection printText(String text, byte[] textSize, byte[] textBold, byte[] textUnderline, int maxlength) {
        if (!this.isOpenedStream()) {
            return this;
        }
        
        try {
            byte[] textBytes = text.getBytes("ISO-8859-1");
            
            if (maxlength == 0) {
                maxlength = textBytes.length;
            }
            
            this.outputStream.write(PrinterCommands.WESTERN_EUROPE_ENCODING);
            this.outputStream.write(PrinterCommands.TEXT_SIZE_NORMAL);
            this.outputStream.write(PrinterCommands.TEXT_WEIGHT_NORMAL);
            this.outputStream.write(PrinterCommands.TEXT_UNDERLINE_OFF);
            
            if (textSize != null) {
                this.outputStream.write(textSize);
            }
            if (textBold != null) {
                this.outputStream.write(textBold);
            }
            if (textUnderline != null) {
                this.outputStream.write(textUnderline);
            }
            
            this.outputStream.write(textBytes, 0, maxlength);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return this;
    }
    
    /**
     * Print image with the connected printer.
     *
     * @param image Bytes contain the image in ESC/POS command
     * @return Fluent interface
     */
    public BluetoothPrinterSocketConnection printImage(byte[] image) {
        if (!this.isOpenedStream()) {
            return this;
        }
        try {
            this.outputStream.write(image);
            Thread.sleep(PrinterCommands.TIME_BETWEEN_TWO_PRINT * 2);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this;
    }
    
    /**
     * Print a barcode with the connected printer.
     *
     * @param barcodeType Set the barcode type. Use PrinterCommands.BARCODE_... constants
     * @param barcode String that contains code numbers
     * @param heightPx dot height of the barcode
     * @return Fluent interface
     */
    public BluetoothPrinterSocketConnection printBarcode(int barcodeType, String barcode, int heightPx) {
        if (!this.isOpenedStream()) {
            return this;
        }
        
        int barcodeLength = 0;
        
        switch (barcodeType) {
            case PrinterCommands.BARCODE_UPCA:
                barcodeLength = 11;
                break;
            case PrinterCommands.BARCODE_UPCE:
                barcodeLength = 6;
                break;
            case PrinterCommands.BARCODE_EAN13:
                barcodeLength = 12;
                break;
            case PrinterCommands.BARCODE_EAN8:
                barcodeLength = 7;
                break;
        }
        
        if (barcodeLength == 0 || barcode.length() < barcodeLength) {
            return this;
        }
    
        barcode = barcode.substring(0, barcodeLength);
        
        try {
            switch (barcodeType) {
                case PrinterCommands.BARCODE_UPCE:
                    String firstChar = barcode.substring(0, 1);
                    if (!firstChar.equals("0") && !firstChar.equals("1")) {
                        barcode = "0" + barcode.substring(0, 5);
                    }
                    break;
                case PrinterCommands.BARCODE_UPCA:
                case PrinterCommands.BARCODE_EAN13:
                case PrinterCommands.BARCODE_EAN8:
                    int stringBarcodeLength = barcode.length(), totalBarcodeKey = 0;
                    for (int i = 0; i < stringBarcodeLength; i++) {
                        int pos = stringBarcodeLength - 1 - i,
                            intCode = Integer.parseInt(barcode.substring(pos, pos + 1), 10);
                        if (i % 2 == 0) {
                            intCode = 3 * intCode;
                        }
                        totalBarcodeKey += intCode;
                    }
                    
                    String barcodeKey = String.valueOf(10 - (totalBarcodeKey % 10));
                    if (barcodeKey.length() == 2) {
                        barcodeKey = "0";
                    }
                    barcode += barcodeKey;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return this;
        }
    
        barcodeLength = barcode.length();
        byte[] barcodeCommand = new byte[barcodeLength + 4];
        System.arraycopy(new byte[]{0x1D, 0x6B, (byte) barcodeType}, 0, barcodeCommand, 0, 3);
        
        try {
            for (int i = 0; i < barcodeLength; i++) {
                barcodeCommand[i + 3] = (byte) (Integer.parseInt(barcode.substring(i, i + 1), 10) + 48);
            }
            
            this.outputStream.write(new byte[]{0x1D, 0x68, (byte) heightPx});
            this.outputStream.write(barcodeCommand);
            Thread.sleep(PrinterCommands.TIME_BETWEEN_TWO_PRINT * 2);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this;
    }



    /**
     * Print a QR code with the connected printer.
     *
     * @param qrCodeType Set the barcode type. Use PrinterCommands.QRCODE_... constants
     * @param text String that contains QR code data
     * @param size dot size of QR code pixel
     * @return Fluent interface
     */
    public BluetoothPrinterSocketConnection printQRCode(int qrCodeType, String text, int size) {
        if (!this.isOpenedStream()) {
            return this;
        }

        if(size < 1) {
            size = 1;
        } else if(size > 16) {
            size = 16;
        }


        try {

            this.outputStream.write(PrinterCommands.WESTERN_EUROPE_ENCODING);

            byte[] textBytes = text.getBytes("ISO-8859-1");

            int
                    commandLength = textBytes.length + 3,
                    pL = commandLength % 256,
                    pH = (int) Math.floor(commandLength / 256);

            /*byte[] qrCodeCommand = new byte[textBytes.length + 7];
            System.arraycopy(new byte[]{0x1B, 0x5A, 0x00, 0x00, (byte)size, (byte)pL, (byte)pH}, 0, qrCodeCommand, 0, 7);
            System.arraycopy(textBytes, 0, qrCodeCommand, 7, textBytes.length);
            this.outputStream.write(qrCodeCommand);*/

            this.outputStream.write(new byte[]{0x1D, 0x28, 0x6B, 0x04, 0x00, 0x31, 0x41, (byte)qrCodeType, 0x00});
            this.outputStream.write(new byte[]{0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43, (byte)size});
            this.outputStream.write(new byte[]{0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x45, (byte)48});

            byte[] qrCodeCommand = new byte[textBytes.length + 8];
            System.arraycopy(new byte[]{0x1D, 0x28, 0x6B, (byte)pL, (byte)pH, 0x31, 0x50, 0x30}, 0, qrCodeCommand, 0, 8);
            System.arraycopy(textBytes, 0, qrCodeCommand, 8, textBytes.length);
            this.outputStream.write(qrCodeCommand);

            this.outputStream.write(new byte[]{0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30});

            Thread.sleep(PrinterCommands.TIME_BETWEEN_TWO_PRINT * 2);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this;
    }
    /**
     * Forces the transition to a new line with the connected printer.
     *
     * @return Fluent interface
     */
    public BluetoothPrinterSocketConnection newLine() {
        return this.newLine(null);
    }
    
    /**
     * Forces the transition to a new line and set the alignment of text and barcodes with the connected printer.
     * @param align Set the alignment of text and barcodes. Use PrinterCommands.TEXT_ALIGN_... constants
     * @return Fluent interface
     */
    public BluetoothPrinterSocketConnection newLine(byte[] align) {
        if (!this.isOpenedStream()) {
            return this;
        }
        
        try {
            this.outputStream.write(PrinterCommands.LF);
            Thread.sleep(PrinterCommands.TIME_BETWEEN_TWO_PRINT);
            if (align != null) {
                this.outputStream.write(align);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        return this;
    }
}
