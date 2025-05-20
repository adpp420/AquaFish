package com.febino.aquafish;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.febino.aquafish.R;
import com.febino.dependencies.Printer;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Set;

import androidx.fragment.app.Fragment;

public class SettingFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
    }

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public final static String SELECTED_BLE_MAC_ADDRESS = "selected_ble_mac_address";
    public final static String SELECTED_BLE_MAC_NAME = "selected_ble_mac_name";
    TextView selectPrinter;

    Button printTestBtn;

    ImageView imageView;


    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstance) {
        SpannableString s = new SpannableString(getResources().getString(R.string.settings).toUpperCase());
        s.setSpan(new RelativeSizeSpan(0.8f), 0,s.length(), 0);
        s.setSpan(new TypefaceSpan(getContext(), "unicode.futurab.ttf"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        View view = layoutInflater.inflate(R.layout.fragment_settings, container, false);
        getActivity().setTitle(s);
        TextView appversionTxt = view.findViewById(R.id.setting_app_version_txt);
        selectPrinter = view.findViewById(R.id.setting_bluetooth_select_txt);
        printTestBtn = view.findViewById(R.id.setting_printer_test_btn);
        imageView = view.findViewById(R.id.setting_image_view);

        sharedPreferences = getContext().getSharedPreferences(MainActivity.APP_NAME, getContext().MODE_PRIVATE);
        editor = sharedPreferences.edit();

        String bleName = sharedPreferences.getString(SELECTED_BLE_MAC_NAME, getString(R.string.select_printer));
        final String bleAddress = sharedPreferences.getString(SELECTED_BLE_MAC_ADDRESS, "");

        bleName.replaceAll(" ", "");
        bleAddress.replaceAll(" ", "");

        selectPrinter.setText(bleName);

        printTestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.i("Address", bleAddress);
                Printer thermalPrinter = new Printer(getContext(), bleAddress);
//                thermalPrinter.printTest("test Message");
                thermalPrinter.printReceipt();
//                imageView.setImageBitmap(thermalPrinter.previewBitmapToESCPOS(thermalPrinter.textAsBitmap("வணக்கம்", 120),500 ));
            }
        });

        selectPrinter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View selectBluetoothView = getLayoutInflater().inflate(R.layout.dialog_select_bluetooth,null);
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                bottomSheetDialog.setContentView(selectBluetoothView);


                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                ArrayList<String> deviceList = new ArrayList<>();

                FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheet != null) {

                    BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                    behavior.setSkipCollapsed(true);
                    behavior.setDraggable(false);
                }

//                bottomSheetDialog.setOnShowListener(dialog -> {
//                    BottomSheetDialog dialogInstance = (BottomSheetDialog) dialog;
//                    FrameLayout bottomSheet = dialogInstance.findViewById(com.google.android.material.R.id.design_bottom_sheet);
//
//                    if (bottomSheet != null) {
//                        // Make height match parent
//                        bottomSheet.setLayoutParams(new FrameLayout.LayoutParams(
//                                ViewGroup.LayoutParams.MATCH_PARENT,
//                                ViewGroup.LayoutParams.MATCH_PARENT
//                        ));
//
//                        BottomSheetBehavior<?> behavior = BottomSheetBehavior.from(bottomSheet);
//                        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);   // Expand fully
//                        behavior.setSkipCollapsed(true);                         // Skip half-height
//                        behavior.setDraggable(true);                             // Optional
//                    }
//                });

                bottomSheetDialog.show();

                ListView selectListView = selectBluetoothView.findViewById(R.id.select_printer_listview);

                if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                    if (pairedDevices.size() > 0) {
                        for (BluetoothDevice device : pairedDevices) {
                            String deviceInfo = device.getName() + " \n" + device.getAddress();
                            deviceList.add(deviceInfo);
                        }
                    } else {
                        deviceList.add("No Paired Devices");
                    }
                } else {
                    deviceList.add("Bluetooth is disabled");
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, deviceList);
                selectListView.setAdapter(adapter);

                selectListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        String[] addressArray = new String[2];
                        addressArray = deviceList.get(position).toString().split("\n");
//                        Toast.makeText(getContext(), "Saved "+addressArray[1], Toast.LENGTH_LONG).show();

                        editor.putString(SELECTED_BLE_MAC_ADDRESS, addressArray[1]);
                        editor.putString(SELECTED_BLE_MAC_NAME, addressArray[0]);
                        editor.commit();

                        addressArray[0].replaceAll(" ", "");

                        selectPrinter.setText(addressArray[0]);

                        bottomSheetDialog.dismiss();

                    }
                });
            }
        });

        appversionTxt.setText(BuildConfig.VERSION_NAME);
        return view;
    }
}
