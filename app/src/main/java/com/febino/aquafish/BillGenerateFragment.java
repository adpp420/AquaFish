package com.febino.aquafish;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.febino.DatabaseManager.CopyCursor;
import com.febino.DatabaseManager.DataBaseManager;
import com.febino.aquafish.R;
import com.febino.dataclass.BillDetails;
import com.febino.dataclass.OrderDetails;
import com.febino.dataclass.ProductDetails;
import com.febino.dataclass.TraderDetails;
import com.febino.dependencies.PdfGenerator;
import com.febino.dependencies.ProjectUtils;
import com.febino.validation.ValidateDetails;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class BillGenerateFragment extends Fragment {

    AutoCompleteTextView billName;
    EditText billDateEdit, billNoEdit;
    ImageButton addOrderImgBtn;
    TextView balanceText,oldBalanceText, billAmountText, totalAmountText, billBalanceText;
    LinearLayout balanceLayout, oldBalanceLayout;
    Calendar mCurrentDate;
    String dateTimeFormat = "yyyy-MM-dd";
    SimpleDateFormat simpleDateFormat;
    ArrayList<TraderDetails> traderDetailsArrayList;

    ArrayList<OrderDetails> selectedOrderArrayList;
    ArrayList<OrderDetails> tempSelectedOrderArrayList;

    TraderDetails selectedTraderDetails = null;

    ListView billGenerateListView;
    BillGenerateOrderAdapter billGenerateOrderAdapter;

    DecimalFormat decimalFormat;

    private long updateLastBillNo;

    private double totalAmount;
    private float balanceAmount, oldBalanceAmount, billAmount,billBalance;
    private Button generateBtn;

    private static final int BALANCE = 2;
    private static final int OLD_BALANCE = 1;
    DataBaseManager db;
    CopyCursor cc;

    Fragment currentFragment;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    boolean isBillNeedToEdit;
    BillDetails updatedBillDetails;

    public BillGenerateFragment(){
        currentFragment = this;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bill_generate_fragment,container,false);
        db = new DataBaseManager(getContext());
        cc = new CopyCursor();

        sharedPreferences = getContext().getSharedPreferences(MainActivity.APP_NAME, getContext().MODE_PRIVATE);
        editor = sharedPreferences.edit();

        isBillNeedToEdit = sharedPreferences.getBoolean(MainActivity.BILL_NEED_TO_EDIT_FLAG, false);



        mCurrentDate = Calendar.getInstance();
        decimalFormat = new DecimalFormat("0.0");



        traderDetailsArrayList = cc.copyTraderListFromCurser(db.getAllTraderFromTraderTable());

        selectedOrderArrayList = new ArrayList<OrderDetails>();
        tempSelectedOrderArrayList = new ArrayList<OrderDetails>();

        billAmountText = view.findViewById(R.id.bill_generate_bill_amount_text);
        balanceText = view.findViewById(R.id.bill_generete_balance_text);
        oldBalanceText = view.findViewById(R.id.bill_generete_old_balance_text);
        totalAmountText = view.findViewById(R.id.bill_generate_total_amount_text);
        billBalanceText = view.findViewById(R.id.bill_generete_bill_balance_text);

        balanceLayout = view.findViewById(R.id.bill_generate_balance_layout);
        oldBalanceLayout = view.findViewById(R.id.bill_generate_old_balance_layout);

        addOrderImgBtn = view.findViewById(R.id.bill_generate_add_order_imgbtn);
        billGenerateListView = view.findViewById(R.id.bill_generate_tab_listview);
        billName = view.findViewById(R.id.bill_main_name_edit);
        billNoEdit = view.findViewById(R.id.bill_main_billno_edit);
        generateBtn = view.findViewById(R.id.bill_generate_btn);

        billGenerateOrderAdapter = new BillGenerateOrderAdapter(selectedOrderArrayList, getContext(),db);
//        tempSelectedOrderArrayList.addAll(selectedOrderArrayList);

        billGenerateListView.setAdapter(billGenerateOrderAdapter);

        simpleDateFormat = new SimpleDateFormat(dateTimeFormat);

        billDateEdit = view.findViewById(R.id.bill_main_date_edit);

        updateLastBillNo = db.getLastBillNoFromBillTable()+1;

        billDateEdit.setText(simpleDateFormat.format(mCurrentDate.getTime()));
        billNoEdit.setText(""+updateLastBillNo);


        if (isBillNeedToEdit) {
            long billID = sharedPreferences.getLong(MainActivity.BILL_ID_TO_EDIT, 0);
//            Toast.makeText(getContext(), "Bill No is " + billNo,Toast.LENGTH_SHORT).show();

            updatedBillDetails = cc.copyBillFromCursor(db.getBillFromBillTableByID(billID));

            selectedTraderDetails = cc.copyTraderFromCursor(db.getTraderInTraderTableByID(updatedBillDetails.getTraderID()));
            billName.setText(selectedTraderDetails.name);

            billDateEdit.setText(updatedBillDetails.getBillDate());

            updateLastBillNo = updatedBillDetails.getBillNo();
            billNoEdit.setText(updateLastBillNo+"");

            selectedOrderArrayList.clear();
            tempSelectedOrderArrayList.clear();

            selectedOrderArrayList = cc.copyOrderListFromCursor(db.getOrderFromOrderTableByBillID(billID));
            tempSelectedOrderArrayList.addAll(selectedOrderArrayList);

            billGenerateOrderAdapter = new BillGenerateOrderAdapter(selectedOrderArrayList, getContext(),db);
            billGenerateListView.setAdapter(billGenerateOrderAdapter);

            balanceAmount = updatedBillDetails.getBalanceAmount();
            oldBalanceAmount = updatedBillDetails.getOldBalanceAmount();
            billAmount = updatedBillDetails.getBillAmount();

            balanceText.setText("" + balanceAmount);
            oldBalanceText.setText(""+oldBalanceAmount);

            updateBillAmount();
            updateTotalAmount();

            generateBtn.setText("UPDATE");

//            balanceText.setText("" + balanceAmount);
//            oldBalanceText.setText("" + oldBalanceAmount);
            billGenerateOrderAdapter.notifyDataSetChanged();

            editor.putBoolean(MainActivity.BILL_NEED_TO_EDIT_FLAG, false);
            editor.putLong(MainActivity.BILL_ID_TO_EDIT, 0);
            editor.apply();

        }

        billName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = layoutInflater.inflate(R.layout.bill_trader_adapter_view, null);

                bottomSheetDialog.setContentView(v);



                FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheet != null) {
                    BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                    behavior.setDraggable(false);
                    behavior.setHideable(false);
                }


                BillTraderSelectAdapter billTraderSelectAdapter = new BillTraderSelectAdapter(traderDetailsArrayList,getContext());
                ListView traderList = v.findViewById(R.id.bill_trader_select_list);
                traderList.setAdapter(billTraderSelectAdapter);

                Button cancelBtn = v.findViewById(R.id.bill_trader_select_dialog_cancel_btn);

                traderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        selectedTraderDetails = traderDetailsArrayList.get(position);
                        selectedOrderArrayList.clear();
                        tempSelectedOrderArrayList.clear();

                        billName.setText(selectedTraderDetails.name);

                        updateLastBillNo = db.getLastBillNoFromBillTable()+1;
                        billNoEdit.setText(""+updateLastBillNo);

                        generateBtn.setText(getResources().getString(R.string.generate));

                        billGenerateOrderAdapter.notifyDataSetChanged();


                        billDateEdit.setText(simpleDateFormat.format(mCurrentDate.getTime()));

                        balanceAmount = 0;
                        oldBalanceAmount = 0;
                        billAmount = 0;

                        balanceText.setText("" + balanceAmount);
                        oldBalanceText.setText(""+oldBalanceAmount);

                        updateBillAmount();
                        updateTotalAmount();

                        isBillNeedToEdit = false;

                        bottomSheetDialog.dismiss();
                    }
                });

                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                    }
                });



                bottomSheetDialog.show();

            }
        });

        billDateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final int[] mYear = {mCurrentDate.get(Calendar.YEAR)};
                final int[] mMonth = {mCurrentDate.get(Calendar.MONTH)};
                final int[] mDay = {mCurrentDate.get(Calendar.DAY_OF_MONTH)};

                DatePickerDialog mDatePicker = new DatePickerDialog(getContext(),R.style.datepicker, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        Calendar myCalendar = Calendar.getInstance();
                        myCalendar.set(Calendar.YEAR, selectedyear);
                        myCalendar.set(Calendar.MONTH, selectedmonth);
                        myCalendar.set(Calendar.DAY_OF_MONTH, selectedday);

                        mCurrentDate = myCalendar;
                        String dayString = simpleDateFormat.format(mCurrentDate.getTime());
                        billDateEdit.setText(dayString);

                        mDay[0] = selectedday;
                        mMonth[0] = selectedmonth;
                        mYear[0] = selectedyear;
                    }
                }, mYear[0], mMonth[0], mDay[0]);
                //mDatePicker.setTitle("Select date");
                mDatePicker.show();

            }
        });

        billGenerateListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());

                LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = layoutInflater.inflate(R.layout.dialog_order_details, null);

                bottomSheetDialog.setContentView(view);



//                FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
//                if (bottomSheet != null) {
//                    BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
//                    behavior.setDraggable(false);
//                    behavior.setHideable(false);
//                }

                OrderDetails orderDetails = selectedOrderArrayList.get(position);
                ProductDetails productDetails = new CopyCursor().copyProductFromCursor(db.getProductFromProductTableByID(orderDetails.getProductID()));
                TraderDetails traderDetails = new CopyCursor().copyTraderFromCursor(db.getTraderInTraderTableByID(orderDetails.getTraderID()));


                Button okBtn = view.findViewById(R.id.order_dialog_ok_btn);
                Button cancelBtn = view.findViewById(R.id.order_dialog_cancel_btn);

                EditText name = view.findViewById(R.id.order_name_edit);
                EditText item = view.findViewById(R.id.order_item_edit);
                EditText box = view.findViewById(R.id.order_box_edit);
                EditText kg = view.findViewById(R.id.order_kgs_edit);
                EditText rate = view.findViewById(R.id.order_rate_edit);
                EditText date = view.findViewById(R.id.order_date_edit);

                name.setFocusable(false);
                item.setFocusable(false);
                date.setFocusable(false);

                name.setText(traderDetails.name);
                item.setText(productDetails.productName);
                box.setText(""+orderDetails.getTotalBox());
                kg.setText("" + orderDetails.getTotalKG());
                rate.setText("" + orderDetails.getRatePerKG());
                date.setText("" + orderDetails.getOrderDate());
                okBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String orderBoxString = box.getText().toString();
                        String orderKGString = kg.getText().toString();
                        String orderRateString = rate.getText().toString();
//                        String orderKGPerBoxString = "35.0"; //TODO: add 35kg/box in the settings page
                        String errorMessage = ValidateDetails.ValidateOrderDetails(getContext(),orderBoxString, orderKGString, orderRateString);

                        if (errorMessage != null) {
                            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                            return;
                        }


                        orderDetails.setTotalBox(Integer.parseInt(orderBoxString));
                        orderDetails.setTotalKG(Float.parseFloat(orderKGString));
                        orderDetails.setRatePerKG(Float.parseFloat(orderRateString));
//                            currentOrderDetails.setKgPerBox(Float.parseFloat(orderKGPerBoxString));
//                        db.updateOrderInOrderTable(orderDetails);
                        Toast.makeText(getContext(),R.string.order_updated,Toast.LENGTH_LONG).show();


                        selectedOrderArrayList.set(position, orderDetails);
                        tempSelectedOrderArrayList.set(position, orderDetails);
                        billGenerateOrderAdapter.notifyDataSetChanged();

                        updateBillAmount();
                        updateTotalAmount();
                        bottomSheetDialog.dismiss();

                    }
                });

                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();

                    }
                });


                bottomSheetDialog.show();
//                return false;
            }
        });

        billGenerateListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());


                TextView messageText = new TextView(getContext());
                messageText.setText("Do you want to remove the Item?");
                messageText.setPadding(50,30,50,30);
                dialogBuilder.setView(messageText);

                dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        OrderDetails orderDetails = selectedOrderArrayList.get(position);
//
//                        orderDetails.setBilled(false);
//                        orderDetails.setBillID(0);
//                        db.updateOrderInOrderTable(orderDetails);

//                        balanceAmount = 0;
//                        oldBalanceAmount = 0;
//
//                        balanceText.setText("" + balanceAmount);
//                        oldBalanceText.setText(""+oldBalanceAmount);



                        selectedOrderArrayList.remove(orderDetails);
                        tempSelectedOrderArrayList.remove(orderDetails);

                        updateBillAmount();
                        updateTotalAmount();
                        billGenerateOrderAdapter.notifyDataSetChanged();

                    }
                });

                dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                dialogBuilder.show();
                return true;
            }
        });

        balanceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertBuildForBalance("Balance", balanceText, BALANCE);
            }
        });

        oldBalanceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertBuildForBalance("Old Balance", oldBalanceText, OLD_BALANCE);
            }
        });

        generateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedTraderDetails == null){
                    Toast.makeText(getContext(), "Select Trader",Toast.LENGTH_LONG).show();
                    return;
                }
                if(selectedOrderArrayList.size() <= 0){
                    Toast.makeText(getContext(), "Add Orders in the Order list",Toast.LENGTH_LONG).show();
                    return;
                }


                long lastBillNo = 0;
                long billID = 0;



                if (!isBillNeedToEdit) {
                    BillDetails billDetails = new BillDetails();
                    lastBillNo = db.getLastBillNoFromBillTable();
                    billDetails.setBillNo(lastBillNo+1);
                    billDetails.setBillDate(billDateEdit.getText().toString());
                    billDetails.setBalanceAmount(balanceAmount);
                    billDetails.setOldBalanceAmount(oldBalanceAmount);
                    billDetails.setBillAmount(billAmount);
                    billDetails.setTraderID(selectedTraderDetails._id);
                    billID = db.addBillInBillTableReturnID(billDetails,selectedOrderArrayList);

                }else{

                    updatedBillDetails.setBillDate(billDateEdit.getText().toString());
                    updatedBillDetails.setBalanceAmount(balanceAmount);
                    updatedBillDetails.setOldBalanceAmount(oldBalanceAmount);
                    updatedBillDetails.setBillAmount(billAmount);

                    Log.i("amount balance", balanceAmount + "-" + oldBalanceAmount + "-" + billAmount);
                    billID = db.updateBillInBillTableReturnID(updatedBillDetails,selectedOrderArrayList);
                }

                for (OrderDetails orderDetails : selectedOrderArrayList) {
                    db.updateOrderInOrderTable(orderDetails);
                }


                selectedTraderDetails = null;
                updateLastBillNo = db.getLastBillNoFromBillTable() +1;

                billName.setText("");
                billNoEdit.setText(""+updateLastBillNo);
                billDateEdit.setText(simpleDateFormat.format(mCurrentDate.getTime()));


                Intent intent = new Intent(getContext(), BillViewActivity.class);
                intent.putExtra(BillViewActivity.BILL_ID_EXTRAS, billID);
                startActivity(intent);

//                PdfGenerator pdfGenerator = new PdfGenerator(getContext(),getActivity(), billDetails, selectedOrderArrayList);
//                pdfGenerator.generateBill();
                getFragmentManager().beginTransaction().detach(currentFragment).attach(currentFragment).commit();
//                resetPage();
//                FragmentTransaction currentFragment = getFragmentManager().beginTransaction().replace(R.id.);
//                getFragmentManager().beginTransaction().replace(R.id.fragment_container, new BillFragment()).commit();

            }
        });

        addOrderImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = billName.getText().toString();

                if(selectedTraderDetails == null || name == null || name.equals(null) || name.equals("")) {
                    Toast.makeText(getContext(), "Please select Name to add order", Toast.LENGTH_LONG).show();
                    return;
                }


                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());

                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.dialog_bill_add_order_details, null);

                bottomSheetDialog.setContentView(v);


                FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheet != null) {
                    BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                    behavior.setDraggable(false);
                    behavior.setHideable(false);
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }

                DataBaseManager db = new DataBaseManager(getContext());
                CopyCursor cc = new CopyCursor();

                final boolean[] isSelectAllClicked = {false};

                ListView orderListView = v.findViewById(R.id.bill_generate_order_list);
                Button okBtn = v.findViewById(R.id.bill_generate_add_dialog_ok_btn);
                Button cancelBtn = v.findViewById(R.id.bill_generate_add_dialog_cancel_btn);
                EditText selectDate = v.findViewById(R.id.bill_add_order_calender_from_date_edit);
                EditText toDate = v.findViewById(R.id.bill_add_order_calender_to_date_edit);
                ImageButton rightSelectDate = v.findViewById(R.id.bill_add_order_calender_right);
                ImageButton leftSelectDate = v.findViewById(R.id.bill_add_order_calender_left);

                ImageView selectAllImageView = v.findViewById(R.id.bill_add_order_select_all_imageview);

                tempSelectedOrderArrayList.clear();
                tempSelectedOrderArrayList.addAll(selectedOrderArrayList);


//                final Calendar[] orderSelectDate = {mCurrentDate};
                final Calendar[] fromSelectDate = {mCurrentDate};
                final Calendar[] toSelectDate = {mCurrentDate};


//                final String[] selectDateString = {simpleDateFormat.format(orderSelectDate[0].getTime())};
                final String[] fromSelectDateString = {simpleDateFormat.format(fromSelectDate[0].getTime())};
                final String[] toSelectDateString = {simpleDateFormat.format(toSelectDate[0].getTime())};

                selectDate.setText(fromSelectDateString[0]);
                toDate.setText(toSelectDateString[0]);

                final ArrayList<OrderDetails>[] orderDetailsArrayList;

                if(isBillNeedToEdit) {
//                    this initialization is to add orders in list of orders adapter which already genereated bill with bill_id
                    orderDetailsArrayList = new ArrayList[]{cc.copyOrderListFromCursor(db.getOrderFromOrderTableByTraderIDWithDate(selectedTraderDetails._id, updatedBillDetails.get_id() , fromSelectDateString[0], toSelectDateString[0]))};
                }else{
                    orderDetailsArrayList = new ArrayList[]{cc.copyOrderListFromCursor(db.getOrderFromOrderTableByTraderIDWithDate(selectedTraderDetails._id, fromSelectDateString[0] ,toSelectDateString[0]))};
                }



                for (int i = 0; i < orderDetailsArrayList[0].size(); i++) {
                    for (int j = 0; j < tempSelectedOrderArrayList.size(); j++) {
                        if(orderDetailsArrayList[0].get(i).get_id() == tempSelectedOrderArrayList.get(j).get_id()){
                            orderDetailsArrayList[0].get(i).isSelected = true;
                        }
                    }
                }

                final BillOrderListSelectAdapter[] billOrderListSelectAdapter = {new BillOrderListSelectAdapter(orderDetailsArrayList[0], getContext(), db)};

                orderListView.setAdapter(billOrderListSelectAdapter[0]);

                selectAllImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isSelectAllClicked[0] = !isSelectAllClicked[0];
                        if (isSelectAllClicked[0]) {
                            selectAllImageView.setImageDrawable(getResources().getDrawable(R.drawable.baseline_check_box_30));
                            for(int k = 0 ; k < orderDetailsArrayList[0].size(); k++){
                                OrderDetails orderDetails = orderDetailsArrayList[0].get(k);
                                orderDetails.isSelected = true;
                                tempSelectedOrderArrayList.add(orderDetails);

                            }
                            billOrderListSelectAdapter[0].notifyDataSetChanged();
                        }
                        else{
                            selectAllImageView.setImageDrawable(getResources().getDrawable(R.drawable.baseline_check_box_outline_blank_30));
                            for(int k = 0 ; k < orderDetailsArrayList[0].size(); k++){
                                OrderDetails orderDetails = orderDetailsArrayList[0].get(k);
                                orderDetails.isSelected = false;
                                tempSelectedOrderArrayList.remove(orderDetails);
                            }
                            billOrderListSelectAdapter[0].notifyDataSetChanged();
                        }
                    }
                });

                selectDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final int[] mYear = {fromSelectDate[0].get(Calendar.YEAR)};
                        final int[] mMonth = {fromSelectDate[0].get(Calendar.MONTH)};
                        final int[] mDay = {fromSelectDate[0].get(Calendar.DAY_OF_MONTH)};

                        DatePickerDialog mDatePicker = new DatePickerDialog(getContext(),R.style.datepicker, new DatePickerDialog.OnDateSetListener() {
                            public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                                Calendar myCalendar = Calendar.getInstance();
                                myCalendar.set(Calendar.YEAR, selectedyear);
                                myCalendar.set(Calendar.MONTH, selectedmonth);
                                myCalendar.set(Calendar.DAY_OF_MONTH, selectedday);


                                String dayString = simpleDateFormat.format(myCalendar.getTime());

                                if(!ProjectUtils.isDateRangeValid(dayString.toString(),toSelectDateString[0])) {
                                    Toast.makeText(getContext(),"From date is greater than to date",Toast.LENGTH_SHORT).show();
                                    return;
                                }



                                fromSelectDateString[0] = dayString.toString();
                                fromSelectDate[0] = myCalendar;
                                selectDate.setText(dayString);
                                if(isBillNeedToEdit) {
//                                      this initialization is to add orders in list of orders adapter which already genereated bill with bill_id
                                    orderDetailsArrayList[0] = cc.copyOrderListFromCursor(db.getOrderFromOrderTableByTraderIDWithDate(selectedTraderDetails._id, updatedBillDetails.get_id() , fromSelectDateString[0], toSelectDateString[0]));
                                }else{
                                    orderDetailsArrayList[0] = cc.copyOrderListFromCursor(db.getOrderFromOrderTableByTraderIDWithDate(selectedTraderDetails._id, fromSelectDateString[0],toSelectDateString[0]));
                                }

                                for (int i = 0; i < orderDetailsArrayList[0].size(); i++) {
                                    for (int j = 0; j < tempSelectedOrderArrayList.size(); j++) {
                                        if(orderDetailsArrayList[0].get(i).get_id() == tempSelectedOrderArrayList.get(j).get_id()){
                                            orderDetailsArrayList[0].get(i).isSelected = true;
                                        }
                                    }
                                }

                                billOrderListSelectAdapter[0] = new BillOrderListSelectAdapter(orderDetailsArrayList[0], getContext(),db);
                                orderListView.setAdapter(billOrderListSelectAdapter[0]);
//                                billOrderListSelectAdapter.notifyDataSetChanged();

                                mDay[0] = selectedday;
                                mMonth[0] = selectedmonth;
                                mYear[0] = selectedyear;
                            }
                        }, mYear[0], mMonth[0], mDay[0]);

                        mDatePicker.show();
                    }
                });

                toDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final int[] mYear = {toSelectDate[0].get(Calendar.YEAR)};
                        final int[] mMonth = {toSelectDate[0].get(Calendar.MONTH)};
                        final int[] mDay = {toSelectDate[0].get(Calendar.DAY_OF_MONTH)};

                        DatePickerDialog mDatePicker = new DatePickerDialog(getContext(),R.style.datepicker, new DatePickerDialog.OnDateSetListener() {
                            public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                                Calendar myCalendar = Calendar.getInstance();
                                myCalendar.set(Calendar.YEAR, selectedyear);
                                myCalendar.set(Calendar.MONTH, selectedmonth);
                                myCalendar.set(Calendar.DAY_OF_MONTH, selectedday);


                                String dayString = simpleDateFormat.format(myCalendar.getTime());




                                if(!ProjectUtils.isDateRangeValid(fromSelectDateString[0],dayString.toString())) {
                                    Toast.makeText(getContext(),"From date is greater than to date",Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                toSelectDate[0] = myCalendar;
                                toSelectDateString[0] = dayString.toString();
                                toDate.setText(dayString);
//                                orderDetailsArrayList[0] = cc.copyOrderListFromCursor(db.getOrderFromOrderTableByTraderIDWithDate(selectedTraderDetails._id, fromSelectDateString[0],toSelectDateString[0]));
                                if(isBillNeedToEdit) {
//                                      this initialization is to add orders in list of orders adapter which already genereated bill with bill_id
                                    orderDetailsArrayList[0] = cc.copyOrderListFromCursor(db.getOrderFromOrderTableByTraderIDWithDate(selectedTraderDetails._id, updatedBillDetails.get_id() , fromSelectDateString[0], toSelectDateString[0]));
                                }else{
                                    orderDetailsArrayList[0] = cc.copyOrderListFromCursor(db.getOrderFromOrderTableByTraderIDWithDate(selectedTraderDetails._id, fromSelectDateString[0],toSelectDateString[0]));
                                }



                                for (int i = 0; i < orderDetailsArrayList[0].size(); i++) {
                                    for (int j = 0; j < tempSelectedOrderArrayList.size(); j++) {
                                        if(orderDetailsArrayList[0].get(i).get_id() == tempSelectedOrderArrayList.get(j).get_id()){
                                            orderDetailsArrayList[0].get(i).isSelected = true;
                                        }
                                    }
                                }

                                billOrderListSelectAdapter[0] = new BillOrderListSelectAdapter(orderDetailsArrayList[0], getContext(),db);
                                orderListView.setAdapter(billOrderListSelectAdapter[0]);
//                                billOrderListSelectAdapter.notifyDataSetChanged();

                                mDay[0] = selectedday;
                                mMonth[0] = selectedmonth;
                                mYear[0] = selectedyear;
                            }
                        }, mYear[0], mMonth[0], mDay[0]);

                        mDatePicker.show();
                    }
                });


//                leftSelectDate.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        orderSelectDate[0].add(Calendar.DATE, -1);
//                        String dayString = simpleDateFormat.format(orderSelectDate[0].getTime());
//                        selectDate.setText(dayString);
//                        selectDateString[0] = dayString.toString();
//
//
//                       orderDetailsArrayList[0] = cc.copyOrderListFromCursor(db.getOrderFromOrderTableByTraderIDWithDate(selectedTraderDetails._id, selectDateString[0]));
//
//
//                        for (int i = 0; i < orderDetailsArrayList[0].size(); i++) {
//                            for (int j = 0; j < selectedOrderArrayList.size(); j++) {
//                                if(orderDetailsArrayList[0].get(i).get_id() == selectedOrderArrayList.get(j).get_id()){
//                                    orderDetailsArrayList[0].get(i).isSelected = true;
//                                }
//                            }
//                        }
//                        billOrderListSelectAdapter[0] = new BillOrderListSelectAdapter(orderDetailsArrayList[0], getContext(),db);
//                        orderListView.setAdapter(billOrderListSelectAdapter[0]);
//
////                        billOrderListSelectAdapter[0].notifyDataSetChanged();
////                        updateBillDetailsArrayList(dayString);
//                        //update the data in the listview
//                    }
//                });
//
//                rightSelectDate.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        orderSelectDate[0].add(Calendar.DATE, 1);
//                        String dayString = simpleDateFormat.format(orderSelectDate[0].getTime());
//                        selectDate.setText(dayString);
//                        selectDateString[0] = dayString.toString();
//
//                        orderDetailsArrayList[0] = cc.copyOrderListFromCursor(db.getOrderFromOrderTableByTraderIDWithDate(selectedTraderDetails._id, selectDateString[0]));
//
//
//                        for (int i = 0; i < orderDetailsArrayList[0].size(); i++) {
//                            for (int j = 0; j < selectedOrderArrayList.size(); j++) {
//                                if(orderDetailsArrayList[0].get(i).get_id() == selectedOrderArrayList.get(j).get_id()){
//                                    orderDetailsArrayList[0].get(i).isSelected = true;
//                                }
//                            }
//                        }
//                        billOrderListSelectAdapter[0] = new BillOrderListSelectAdapter(orderDetailsArrayList[0], getContext(),db);
//                        orderListView.setAdapter(billOrderListSelectAdapter[0]);
//
////                        billOrderListSelectAdapter[0].notifyDataSetChanged();
////                        updateBillDetailsArrayList(dayString);
//                        //update the data in the listview
//                    }
//                });




                orderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        OrderDetails orderDetails = orderDetailsArrayList[0].get(position);
                        orderDetails.isSelected = !orderDetails.isSelected;


                        if(orderDetails.isSelected) tempSelectedOrderArrayList.add(orderDetails);
                        else{
                            for(int k = 0; k < tempSelectedOrderArrayList.size();k++){

                                if (orderDetails.get_id() == tempSelectedOrderArrayList.get(k).get_id()) {
                                    tempSelectedOrderArrayList.remove(k);
                                }
                            }

                        }

                        billOrderListSelectAdapter[0].notifyDataSetChanged();

                    }
                });


                okBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        selectedOrderArrayList.clear();
                        selectedOrderArrayList.addAll(tempSelectedOrderArrayList);
//                        selectedOrderArrayList = tempSelectedOrderArrayList;
                        updateBillAmount();
                        updateTotalAmount();
                        billGenerateOrderAdapter.notifyDataSetChanged();
                        bottomSheetDialog.dismiss();
                    }
                });

                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                    }
                });


                bottomSheetDialog.show();

            }
        });


        return view;
    }

    private void updateOrderDetailsArrayList(){

    }

    private void updateBillAmount() {
        billAmount = 0.0f;

        for (int i = 0; i < selectedOrderArrayList.size(); i++) {
            OrderDetails orderDetails = selectedOrderArrayList.get(i);
            float kg = orderDetails.getTotalKG();
            float rate = orderDetails.getRatePerKG();
            int box = orderDetails.getTotalBox();
            float boxPerKg = 35.0f;

            billAmount += (kg + (box * boxPerKg)) * rate;

        }
        billAmountText.setText("" + decimalFormat.format(billAmount));

    }

    private void updateTotalAmount(){
        billBalance = billAmount + balanceAmount;
        totalAmount = billBalance + oldBalanceAmount;
        totalAmountText.setText("" + decimalFormat.format(totalAmount));
        billBalanceText.setText("" + decimalFormat.format(billBalance));

    }

    private void showAlertBuildForBalance(String title, TextView setTextView, int updateValue) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);

        final EditText oldBalaceEdit = new EditText(getContext());
        oldBalaceEdit.setInputType(InputType.TYPE_CLASS_NUMBER);


        if(updateValue == OLD_BALANCE && oldBalanceAmount != 0){
            oldBalaceEdit.setText(decimalFormat.format(oldBalanceAmount));
        } else if (updateValue == BALANCE && balanceAmount != 0) {
            oldBalaceEdit.setText(decimalFormat.format(balanceAmount));
        }

        builder.setView(oldBalaceEdit);



        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String balanceString = oldBalaceEdit.getText().toString();

                float amount = 0.0f;

                try {
                    amount = Float.parseFloat(balanceString);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Enter valid Input", Toast.LENGTH_LONG).show();
                    return;
                }

                if(updateValue == BALANCE) {
                    balanceAmount = amount;

                } else if (updateValue == OLD_BALANCE) {
                    oldBalanceAmount = amount;
                }
                setTextView.setText(decimalFormat.format(amount));
                updateTotalAmount();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

            }
        });
        builder.show();
    }

    public void resetPage(){
        selectedTraderDetails = null;
        updateLastBillNo = db.getLastBillNoFromBillTable()+1;

        billName.setText("");
        billNoEdit.setText("" + updateLastBillNo);

        selectedOrderArrayList.clear();
        tempSelectedOrderArrayList.clear();
        billGenerateOrderAdapter.notifyDataSetChanged();

        balanceAmount = 0;
        oldBalanceAmount = 0;

        balanceText.setText("" + balanceAmount);
        oldBalanceText.setText(""+oldBalanceAmount);

        updateBillAmount();
        updateTotalAmount();

    }
}
