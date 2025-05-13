package com.febino.aquafish;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.febino.DatabaseManager.CopyCursor;
import com.febino.DatabaseManager.DataBaseManager;
import com.febino.aquafish.R;
import com.febino.dataclass.TraderDetails;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

public class TraderFragment extends Fragment {

    DataBaseManager db;
    ArrayList<TraderDetails> traderDetailsArrayList;
//    TraderAdapter traderAdapter;
    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

    }


    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstance) {

        SpannableString s = new SpannableString(getResources().getString(R.string.trader).toUpperCase());
        s.setSpan(new RelativeSizeSpan(0.8f), 0,s.length(), 0);
        s.setSpan(new TypefaceSpan(getContext(), "unicode.futurab.ttf"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        View view = layoutInflater.inflate(R.layout.fragment_trader, container, false);


        FragmentActivity fa = getActivity();
        fa.setTitle(s);

        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/futura medium bt.ttf");
        EditText searchEdit = view.findViewById(R.id.trader_search_edit);
        ImageButton traderAddBtn = (ImageButton) view.findViewById(R.id.trader_add_button);

        searchEdit.setTypeface(font);

        traderDetailsArrayList = new ArrayList<TraderDetails>();
        db = new DataBaseManager(getContext());
        CopyCursor cc = new CopyCursor();

        traderDetailsArrayList = cc.copyTraderListFromCurser(db.getAllTraderFromTraderTable());

//        log("Count = " + traderDetailsArrayList.size());


        TraderAdapter traderAdapter = new TraderAdapter(traderDetailsArrayList, getContext());
        ListView traderListView = (ListView) view.findViewById(R.id.trader_list_view);
        traderListView.setAdapter(traderAdapter);
//        traderListView.setDivider(null);


        traderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//                traderDetailsArrayList.get(position).logValues();
                TraderDetails trader = traderDetailsArrayList.get(position);
                String lastTraderID = "";

                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.dialog_trader_detail, null);

                Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/unicode.futurab.ttf");
                TextView dialogHeader = v.findViewById(R.id.trader_header_textview);
                TextView traderNameTextView = v.findViewById(R.id.trader_name_textview);
                TextView traderAliasTextView = v.findViewById(R.id.trader_alias_textview);
                TextView traderLocationTextView = v.findViewById(R.id.trader_location_textview);
                TextView traderMobileTextView = v.findViewById(R.id.trader_phone_textview);
                TextView traderIDTextView = v.findViewById(R.id.trader_id_textview);

                EditText traderNameEditText = v.findViewById(R.id.trader_name_edit);
                EditText traderAliasEditText = v.findViewById(R.id.trader_alias_edit);
                EditText traderLocationEditText = v.findViewById(R.id.trader_location_edit);
                EditText traderMobileEditText = v.findViewById(R.id.trader_phone_edit);
                EditText traderIDEditText = v.findViewById(R.id.trader_id_edit);

                traderNameEditText.setText(trader.name);
                traderAliasEditText.setText(trader.alias);
                traderLocationEditText.setText(trader.location);
                traderMobileEditText.setText(trader.mobile);
                traderIDEditText.setText(trader.trader_id);

                lastTraderID = trader.trader_id;
                Button traderOKBtn = v.findViewById(R.id.trader_dialog_ok_btn);
                Button traderCancelBtn = v.findViewById(R.id.trader_dialog_cancel_btn);

                dialogHeader.setTypeface(font);
                dialogHeader.setText(dialogHeader.getText().toString().toUpperCase());
                traderOKBtn.setTypeface(font);
                traderCancelBtn.setTypeface(font);

                String finalLastTraderID = lastTraderID;
                traderOKBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        TraderDetails trader = new TraderDetails();


                        trader.trader_id = traderIDEditText.getText().toString();
                        trader.name = traderNameEditText.getText().toString();
                        trader.alias = traderAliasEditText.getText().toString();
                        trader.location = traderLocationEditText.getText().toString();
                        trader.mobile = traderMobileEditText.getText().toString();


                        if(trader.name.equals(" ") || trader.name.equals("") || trader.name.equals(null)){
                            Toast.makeText(getContext(),R.string.give_value_for_name,Toast.LENGTH_LONG).show();
                            return;
                        }
                        else if(trader.trader_id.equals(" ") || trader.trader_id.equals("") || trader.trader_id.equals(null)){
                            Toast.makeText(getContext(),R.string.give_value_for_trader_id,Toast.LENGTH_LONG).show();
                            return;
                        }

                        else if(db.checkTraderIDExist(trader.trader_id) && !finalLastTraderID.equals(trader.trader_id)){
                            Toast.makeText(getContext(),R.string.trader_id_exist,Toast.LENGTH_LONG).show();
                            return;
                        }

//                        db.addTraderInTraderTable(trader);
                        db.updateTraderInTraderTable(trader);
                        traderAdapter.notifyDataSetChanged();
                        bottomSheetDialog.dismiss();
                        
                    }


                });

                traderCancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                    }
                });


                font = Typeface.createFromAsset(getContext().getAssets(), "fonts/futura medium bt.ttf");
                traderNameTextView.setTypeface(font);
                traderAliasTextView.setTypeface(font);
                traderLocationTextView.setTypeface(font);
                traderMobileTextView.setTypeface(font);
                traderIDTextView.setTypeface(font);

                traderNameEditText.setTypeface(font);
                traderAliasEditText.setTypeface(font);
                traderLocationEditText.setTypeface(font);
                traderMobileEditText.setTypeface(font);
                traderIDEditText.setTypeface(font);

                bottomSheetDialog.setContentView(v);
                bottomSheetDialog.show();

//                Toast.makeText(getContext(), "Trader Name addition", Toast.LENGTH_SHORT).show();
            }
        });
        traderListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                TraderDetails traderDetails = traderDetailsArrayList.get(position);
                log(traderDetails.name+"-"+traderDetails._id);
                View warningDialogLayout = getLayoutInflater().inflate(R.layout.dialog_delete_warning,null);

                BottomSheetDialog warningDialogButtom = new BottomSheetDialog(view.getContext());
                warningDialogButtom.setContentView(warningDialogLayout);
                warningDialogButtom.show();

//                AlertDialog.Builder warningAlertDialogBuilder = new AlertDialog.Builder(view.getContext());
//                warningAlertDialogBuilder.setView(warningDialogLayout);
//                AlertDialog warningAlert = warningAlertDialogBuilder.show();

                TextView selectedTraderNameTxt = warningDialogLayout.findViewById(R.id.warning_delete_name_textview);
                selectedTraderNameTxt.setText("\""+traderDetails.name+"\"");

                Button yesBtn = warningDialogLayout.findViewById(R.id.warning_dialog_yes_btn);
                Button noBtn = warningDialogLayout.findViewById(R.id.warning_dialog_no_btn);

                yesBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        db.deleteDetailsByID(traderDetails._id);
                        db.deleteOrderByTraderID(traderDetails._id);
                        log("Trader Deleted...");
                        traderDetailsArrayList.remove(position);
                        traderAdapter.notifyDataSetChanged();
                        warningDialogButtom.dismiss();

                    }
                });

                noBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        warningAlert.dismiss();
                        warningDialogButtom.dismiss();
                    }
                });
                return true;
            }
        });
        traderAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.dialog_trader_detail, null);

                Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/unicode.futurab.ttf");
                TextView dialogHeader = v.findViewById(R.id.trader_header_textview);
                TextView traderNameTextView = v.findViewById(R.id.trader_name_textview);
                TextView traderAliasTextView = v.findViewById(R.id.trader_alias_textview);
                TextView traderLocationTextView = v.findViewById(R.id.trader_location_textview);
                TextView traderMobileTextView = v.findViewById(R.id.trader_phone_textview);
                TextView traderIDTextView = v.findViewById(R.id.trader_id_textview);

                EditText traderNameEditText = v.findViewById(R.id.trader_name_edit);
                EditText traderAliasEditText = v.findViewById(R.id.trader_alias_edit);
                EditText traderLocationEditText = v.findViewById(R.id.trader_location_edit);
                EditText traderMobileEditText = v.findViewById(R.id.trader_phone_edit);
                EditText traderIDEditText = v.findViewById(R.id.trader_id_edit);

                Button traderOKBtn = v.findViewById(R.id.trader_dialog_ok_btn);
                Button traderCancelBtn = v.findViewById(R.id.trader_dialog_cancel_btn);

                dialogHeader.setTypeface(font);
                dialogHeader.setText(dialogHeader.getText().toString().toUpperCase());
                traderOKBtn.setTypeface(font);
                traderCancelBtn.setTypeface(font);

                traderOKBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TraderDetails trader = new TraderDetails();
                        trader.trader_id = traderIDEditText.getText().toString();
                        trader.name = traderNameEditText.getText().toString();
                        trader.alias = traderAliasEditText.getText().toString();
                        trader.location = traderLocationEditText.getText().toString();
                        trader.mobile = traderMobileEditText.getText().toString();


                        if(trader.name.equals(" ") || trader.name.equals("") || trader.name.equals(null)){
                            Toast.makeText(getContext(),R.string.give_value_for_name,Toast.LENGTH_LONG).show();
                            return;
                        }
                        else if(trader.trader_id.equals(" ") || trader.trader_id.equals("") || trader.trader_id.equals(null)){
                            Toast.makeText(getContext(),R.string.give_value_for_trader_id,Toast.LENGTH_LONG).show();
                            return;
                        }

                        else if(db.checkTraderIDExist(trader.trader_id)){
                            Toast.makeText(getContext(),R.string.trader_id_exist,Toast.LENGTH_LONG).show();
                            return;
                        }

//                        db.addTraderInTraderTable(trader);
                        trader._id = (int) db.addTraderInTraderTableReturnID(trader);
                        if(trader._id > 0){
                            traderDetailsArrayList.add(trader);
                            traderAdapter.notifyDataSetChanged();
                            bottomSheetDialog.dismiss();
                        }else{
                            Toast.makeText(getContext(),R.string.error_in_trader_data,Toast.LENGTH_LONG).show();
                            return;
                        }


                    }


                });

                traderCancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                    }
                });


                font = Typeface.createFromAsset(getContext().getAssets(), "fonts/futura medium bt.ttf");
                traderNameTextView.setTypeface(font);
                traderAliasTextView.setTypeface(font);
                traderLocationTextView.setTypeface(font);
                traderMobileTextView.setTypeface(font);
                traderIDTextView.setTypeface(font);

                traderNameEditText.setTypeface(font);
                traderAliasEditText.setTypeface(font);
                traderLocationEditText.setTypeface(font);
                traderMobileEditText.setTypeface(font);
                traderIDEditText.setTypeface(font);

                bottomSheetDialog.setContentView(v);
                bottomSheetDialog.show();

//                Toast.makeText(getContext(), "Trader Name addition", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void log(String message){
        Log.i(MainActivity.TAG_NAME + "-" + "TraderFragment", message);
    }
}
