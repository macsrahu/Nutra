package com.sales.numax.printing.textparser;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.sales.numax.printing.Printer;
import com.sales.numax.printing.bluetooth.BluetoothPrinterSocketConnection;


public class PrinterTextParserImg implements PrinterTextParserElement {
    
    /**
     * Convert Drawable instance to a hexadecimal string of the image data.
     *
     * @param printer A Printer instance that will print the image.
     * @param drawable Drawable instance to be converted.
     * @return A hexadecimal string of the image data. Empty string if Drawable cannot be cast to BitmapDrawable.
     */
    public static String bitmapToHexadecimalString(Printer printer, Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return PrinterTextParserImg.bitmapToHexadecimalString(printer, (BitmapDrawable) drawable);
        }
        return "";
    }
    
    /**
     * Convert BitmapDrawable instance to a hexadecimal string of the image data.
     *
     * @param printer A Printer instance that will print the image.
     * @param bitmapDrawable BitmapDrawable instance to be converted.
     * @return A hexadecimal string of the image data.
     */
    public static String bitmapToHexadecimalString(Printer printer, BitmapDrawable bitmapDrawable) {
        return PrinterTextParserImg.bitmapToHexadecimalString(printer, bitmapDrawable.getBitmap());
    }
    
    /**
     * Convert Bitmap instance to a hexadecimal string of the image data.
     *
     * @param printer A Printer instance that will print the image.
     * @param bitmap Bitmap instance to be converted.
     * @return A hexadecimal string of the image data.
     */
    public static String bitmapToHexadecimalString(Printer printer, Bitmap bitmap) {
        return PrinterTextParserImg.bytesToHexadecimalString(printer.bitmapToBytes(bitmap));
    }
    
    /**
     * Convert byte array to a hexadecimal string of the image data.
     *
     * @param bytes Bytes contain the image in ESC/POS command.
     * @return A hexadecimal string of the image data.
     */
    public static String bytesToHexadecimalString(byte[] bytes) {
        StringBuilder imageHexString = new StringBuilder();
        for (byte aByte : bytes) {
            String hexString = Integer.toHexString(aByte & 0xFF);
            if (hexString.length() == 1) {
                hexString = "0" + hexString;
            }
            imageHexString.append(hexString);
        }
        return imageHexString.toString();
    }
    
    /**
     * Convert hexadecimal string of the image data to bytes ESC/POS command.
     *
     * @param hexString Hexadecimal string of the image data.
     * @return Bytes contain the image in ESC/POS command.
     */
    public static byte[] hexadecimalStringToBytes(String hexString) {
        byte[] bytes = new byte[0];
        
        try {
            bytes = new byte[hexString.length() / 2];
            for (int i = 0; i < bytes.length; i++) {
                int pos = i * 2;
                bytes[i] = (byte) Integer.parseInt(hexString.substring(pos, pos + 2), 16);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return bytes;
    }
    
    
    private int length;
    private byte[] image;
    
    /**
     * Create new instance of PrinterTextParserImg.
     *
     * @param printerTextParserColumn Parent PrinterTextParserColumn instance.
     * @param textAlign Set the image alignment. Use PrinterTextParser.TAGS_ALIGN_... constants.
     * @param hexadecimalString Hexadecimal string of the image data.
     */
    public PrinterTextParserImg(PrinterTextParserColumn printerTextParserColumn, String textAlign, String hexadecimalString) {
        this(printerTextParserColumn, textAlign, PrinterTextParserImg.hexadecimalStringToBytes(hexadecimalString));
    }

    /**
     * Create new instance of PrinterTextParserImg.
     *
     * @param printerTextParserColumn Parent PrinterTextParserColumn instance.
     * @param textAlign Set the image alignment. Use PrinterTextParser.TAGS_ALIGN_... constants.
     * @param image Bytes contain the image in ESC/POS command.
     */
    public PrinterTextParserImg(PrinterTextParserColumn printerTextParserColumn, String textAlign, byte[] image) {
        Printer printer = printerTextParserColumn.getLine().getTextParser().getPrinter();

        int byteWidth = ((int) image[4] & 0xFF),
                width = byteWidth * 8,
                height = ((int) image[6] & 0xFF),
                nbrByteDiff = (int) Math.floor(((float) (printer.getPrintingWidthPx() - width)) / 8f),
                nbrWhiteByteToInsert = 0;

        switch (textAlign) {
            case PrinterTextParser.TAGS_ALIGN_CENTER:
                nbrWhiteByteToInsert = Math.round(((float) nbrByteDiff) / 2f);
                break;
            case PrinterTextParser.TAGS_ALIGN_RIGHT:
                nbrWhiteByteToInsert = nbrByteDiff;
                break;
        }

        if (nbrWhiteByteToInsert > 0) {
            int newByteWidth = byteWidth + nbrWhiteByteToInsert;
            byte[] newImage = new byte[newByteWidth * height + 8];
            System.arraycopy(image, 0, newImage, 0, 8);
            newImage[4] = (byte) newByteWidth;
            for (int i = 0; i < height; i++) {
                System.arraycopy(image, (byteWidth * i + 8), newImage, (newByteWidth * i + nbrWhiteByteToInsert + 8), byteWidth);
            }
            image = newImage;
        }

        this.length = (int) Math.ceil(((float) (((int) image[4] & 0xFF) * 8)) / ((float) printer.getCharSizeWidthPx()));
        this.image = image;
    }
    @Override
    public int length() {
        return this.length;
    }

    /**
     * Print image
     *
     * @param printerSocket Bluetooth printer socket connection
     * @return this Fluent method
     */
    @Override
    public PrinterTextParserImg print(BluetoothPrinterSocketConnection printerSocket) {
        printerSocket.printImage(this.image);
        return this;
    }

    /**
     * Get the image width in char length.
     *
     * @return int
     */

}
