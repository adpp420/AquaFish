package com.febino.dependencies;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.febino.DatabaseManager.CopyCursor;
import com.febino.DatabaseManager.DataBaseManager;
import com.febino.dataclass.BillDetails;
import com.febino.dataclass.OrderDetails;
import com.febino.dataclass.ProductDetails;
import com.febino.dataclass.TraderDetails;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import androidx.annotation.RequiresApi;

/**
 * Created by https://goo.gl/UAfmBd on 2/6/2017.
 */

public class Printer {

    private OutputStream outputStream;
    private Context context;

    private String machineID = "";
    private boolean isTimeFormat24hr;

    public static final byte HT = 0x9;
    public static final byte LF = 0x0A;
    public static final byte CR = 0x0D;
    public static final byte ESC = 0x1B;
    public static final byte DLE = 0x10;
    public static final byte GS = 0x1D;
    public static final byte FS = 0x1C;
    public static final byte STX = 0x02;
    public static final byte US = 0x1F;
    public static final byte CAN = 0x18;
    public static final byte CLR = 0x0C;
    public static final byte EOT = 0x04;

    public static final byte[] INIT = {27, 64};
    public static byte[] FEED_LINE = {10};

    public static byte[] SELECT_FONT_A = {20, 33, 0};

    public static byte[] SET_BAR_CODE_HEIGHT = {29, 104, 100};
    public static byte[] PRINT_BAR_CODE_1 = {29, 107, 2};
    public static byte[] SEND_NULL_BYTE = {0x00};

    public static byte[] SELECT_PRINT_SHEET = {0x1B, 0x63, 0x30, 0x02};
    public static byte[] FEED_PAPER_AND_CUT = {0x1D, 0x56, 66, 0x00};

    public static byte[] SELECT_CYRILLIC_CHARACTER_CODE_TABLE = {0x1B, 0x74, 0x11};

    public static byte[] SELECT_BIT_IMAGE_MODE = {0x1B, 0x2A, 33, -128, 0};
    public static byte[] SET_LINE_SPACING_24 = {0x1B, 0x33, 24};
    public static byte[] SET_LINE_SPACING_30 = {0x1B, 0x33, 30};

    public static byte[] TRANSMIT_DLE_PRINTER_STATUS = {0x10, 0x04, 0x01};
    public static byte[] TRANSMIT_DLE_OFFLINE_PRINTER_STATUS = {0x10, 0x04, 0x02};
    public static byte[] TRANSMIT_DLE_ERROR_STATUS = {0x10, 0x04, 0x03};
    public static byte[] TRANSMIT_DLE_ROLL_PAPER_SENSOR_STATUS = {0x10, 0x04, 0x04};

    public static final byte[] ESC_FONT_COLOR_DEFAULT = new byte[] { 0x1B, 'r',0x00 };
//    public static final byte[] ESC_FONT_COLOR_DEFAULT = new byte[] { 0x1B, 'r',0x00 };
    public static final byte[] FS_FONT_ALIGN = new byte[] { 0x1C, 0x21, 0x01, 0x1B, 0x21, 0x01 };
    public static final byte[] ESC_ALIGN_LEFT = new byte[] { 0x1b, 'a', 0x00 };
    public static final byte[] ESC_ALIGN_RIGHT = new byte[] { 0x1b, 'a', 0x02 };
    public static final byte[] ESC_ALIGN_CENTER = new byte[] { 0x1b, 'a', 0x01 };
    public static final byte[] ESC_CANCEL_BOLD = new byte[] { 0x1B, 0x45, 0 };


    /*********************************************/
    public static final byte[] ESC_HORIZONTAL_CENTERS = new byte[] { 0x1B, 0x44, 20, 28, 00};
    public static final byte[] ESC_CANCLE_HORIZONTAL_CENTERS = new byte[] { 0x1B, 0x44, 00 };
    /*********************************************/

    public static final byte[] ESC_ENTER = new byte[] { 0x1B, 0x4A, 0x40 };
    public static final byte[] PRINTE_TEST = new byte[] { 0x1D, 0x28, 0x41 };


    public static final byte[] FULL_CUT = new byte[]{0x1D, 0x56, 0x00}; // Full cut
    // or
    public static final byte[] HALF_CUT = new byte[]{0x1D, 0x56, 0x01}; // Partial cut (if supported)



    String printerAddress = null;
    BluetoothDevice device;
    private BluetoothAdapter bluetoothAdapter;
    BluetoothSocket btSocket = null;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    ArrayList<OrderDetails> orderDetailsArrayList;
    TraderDetails traderDetails;
    BillDetails billDetails;

    CopyCursor copyCursor;

    DataBaseManager dataBaseManager;
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public Printer(Context context, String printerAddress) {

        this.context = context;
        this.printerAddress = printerAddress;
        Log.i("Printer Address", printerAddress);

        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        device = bluetoothAdapter.getRemoteDevice(printerAddress);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void printCompanyHeader() throws IOException {

//        String companyName = "S.M.K. FISH CENTRE";
        String companyName = "S.P.F. டேங்க் மீன், ஈரோடு";
//                        printTamilText(companyName,50);
        printCustom(companyName,3,1);

        String companyQuote = "லோகு, கட்லா, ௫பா, பங்காஸ், ஜிலேபி, அனைத்து வகை மீன் வியாபாரம்";
        printTamilText(companyQuote,25);

//        String shop1 = "கடை 1 :810/821, மேட்டுர் பாலம், ATRIUM ஹோட்டல் அருகில், ஈரோடு - 638009";
//        printTamilText(shop1,15);
//
//        String shop2 = "கடை 2 :கேர் 24 ஹாஸ்பிடல் அருகில், அம்மன் நகர், பெருந்துறை ரோடு, ஈரோடு - 9";
//        printTamilText(shop2,15);

        String phone1 = "Ph: 9952483992";
        printTamilText(phone1,20);

//        String phone2 = "M கலைவாணி - 98659 37270, 81440 87270";
//        printTamilText(phone2,20);

//        String phone3 = "T முருகேசன் - 90803 46466, 91714 22312";
//        printTamilText(phone3,20);
        printCustom(new String(new char[64]).replace("\0", "-"),0,1);

    }
    public void printReceipt(DataBaseManager dataBaseManager,BillDetails billDetails, TraderDetails traderDetails, ArrayList<OrderDetails> orderDetailsArrayList) {
        try {

            this.dataBaseManager = dataBaseManager;
            copyCursor = new CopyCursor();

            connectPrinter();

            if(btSocket != null) {

                printCompanyHeader();

                printTamilText(traderDetails.name,25);
                printCustom("Bill No   : "+billDetails.getBillNo(),1,0);
                printCustom("Bill Date : "+billDetails.getBillDate(),1,0);


                printCustom(new String(new char[64]).replace("\0", "-"),0,1);
//                        printCustom("   Date        Breed        Box     Kg     Rate         Amount  ",0,1);
//                printTamilText("    Date           Breed         Box      Kg      Rate         Amount",22);
                printTamilText(" Date          Breed                Box      Kg           Rate       Amount",22);
                printCustom(new String(new char[64]).replace("\0", "-"),0,1);

                List<String[]> lines = new ArrayList<>();

                String savedDate = "";
                for(int i=0;i<orderDetailsArrayList.size();i++){
                    OrderDetails orderDetails = orderDetailsArrayList.get(i);
                    ProductDetails productDetails = copyCursor.copyProductFromCursor(dataBaseManager.getProductFromProductTableByID(orderDetails.getProductID()));
//                    printTamilText("20-05-2025        Breed        10       20.0     50.0        18500.00",22);
//                    printCustom("20-05-2025     Breed        10     20.0    50.0          18500.0",0,1);

                    float amount = (orderDetails.getTotalKG()+(orderDetails.getTotalBox()*35))*orderDetails.getRatePerKG();

                    String changedDate = " ";
                    if (!savedDate.equals(orderDetails.getOrderDate())) {
                        savedDate = orderDetails.getOrderDate();
                        changedDate = orderDetails.getOrderDate();
                    }



                    lines.add(new String[]{changedDate, productDetails.productName, ""+orderDetails.getTotalBox(), ""+orderDetails.getTotalKG(), ""+orderDetails.getRatePerKG(), ""+amount});
//                    lines.add(formatLine("2024-05-15", "Country", 8, 9.50f, 130.00f, 1235.00f));


//                    String[] orderArray = {changedDate + "", productDetails.productName, "" + orderDetails.getTotalBox(), "" + orderDetails.getTotalKG(), "" + orderDetails.getRatePerKG(), "" + amount};
//                    printTamilText(textAlign(orderArray),22, false);

//                    printTamilText(orderDetails.getOrderDate()+"        "+productDetails.productName+"       "+orderDetails.getTotalBox()+"     "+orderDetails.getTotalKG()+"   "+orderDetails.getRatePerKG()+"       "+amount,22);
                }

                Bitmap billBitmap = drawTableWithTamil(lines, 20f, 576);
                byte[] bytes = convertBitmapToRasterESC(billBitmap);
                outputStream.write(bytes);
                outputStream.flush();

                printCustom(new String(new char[64]).replace("\0", "-"),0,1);

//                printCustom("Bill Amount     "+ billDetails.getBillAmount(),0,2);
//                printCustom("Balance     "+ billDetails.getBalanceAmount(),0,2);
//                printCustom("Bill + Balance     "+ (billDetails.getBalanceAmount() + billDetails.getBillAmount()),0,2);
//                printCustom("Old Balance    "+ billDetails.getOldBalanceAmount() ,0,2);

                lines.clear();
                lines.add(new String[]{"", "Bill Amount", "", "", "", ""+billDetails.getBillAmount()});
                lines.add(new String[]{"", "Balance", "", "", "", ""+billDetails.getBalanceAmount()});
                lines.add(new String[]{"", "Bill + Balance", "", "", "", ""+(billDetails.getBalanceAmount() + billDetails.getBillAmount())});
                lines.add(new String[]{"", "Old Balance", "", "", "", ""+billDetails.getOldBalanceAmount()});

                billBitmap = drawTableWithTamil(lines, 20f, 576);
                bytes = convertBitmapToRasterESC(billBitmap);
                outputStream.write(bytes);
                outputStream.flush();


                printCustom(new String(new char[64]).replace("\0", "-"),0,1);
                lines.clear();
                lines.add(new String[]{"", "Total", "", "", "", ""+(billDetails.getBillAmount() + billDetails.getBalanceAmount() + billDetails.getOldBalanceAmount())});

                billBitmap = drawTableWithTamil(lines, 20f, 576);
                bytes = convertBitmapToRasterESC(billBitmap);
                outputStream.write(bytes);
                outputStream.flush();

//                printCustom("Total    "+ (billDetails.getBillAmount() + billDetails.getBalanceAmount() + billDetails.getOldBalanceAmount()) ,0,2);
                printCustom(new String(new char[64]).replace("\0", "-"),0,1);

                for(int j = 0; j < 3 ; j++)
                printNewLine();
                fullCut();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    public void printReceipt(){
                try {

                    connectPrinter();
                    if(btSocket != null) {


                        String currentDate;
                        if(isTimeFormat24hr)
                            currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
                        else
                            currentDate = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).format(new Date());

                        printCustom(new String(new char[64]).replace("\0", "-"),0,1);

                        printCustom(alignLabelValue("Date",currentDate),0,1);
                        printNewLine();
                        printNewLine();
                        printCompanyHeader();


                        printNewLine();
                        printNewLine();
                        printNewLine();

                        fullCut();

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }catch (Exception e){
                    e.printStackTrace();
                }
//            }
//
//        }).start();

    }

    public void printTamilText(String tamilText, int textSize) throws IOException {
//        Bitmap bitmap = textAsBitmap(tamilText, 50); // Tamil greeting
        Bitmap bitmap = textToCenteredBitmap(tamilText, textSize); // Tamil greeting

        byte[] bytesToPrint = convertBitmapToRasterESC(bitmap);


        int chunkSize = 512;  // You can also use 256 or 1024 based on your printer stability

        for (int i = 0; i < bytesToPrint.length; i += chunkSize) {
            int end = Math.min(bytesToPrint.length, i + chunkSize);
            outputStream.write(bytesToPrint, i, end - i);
            outputStream.flush();
        }

    }

    public void printTamilText(String tamilText, int textSize,boolean isCenter) throws IOException {
//        Bitmap bitmap = textAsBitmap(tamilText, 50); // Tamil greeting
        Bitmap bitmap;
//        if(isCenter)
//            bitmap = textToCenteredBitmap(tamilText, textSize); // Tamil greeting
//
//        else

        outputStream.write(ESC_ALIGN_LEFT);
        bitmap = textAsBitmap(tamilText, textSize); // Tamil greeting


        byte[] bytesToPrint = convertBitmapToRasterESC(bitmap);


        int chunkSize = 512;  // You can also use 256 or 1024 based on your printer stability

        for (int i = 0; i < bytesToPrint.length; i += chunkSize) {
            int end = Math.min(bytesToPrint.length, i + chunkSize);
            outputStream.write(bytesToPrint, i, end - i);
            outputStream.flush();
        }

    }

    public byte[] hexStringToByteArray(String hexString) {
        hexString = hexString.replaceAll("\\s+", ""); // remove spaces
        int len = hexString.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i+1), 16));
        }

        return data;
    }

    private Bitmap resizeBitmap(Bitmap input, int maxWidth) {
        float aspectRatio = (float) input.getHeight() / input.getWidth();
        int height = (int) (maxWidth * aspectRatio);
        return Bitmap.createScaledBitmap(input, maxWidth, height, false);
    }

    public Bitmap previewBitmapToESCPOS(Bitmap bitmap,int size) {
        // Resize to fit 384px width (adjust as per your printer spec)
        size = size < 100 ? 100 : size;
        int maxWidth = (size > 576) ? 576 : size; // 576 in max

        return resizeBitmap(bitmap, maxWidth);
    }

    private Bitmap trimWhiteBottom(Bitmap bmp) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();

        int lastRow = height - 1;
        boolean rowIsWhite;

        for (; lastRow >= 0; lastRow--) {
            rowIsWhite = true;
            for (int x = 0; x < width; x++) {
                int pixel = bmp.getPixel(x, lastRow);
                int gray = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3;
                if (gray < 245) { // Not white
                    rowIsWhite = false;
                    break;
                }
            }
            if (!rowIsWhite) break;
        }

        return Bitmap.createBitmap(bmp, 0, 0, width, lastRow + 1);
    }

    public byte[] convertBitmapToESCPOS(Bitmap bitmap,int size) {
        // Resize to fit 384px width (adjust as per your printer spec)
        size = size < 100 ? 100 : size;
        int maxWidth = (size > 576) ? 576 : size; // 576 in max

        bitmap = resizeBitmap(bitmap, maxWidth);

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        List<Byte> imageBytes = new ArrayList<>();

        Log.i("width", ""+width);
        Log.i("height", "" + height);

        // Line spacing to default
        imageBytes.add((byte) 0x1B);
        imageBytes.add((byte) 0x33);
        imageBytes.add((byte) 0x00);

        for (int y = 0; y < height; y += 24) {
            imageBytes.add((byte) 0x1B); // ESC
            imageBytes.add((byte) 0x2A); // *
            imageBytes.add((byte) 0x21); // 24-dot double-density
            imageBytes.add((byte) (width % 256)); // width low byte
            imageBytes.add((byte) (width / 256)); // width high byte

            for (int x = 0; x < width; x++) {
                for (int k = 0; k < 3; k++) {
                    byte slice = 0;
                    for (int b = 0; b < 8; b++) {
                        int yIndex = y + (k * 8) + b;
                        int pixel = 0;
                        if (yIndex < height) {
                            pixel = bitmap.getPixel(x, yIndex);
                        }
                        int gray = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3;
                        int threshold = (yIndex > height - 10) ? 220 : 150;
                        if (gray < threshold) {
                            slice |= (1 << (7 - b));
                        }
//                        if (gray < 150) {
//                            slice |= (1 << (7 - b));
//                        }
                    }
                    imageBytes.add(slice);
                }
            }

            // New line
            imageBytes.add((byte) 0x0A);
        }

        // Convert to byte array
        byte[] bytes = new byte[imageBytes.size()];
        for (int i = 0; i < imageBytes.size(); i++) {
            bytes[i] = imageBytes.get(i);
        }

        return bytes;
    }

    public static byte[] convertBitmapToRasterESC(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Each byte encodes 8 horizontal pixels → width in bytes
        int widthBytes = (int) Math.ceil(width / 8.0);
        byte[] imageBytes = new byte[widthBytes * height];

        // Create monochrome data
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bitmap.getPixel(x, y);
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);
                int gray = (r + g + b) / 3;

                if (gray < 160) {
                    imageBytes[y * widthBytes + (x / 8)] |= (byte)(0x80 >> (x % 8));
                }
            }
        }

        // ESC/POS raster header
        List<Byte> command = new ArrayList<>();

        // GS v 0 m
        command.add((byte) 0x1D);
        command.add((byte) 0x76);
        command.add((byte) 0x30);
        command.add((byte) 0x00); // mode: normal

        // width in bytes (little endian)
        command.add((byte) (widthBytes % 256));
        command.add((byte) (widthBytes / 256));

        // height in dots (little endian)
        command.add((byte) (height % 256));
        command.add((byte) (height / 256));

        // image data
        for (byte b : imageBytes) {
            command.add(b);
        }

        // Convert to byte[]
        byte[] bytes = new byte[command.size()];
        for (int i = 0; i < command.size(); i++) {
            bytes[i] = command.get(i);
        }

        return bytes;
    }


    public String formatLine(String date, String breed, int box, float kg, float rate, float amount) {
        return String.format(
                "%-10s %-8s %4d %6.2f %7.2f %9.2f",
                date, breed, box, kg, rate, amount
        );
    }

    public Bitmap drawTableWithTamil(List<String[]> rows, float textSize, int canvasWidth) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setTextSize(textSize);
        paint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));  // Use custom Tamil font if needed

        float lineHeight = textSize + 12;
        int height = (int) (rows.size() * lineHeight + 20);

        Bitmap bitmap = Bitmap.createBitmap(canvasWidth, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        // Fixed X positions (adjust as needed for 384px or 576px printers)
        float xDate   = 0;
        float xBreed  = 120;
        float xBox    = 290;
        float xKg     = 360;
        float xRate   = 450;
        float xAmount = 560;

        float y = lineHeight;

        for (String[] row : rows) {
            if (row.length < 6) continue;

            // Left-align Tamil text
            canvas.drawText(row[0], xDate, y, paint);   // Date
            canvas.drawText(row[1], xBreed, y, paint);  // Breed (Tamil)

            // Right-align numbers
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(row[2], xBox, y, paint);    // Box
            canvas.drawText(row[3], xKg, y, paint);     // Kg
            canvas.drawText(row[4], xRate, y, paint);   // Rate
            canvas.drawText(row[5], xAmount, y, paint); // Amount

            paint.setTextAlign(Paint.Align.LEFT); // reset for Tamil
            y += lineHeight;
        }

        return bitmap;
    }


    public Bitmap textBlockToBitmap(List<String> lines, float textSize, int canvasWidth) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.MONOSPACE);  // Fixed-width font for alignment

        float lineHeight = textSize + 8;
        int height = (int) (lines.size() * lineHeight + 20);

        Bitmap bitmap = Bitmap.createBitmap(canvasWidth, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        float y = lineHeight;
        for (String line : lines) {
            canvas.drawText(line, 0, y, paint);
            y += lineHeight;
        }

        return bitmap;
    }




    public Bitmap textAsBitmap(String text, float textSize) {

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(Color.BLACK);

        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL)); // or use custom Tamil font

        float baseline = -paint.ascent();
        int width = (int) (paint.measureText(text) + 0.5f);
        int height = (int) (baseline + paint.descent() + 0.5f);

        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawColor(Color.WHITE);
        canvas.drawText(text, 0, baseline, paint);
        return image;
    }

    public Bitmap textToCenteredBitmap(String text, float textSize) {

        int canvasWidth = 576;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setTextSize(textSize);
        paint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        paint.setTextAlign(Paint.Align.LEFT);  // we'll calculate position manually

        // Measure text size
        Rect textBounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), textBounds);
        float textWidth = paint.measureText(text);
        float textHeight = textBounds.height();

        // Create bitmap (enough height + padding)
        int padding = 5;
        Bitmap bitmap = Bitmap.createBitmap(canvasWidth, (int) (textHeight + 2 * padding), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        // Calculate X for centered text
        float x = (canvasWidth - textWidth) / 2;
        float y = padding - textBounds.top;

        // Draw text centered
        canvas.drawText(text, x, y, paint);

        return bitmap;
    }


    public byte[] convertBitmapToThermalPrinter(Bitmap bitmap,int size) {

        int maxWidth = (size > 576) ? 576 : size; // 576 in max

//        bitmap = resizeBitmap(bitmap, maxWidth);

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        List<Byte> imageBytes = new ArrayList<>();



//        imageBytes.add((byte) 0x1B); imageBytes.add((byte) 0x33); imageBytes.add((byte) 0x00); // line spacing
        imageBytes.add((byte) 0x1B); imageBytes.add((byte) 0x21); imageBytes.add((byte) 0x00); // Center command
        imageBytes.add((byte) 0x1B); imageBytes.add((byte) 0x61); imageBytes.add((byte) 0x01); // normal text command
        imageBytes.add((byte) 0x1B); imageBytes.add((byte) 0x61); imageBytes.add((byte) 0x01); // normal text command
        imageBytes.add((byte) 0x1D); imageBytes.add((byte) 0x76); imageBytes.add((byte) 0x30); // image command



        Log.i("width", ""+width);
        Log.i("height", ""+height);

        imageBytes.add((byte) 0x00); //Low Width
        imageBytes.add((byte) width); //High Width

        imageBytes.add((byte) 0x00); //Low Height
        imageBytes.add((byte) height); //Low Height




        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                byte slice = 0;
                int pixel = bitmap.getPixel(x, y);
//                Log.i("pixels", pixel + "");
                int red = Color.red(pixel);
                int green = Color.green(pixel);
                int blue = Color.blue(pixel);
                int alpha = Color.alpha(pixel);

//                int red   = (pixel >> 16) & 0xFF;
//                int green = (pixel >> 8) & 0xFF;
//                int blue  = (pixel) & 0xFF;
//                int alpha = (pixel >> 24) & 0xFF;

                int gray = 255 - ( (red + blue + green) / 3);
//                Log.i("pixels - gray (a, r,g,b)", pixel + " - "+gray+" (" + alpha + ","+ red + ","+ green + ","+ blue + ")");

//                if (gray < 128) slice |= (1 << (7 - b));
                if (gray < 128) slice = 0x00;
                else slice = (byte) gray;

                imageBytes.add(slice);

            }
        }

        imageBytes.add((byte) 0x1B); imageBytes.add((byte) 0x61); imageBytes.add((byte) 0x00); //End Image command


        imageBytes.add((byte) 0x0A); //New Line
        imageBytes.add((byte) 0x0A); //New Line


        byte[] bytes = new byte[imageBytes.size()];
        for (int i = 0; i < imageBytes.size(); i++) {
            bytes[i] = imageBytes.get(i);
//            Log.i("" + i, "" + String.format("%02X",bytes[i]));
        }


        return bytes;
    }




    public void printTest (String text){
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    connectPrinter();

                    device = bluetoothAdapter.getRemoteDevice(printerAddress);

                    if(btSocket != null) {
                        String currentDate;
                        if(isTimeFormat24hr)
                            currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
                        else
                            currentDate = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).format(new Date());

                        printCustom(new String(new char[42]).replace("\0", "-"),0,1);
                        printCustom(text,0,1);
                        printCustom(alignLabelValue("Date",currentDate),0,1);

                        printCustom(new String(new char[42]).replace("\0", "-"),0,1);

                        printText();
                        printNewLine();
                        printNewLine();



                        fullCut();
//                        halfCut();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

        }).start();
    }

    private void printText(){
        try {

            byte[] TXT_SMALL      = {0x1b,0x21,0x01}; // Small text
            byte[] TXT_NORMAL     = {0x1b,0x21,0x00}; // Normal text
            byte[] TXT_MEDIUM      = {0x1b,0x21,0x21}; // Medium text
            byte[] TXT_LARGE      = {0x1b,0x21,0x32}; // Large text



//            byte[] TXT_2HEIGHT     = {0x1b,0x21,0x10}; // Double height text
//            byte[] TXT_2WIDTH      = {0x1b,0x21,0x20}; // Double width text
//            byte[] TXT_4SQUARE     = {0x1b,0x21,0x30}; // Quad area text
//            byte[] TXT_UNDERL_OFF  = {0x1b,0x2d,0x00}; // Underline font OFF
//            byte[] TXT_UNDERL_ON   = {0x1b,0x2d,0x01}; // Underline font 1-dot ON
//            byte[] TXT_UNDERL2_ON  = {0x1b,0x2d,0x02}; // Underline font 2-dot ON
//            byte[] TXT_BOLD_OFF    = {0x1b,0x45,0x00}; // Bold font OFF
//            byte[] TXT_BOLD_ON     = {0x1b,0x45,0x01}; // Bold font ON
//            byte[] TXT_FONT_A      = {0x1b,0x4d,0x48}; // Font type A
//            byte[] TXT_FONT_B      = {0x1b,0x4d,0x01};// Font type B
//            byte[] TXT_ALIGN_LT    = {0x1b,0x61,0x00}; // Left justification
//            byte[] TXT_ALIGN_CT    = {0x1b,0x61,0x01}; // Centering
//            byte[] TXT_ALIGN_RT    = {0x1b,0x61,0x02}; // Right justification



            byte[][] size = {
                    TXT_SMALL,
                    TXT_NORMAL,
                    TXT_MEDIUM,
                    TXT_LARGE,

//                    TXT_2HEIGHT,
//                    TXT_2WIDTH,
//                    TXT_4SQUARE,
//                    TXT_UNDERL_OFF,
//                    TXT_UNDERL_ON,
//                    TXT_UNDERL2_ON,
//                    TXT_BOLD_OFF,
//                    TXT_BOLD_ON,
//                    TXT_FONT_A,
//                    TXT_FONT_B,
//                    TXT_ALIGN_LT, //ok
//                    TXT_ALIGN_CT, //ok
//                    TXT_ALIGN_RT  //ok
            };


//            outputStream.write(TXT_FONT_A);

            for(int i = 0;i<size.length;i++){
//                byte[] textSize = new byte[]{0x1D, 0x21, 0x11};
                outputStream.write(size[i]);
                outputStream.write("This is test A\n".getBytes());

            }

//            outputStream.write(TXT_FONT_B);


//            for(int i = 0;i<size.length;i++){
////                byte[] textSize = new byte[]{0x1D, 0x21, 0x11};
//                outputStream.write(size[i]);
//                outputStream.write("This is test B\n".getBytes());
//
//            }

//            byte[] textSize = new byte[]{0x1D, 0x21, 0x11};

//            outputStream.write(TXT_NORMAL);
//            outputStream.write(TXT_FONT_A);
//            outputStream.write(TXT_UNDERL_OFF);
//            outputStream.write(TXT_BOLD_OFF);



//
////            outputStream.write("This is test Big Text Line\n".getBytes());
//            outputStream.write(("System: KPC307-UB \n" +
//                    "SNO: 0OOO \n" +
//                    "Command mode: EPSON(ESC/POS)" +
//                    "Interface: USB\n" +
//                    "\t\t&Bluetooth\n" +
//
//                    "\n").getBytes());
//
//
//            byte[] normalSize = new byte[]{0x1D, 0x21, 0x00};
//            outputStream.write(normalSize);
//            outputStream.write("Normal Text Line\n".getBytes());
//
//
//            byte[] boldWithMedium = new byte[]{0x1D,0x21,0x01}; // 2- bold with medium text
//            outputStream.write(boldWithMedium);
//            outputStream.write("Medium Bold Text Line\n".getBytes());
//
//            byte[] boldWithLarge = new byte[]{0x1D,0x21,0x10}; // 2- bold with medium text
//            outputStream.write(boldWithLarge);
//            outputStream.write("Large Bold Text Line\n".getBytes());

            outputStream.flush();
        } catch (Exception exception) {

        }

    }


    public void printCustom(String msg, int size, int align) {
        //Print config "mode"
//        byte[] cc = new byte[]{0x1D,0x21,0x00};  // 0- normal size text
//        //byte[] cc1 = new byte[]{0x1B,0x21,0x00};  // 0- normal size text
//        byte[] bb = new byte[]{0x1D,0x21,0x01};  // 1- only bold text
//        byte[] bb2 = new byte[]{0x1D,0x21,0x20}; // 2- bold with medium text
//        byte[] bb3 = new byte[]{0x1D,0x21,0x10}; // 3- bold with large text

        byte[] TXT_SMALL      = {0x1b,0x21,0x01}; // Small text
        byte[] TXT_NORMAL     = {0x1b,0x21,0x00}; // Normal text
        byte[] TXT_MEDIUM      = {0x1b,0x21,0x21}; // Medium text
        byte[] TXT_LARGE      = {0x1b,0x21,0x32}; // Large text


        try {
            switch (size){
                case 0:
                    outputStream.write(TXT_SMALL);
                    break;
                case 1:
                    outputStream.write(TXT_NORMAL);
                    break;
                case 2:
                    outputStream.write(TXT_MEDIUM);
                    break;
                case 3:
                    outputStream.write(TXT_LARGE);
                    break;
            }

            switch (align){
                case 0:
                    //left align
                    outputStream.write(Printer.ESC_ALIGN_LEFT);
                    break;
                case 1:
                    //center align
                    outputStream.write(Printer.ESC_ALIGN_CENTER);
                    break;
                case 2:
                    //right align
                    outputStream.write(Printer.ESC_ALIGN_RIGHT);
                    break;
            }
            outputStream.write(msg.getBytes());
            outputStream.write(Printer.LF);
            //outputStream.write(cc);
            //printNewLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String textAlign(String[] dataString){

        StringBuffer stringBuffer = new StringBuffer();
        String temp;

        int maxCharLength;
        int charLength;
        String currentString;

        maxCharLength = 10 ;
        currentString = dataString[0];
        charLength = currentString.length() > maxCharLength ? maxCharLength : currentString.length();
        temp= currentString + new String(new char[maxCharLength-charLength]).replace("\0"," ") ;
        stringBuffer.append(temp);
        stringBuffer.append("  ");

        maxCharLength = 10 ;
        currentString = dataString[1];
        charLength = currentString.length() > maxCharLength ? maxCharLength : currentString.length();
        temp= currentString + new String(new char[maxCharLength-charLength]).replace("\0"," ") ;
        stringBuffer.append(temp);
        stringBuffer.append("  ");


        maxCharLength = 2 ;
        currentString = dataString[2];
        charLength = currentString.length() > maxCharLength ? maxCharLength : currentString.length();
        temp= new String(new char[maxCharLength-charLength]).replace("\0"," ") + currentString;
        stringBuffer.append(temp);
        stringBuffer.append("  ");


        maxCharLength = 4 ;
        currentString = dataString[3];
        charLength = currentString.length() > maxCharLength ? maxCharLength : currentString.length();
        temp= new String(new char[maxCharLength-charLength]).replace("\0"," ") + currentString;
        stringBuffer.append(temp);
        stringBuffer.append("  ");

        maxCharLength = 5 ;
        currentString = dataString[4];
        charLength = currentString.length() > maxCharLength ? maxCharLength : currentString.length();
        temp= new String(new char[maxCharLength-charLength]).replace("\0"," ") + currentString;
        stringBuffer.append(temp);
        stringBuffer.append("  ");

        maxCharLength = 8 ;
        currentString = dataString[5];
        charLength = currentString.length() > maxCharLength ? maxCharLength : currentString.length();
        temp= new String(new char[maxCharLength-charLength]).replace("\0"," ") + currentString;
        stringBuffer.append(temp);
        stringBuffer.append(" ");

        return stringBuffer.toString();

    }

    public String stringAlign(String[] dataString){
        StringBuffer stringBuffer = new StringBuffer();
        String temp;

        if(dataString[0].length()>3)
            temp= new String(new char[5-dataString[0].length()]).replace("\0"," ") + dataString[0];
        else
            temp= new String(new char[3-dataString[0].length()]).replace("\0"," ") + dataString[0];
        stringBuffer.append(temp);
        temp = new String(new char[11-stringBuffer.length()-dataString[1].length()]).replace("\0"," ")+ dataString[1];
        stringBuffer.append(temp);
        temp = new String(new char[24-stringBuffer.length()-dataString[2].length()]).replace("\0"," ")+ dataString[2];
        stringBuffer.append(temp);

        if(dataString[0].length() > 8)
            temp = new String(new char[40-stringBuffer.length()-dataString[3].length()]).replace("\0"," ")+ dataString[3];
        else
            temp = new String(new char[39-stringBuffer.length()-dataString[3].length()]).replace("\0"," ")+ dataString[3];

        stringBuffer.append(temp);

//        printCustom("  3        55      158.43      13:36:32",0,0);
//        printCustom(new String(new char[42]).replace("\0", "-"),0,0);
//        printCustom("Total     165      450.43     2Hr 22min",0,0);


        return stringBuffer.toString();
    }

//    public void printPhoto(int img) {
//        try {
//            Bitmap bmp = BitmapFactory.decodeResource(context.getResources(),
//                    img);
//            if(bmp!=null){
//                byte[] command = Utils.decodeBitmap(bmp);
//                outputStream.write(Printer.ESC_ALIGN_CENTER);
//                printText(command);
//            }else{
//                Log.e("Print Photo error", "the file isn't exists");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.e("PrintTools", "the file isn't exists");
//        }
//    }

//    public void printUnicode(){
//        try {
//            outputStream.write(Printer.ESC_ALIGN_CENTER);
//            printText(Utils.UNICODE_TEXT);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }





    public String alignLabelValue(String label, String value) {
        String ans = label + new String(new char[15-label.length()]).replace("\0", " ") +": "+ value;
        return ans;
    }


    public void halfCut() {
        try {
            outputStream.write(Printer.HALF_CUT);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void fullCut() {
        try {
            outputStream.write(Printer.FULL_CUT);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void printNewLine() {
        try {
            outputStream.write(Printer.FEED_LINE);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void tarePaper(){
        try{

            outputStream.write(Printer.PRINTE_TEST);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resetPrint() {
        try{
            outputStream.write(Printer.ESC_FONT_COLOR_DEFAULT);
            outputStream.write(Printer.FS_FONT_ALIGN);
            outputStream.write(Printer.ESC_ALIGN_LEFT);
            outputStream.write(Printer.ESC_CANCEL_BOLD);
            outputStream.write(Printer.LF);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printText(String msg) {
        try {
            // Print normal text
            outputStream.write(msg.getBytes());
            printNewLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //print byte[]
    public void printText(byte[] msg) {
        try {
            // Print normal text
            outputStream.write(msg);
            printNewLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String leftRightAlign(String str1, String str2) {
        String ans = str1 +str2;
        if(ans.length() <32){
            int n = (32 - str1.length() + str2.length());
            ans = str1 + new String(new char[n]).replace("\0", " ") + str2;
        }
        return ans;
    }

    public String[] getDateTime() {
        final Calendar c = Calendar.getInstance();
        String dateTime [] = new String[2];
        dateTime[0] = c.get(Calendar.DAY_OF_MONTH) +"/"+ c.get(Calendar.MONTH) +"/"+ c.get(Calendar.YEAR);
        dateTime[1] = c.get(Calendar.HOUR_OF_DAY) +":"+ c.get(Calendar.MINUTE);
        return dateTime;
    }


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, MY_UUID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    private void connectPrinter() throws IOException {
        if (btSocket != null && btSocket.isConnected()) {
            return;
        }
        disconnectPrinter();
        device = bluetoothAdapter.getRemoteDevice(printerAddress);
        btSocket = createBluetoothSocket(device);
        btSocket.connect();
        outputStream = btSocket.getOutputStream();
    }

    private void disconnectPrinter(){
        try {
            if(btSocket != null)
                if(outputStream != null) outputStream.close();
                btSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void toastMessage(final String message){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }



}