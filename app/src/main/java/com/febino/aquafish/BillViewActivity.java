package com.febino.aquafish;

import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.FragmentTransaction;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.febino.DatabaseManager.CopyCursor;
import com.febino.DatabaseManager.DataBaseManager;
import com.febino.dataclass.BillDetails;
import com.febino.dataclass.OrderDetails;
import com.febino.dataclass.TraderDetails;
import com.febino.dependencies.BillGenerator;
import com.febino.dependencies.PdfGenerator;
import com.febino.dependencies.PdfGeneratorIText7;
import com.febino.dependencies.Printer;
import com.febino.dependencies.ProjectUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class BillViewActivity extends AppCompatActivity {

    Button printBtn, shareBtn, backBtn;

    public static final String BILL_ID_EXTRAS = "bill_id_string";

    float billAmount, balanceAmount, oldBalanceAmount, billBalanceAmount;

    TextView billAmountText, balanceAmountText, oldBalanceAmountTxt, totalAmountText, billBalanceText;
    TextView traderNameText, traderIDText;
    ListView billViewOrderListView;
//    EditText billNoEdit, billDateEdit;
    TextView billNoText, billDateText;
    DataBaseManager db;
    CopyCursor cc;
    BillDetails billDetails;

    DecimalFormat decimalFormat;
    TraderDetails traderDetails;
    ArrayList<OrderDetails> orderDetailsArrayList;
    ImageButton backImageBtn, printImageBtn, shareImageBtn, editImageBtn;
    private View billViewActivity;
    long billID = 0;




    String printerAddress = null;
    BluetoothDevice device;
    private BluetoothAdapter bluetoothAdapter;
    BluetoothSocket btSocket = null;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_ENABLE_BT = 4;





    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_bill_view);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        billViewActivity = findViewById(R.id.bill_view_layout);
//        billViewActivity = getWindow().getDecorView().findViewById(android.R.id.content);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            billID = extras.getLong(BILL_ID_EXTRAS, 0);
//            Log.i("bill ID", ""+billID);
//            return;
        }

        sharedPreferences = getSharedPreferences(MainActivity.APP_NAME, MODE_PRIVATE);

        db = new DataBaseManager(BillViewActivity.this);
        cc = new CopyCursor();
        decimalFormat = new DecimalFormat("0.0");

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        printerAddress = sharedPreferences.getString(SettingFragment.SELECTED_BLE_MAC_ADDRESS, "");



        billDetails = cc.copyBillFromCursor(db.getBillFromBillTableByID(billID));
        orderDetailsArrayList = cc.copyOrderListFromCursor(db.getOrderFromOrderTableByBillID(billID));
        traderDetails = cc.copyTraderFromCursor(db.getTraderInTraderTableByID(billDetails.getTraderID()));

//        Log.i("Bill Id", "" + billID);

        printBtn = findViewById(R.id.bill_view_print_btn);
        shareBtn = findViewById(R.id.bill_view_share_btn);
        backBtn = findViewById(R.id.bill_view_back_btn);


        backImageBtn = findViewById(R.id.bill_view_back_img_btn);
        printImageBtn = findViewById(R.id.bill_view_print_img_btn);
        shareImageBtn = findViewById(R.id.bill_view_share_img_btn);
        editImageBtn = findViewById(R.id.bill_view_edit_img_btn);

        traderNameText = findViewById(R.id.bill_view_name_textview);
        traderIDText = findViewById(R.id.bill_view_trader_id_textview);

        billAmountText = findViewById(R.id.bill_view_bill_amount_text);
        balanceAmountText = findViewById(R.id.bill_view_balance_text);
        billBalanceText = findViewById(R.id.bill_view_bill_balance_text);
        oldBalanceAmountTxt = findViewById(R.id.bill_view_old_balance_text);
        totalAmountText = findViewById(R.id.bill_view_total_amount_text);

        billViewOrderListView = findViewById(R.id.bill_view_listview);

        billNoText = findViewById(R.id.bill_view_billno_text);
        billDateText = findViewById(R.id.bill_view_date_text);

//        billNoEdit = findViewById(R.id.bill_view_billno_edit);
//        billDateEdit = findViewById(R.id.bill_view_date_edit);

        traderNameText.setText(traderDetails.name);
        traderIDText.setText(traderDetails.trader_id);

//        billNoEdit.setText(billDetails.getBillNo()+"");
//        billDateEdit.setText(convertDateFormat(billDetails.getBillDate()));

        billNoText.setText(billDetails.getBillNo()+"");
        billDateText.setText(ProjectUtils.convertDateFormatToNormal(billDetails.getBillDate()));


        billAmount = billDetails.getBillAmount();
        balanceAmount = billDetails.getBalanceAmount();
        billBalanceAmount = billAmount + balanceAmount;
        oldBalanceAmount = billDetails.getOldBalanceAmount();

        billAmountText.setText(billAmount+"");
        balanceAmountText.setText(balanceAmount+"");
        oldBalanceAmountTxt.setText(oldBalanceAmount+"");
        billBalanceText.setText(billBalanceAmount+"");

        totalAmountText.setText(""+(billAmount+balanceAmount+oldBalanceAmount));


        BillViewActivityOrderAdapter billViewActivityOrderAdapter = new BillViewActivityOrderAdapter(orderDetailsArrayList,BillViewActivity.this,db);
        billViewOrderListView.setAdapter(billViewActivityOrderAdapter);


        shareImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                PdfGenerator pdfGenerator = new PdfGenerator(BillViewActivity.this, BillViewActivity.this, billDetails, orderDetailsArrayList);
//                pdfGenerator.shareBill();

                BillGenerator billGenerator = new BillGenerator(BillViewActivity.this, billDetails,BillViewActivity.this, billViewActivity);
                billGenerator.share(billViewOrderListView);


            }
        });

        printImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printImageBtn.setEnabled(false);
                if(printerAddress == null || printerAddress.equals("")){
                    toastMessage("Please select Printer from Settings page");
                    return;
                }
                toastMessage("Please Wait....Connecting to Printer");

                Printer thermalPrinter = new Printer(getApplicationContext(), printerAddress);

                thermalPrinter.printReceipt(db, billDetails, traderDetails, orderDetailsArrayList);


//                thermalPrinter.printTest("This is test message");



                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        printImageBtn.setEnabled(true);
                    }
                });
            }
        });

        backImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        editImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

//                Intent mainActivityIntent = new Intent(BillViewActivity.this, MainActivity.class);
////                mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                mainActivityIntent.putExtra("testing", 123);
//                startActivity(mainActivityIntent);

                SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.APP_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(MainActivity.BILL_NEED_TO_EDIT_FLAG, true);
                editor.putLong(MainActivity.BILL_ID_TO_EDIT, billDetails.get_id());
                editor.apply();

            }
        });

    }

    public static File store(Bitmap bm, String fileName){
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Screenshots";
        File dir = new File(dirPath);
        if(!dir.exists())
            dir.mkdirs();
        File file = new File(dirPath, fileName);
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 85, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    private void shareImage(File file){
        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");

        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, "");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        try {
            startActivity(Intent.createChooser(intent, "Share Screenshot"));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getApplicationContext(), "No App Available", Toast.LENGTH_SHORT).show();
        }
    }


//    public static String convertDateFormat(String dateStr) {
//
//        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
//
//
//        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");
//
//        try {
//
//            Date date = inputFormat.parse(dateStr);
//            return outputFormat.format(date);
//        } catch (ParseException e) {
//            e.printStackTrace();
//            return null;  // Return null if parsing fails
//        }
//    }

    private void toastMessage(final String message){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }



}