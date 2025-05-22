package com.febino.aquafish;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;


import com.febino.DatabaseManager.CopyCursor;
import com.febino.DatabaseManager.DataBaseManager;
import com.febino.aquafish.R;
import com.febino.dataclass.TraderDetails;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static  final String TAG_NAME = "AquaFish";
    public static final String APP_NAME = "AquaFish";
    boolean doubleBackToExitPressedOnce = false;

    TraderFragment traderFragment;
    OrderFragment orderFragment;
    BillFragment billFragment;
    StockFragment stockFragment;
    SettingFragment settingFragment;
    BottomNavigationView bottomNavigationView;

//    public static final int EDIT_BILL = 23658;


    public static final String BILL_NEED_TO_EDIT_FLAG = "bill_need_to_edit";
    public static final String BILL_ID_TO_EDIT = "bill_id_to_edit";

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new TraderFragment()).commit();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH}, 1001);
                return;
            }
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/unicode.futurab.ttf");
        CustomTypefaceSpan typefaceSpan = new CustomTypefaceSpan("", font);

        for (int i = 0; i <bottomNavigationView.getMenu().size(); i++) {
            MenuItem menuItem = bottomNavigationView.getMenu().getItem(i);
            SpannableStringBuilder spannableTitle = new SpannableStringBuilder(menuItem.getTitle());
            spannableTitle.setSpan(typefaceSpan, 0, spannableTitle.length(), 0);
            menuItem.setTitle(spannableTitle);
        }

        traderFragment = new TraderFragment();
        orderFragment = new OrderFragment();
        billFragment = new BillFragment();
        stockFragment = new StockFragment();
        settingFragment = new SettingFragment();

        bottomNavigationView.setSelectedItemId(R.id.action_order);
        openFragment(orderFragment);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                switch (item.getItemId()) {
                    case R.id.action_trader:
//                        Toast.makeText(MainActivity.this, "Trader", Toast.LENGTH_SHORT).show();
                        selectedFragment = traderFragment;

                        break;
                    case R.id.action_order:
//                        Toast.makeText(MainActivity.this, "Order", Toast.LENGTH_SHORT).show();
                        selectedFragment = orderFragment;
                        break;
                    case R.id.action_bill:
//                        Toast.makeText(MainActivity.this, "Bill", Toast.LENGTH_SHORT).show();
                        selectedFragment = billFragment;
                        break;
                    case R.id.action_stock:
//                        Toast.makeText(MainActivity.this, "Stock", Toast.LENGTH_SHORT).show();
                        selectedFragment = stockFragment;
                        break;
                    case R.id.action_setting:
//                        Toast.makeText(MainActivity.this, "Setting", Toast.LENGTH_SHORT).show();
                        selectedFragment = settingFragment;
                        break;
                }
                openFragment(selectedFragment);

                return true;
            }
        });

//        ActionBar actionBar = this.getSupportActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);            //For back button in titlebar
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences(APP_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        boolean isBillNeedToEdit = sharedPreferences.getBoolean(BILL_NEED_TO_EDIT_FLAG, false);
        if (isBillNeedToEdit) {
            long billID = sharedPreferences.getLong(BILL_ID_TO_EDIT, 0);
//            Toast.makeText(getApplicationContext(), "Bill_ID is "+ billID, Toast.LENGTH_SHORT).show();

            billFragment = new BillFragment();

            openFragment(billFragment);
            bottomNavigationView.setSelectedItemId(R.id.action_bill);
            billFragment.changeBillFragmentView();
//            editor.putBoolean(BILL_NEED_TO_EDIT_FLAG, false);
//            editor.putLong(BILL_ID_TO_EDIT, (long)0);
//            editor.apply();
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, this.getString(R.string.once_again_back), Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);

    }


//    @Override
//    protected void attachBaseContext(Context newBase) {
//        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
//    }
//    @Override
//    protected void attachBaseContext(Context newBase) {
//        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
//    }

//    static {
//        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
//    }

    private void openFragment(Fragment fragment) {
        FragmentTransaction fm = getSupportFragmentManager().beginTransaction();
        fm.replace(R.id.fragment_container, fragment).commit();
    }
}
