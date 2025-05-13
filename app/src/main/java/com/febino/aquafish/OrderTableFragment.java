package com.febino.aquafish;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.febino.DatabaseManager.CopyCursor;
import com.febino.DatabaseManager.DataBaseManager;
import com.febino.aquafish.R;
import com.febino.dataclass.BillDetails;
import com.febino.dataclass.OrderDetails;
import com.febino.dataclass.ProductDetails;
import com.febino.dataclass.TraderDetails;
import com.febino.validation.ValidateDetails;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class OrderTableFragment extends Fragment implements HorizontalScroll.ScrollViewListener, VerticalScroll.ScrollViewListener{
    private static int SCREEN_HEIGHT;
    private static int SCREEN_WIDTH;
    RelativeLayout relativeLayoutMain;

    RelativeLayout relativeLayoutA;
    RelativeLayout relativeLayoutB;
    RelativeLayout relativeLayoutC;
    RelativeLayout relativeLayoutD;
    RelativeLayout relativeLayoutE;
    RelativeLayout relativeLayoutF;

    TableLayout tableLayoutA;
    TableLayout tableLayoutB;
    TableLayout tableLayoutC;
    TableLayout tableLayoutD;
    TableLayout tableLayoutE;
    TableLayout tableLayoutF;

    TableRow tableRow;
    TableRow tableRowB;
    TableRow tableRowF;

    HorizontalScroll horizontalScrollViewB;
    HorizontalScroll horizontalScrollViewD;
    HorizontalScroll horizontalScrollViewF;

    VerticalScroll scrollViewC;
    VerticalScroll scrollViewD;

    View relativeView;

    /*
         This is for counting how many columns are added in the row.
    */
    int tableColumnCountB= 0;
    int tableColumnCountF= 0;

    /*
         This is for counting how many row is added.
    */
    int tableRowCountC= 0;

    public DataBaseManager dataBaseManager;
    public ArrayList<TraderDetails> traderDetailsArrayList;
    public ArrayList<ProductDetails> productDetailsArrayList;


    public ArrayList<ArrayList<OrderDetails>> orderDetailsArrayListArray;
    public ArrayList<OrderDetails> totalOrderByProductArray;
    CopyCursor copyCursor;

    public ArrayList<OrderDetails> OrderListByName;

    private String selectedDate;

    private Fragment currentFragment;

    public OrderTableFragment(String selectedDate){
        this.selectedDate = selectedDate;
        currentFragment = this;
    }

    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View onCreateView(LayoutInflater layoutInflater1, ViewGroup viewGroup, Bundle bundle){

        relativeView = layoutInflater1.inflate(R.layout.order_adapter_viewpager, viewGroup, false);



        updateView(relativeView);

        return relativeView;
    }

    public void updateSelectedDate(String selectedDate){
        this.selectedDate = selectedDate;
        getFragmentManager().beginTransaction().detach(this).attach(this).commit();
    }

    private void updateView(View view){
        relativeLayoutMain = null;
        relativeLayoutMain = (RelativeLayout)view.findViewById(R.id.order_adapter_viewpager_layout);
        if(tableRow != null) tableRow.removeAllViews();
        tableRowCountC= 0;
        tableColumnCountB = 0;
        tableColumnCountF = 0;
//        View view = layoutInflater1.inflate(R.layout.order_adapter_viewpager, new TableMainLayout(getContext()), true);
        dataBaseManager = new DataBaseManager(getContext());


        copyCursor = new CopyCursor();
        traderDetailsArrayList =  copyCursor.copyTraderListFromCurser(dataBaseManager.getAllTraderFromTraderTable());
        productDetailsArrayList = copyCursor.copyProductListFromCurser(dataBaseManager.getAllProductFromProductTable());

//        orderDetailsArrayListArray = copyCursor.copyArrayOfOrderListFromCursor(dataBaseManager.getOrderFromOrderTableWithDateAndOrderBy("2024-04-04"),traderDetailsArrayList.size(),productDetailsArrayList.size());
        orderDetailsArrayListArray = copyCursor.copyArrayOfOrderListFromCursor(dataBaseManager.getOrderFromOrderTableWithDateAndOrderBy(selectedDate),traderDetailsArrayList,productDetailsArrayList);



//        int count = 0;
//        for(int i = 0;i<orderDetailsArrayListArray.size();i++){
//            Log.i("order -","-----------------------------------");
//            for(int j=0;j<orderDetailsArrayListArray.get(i).size();j++){
//                OrderDetails orderDetails = orderDetailsArrayListArray.get(i).get(j);
//                Log.i("order -","--------------"+ ++count);
//                Log.i("order ID",""+orderDetails.get_id());
//                Log.i("order TradeID",""+orderDetails.getTraderID());
//                Log.i("order ProductID",""+orderDetails.getProductID());
//                Log.i("order Kgs", "" + orderDetails.getTotalKG());
//                Log.i("order box", "" + orderDetails.getTotalBox());
//                Log.i("order Rate", "" + orderDetails.getRatePerKG());
//                Log.i("order -","--------------");
//            }
//            Log.i("order -","-----------------------------------");
//        }



        getScreenDimension();
        initializeRelativeLayout();
        initializeScrollers();
        initializeTableLayout();
        horizontalScrollViewB.setScrollViewListener(this);
        horizontalScrollViewD.setScrollViewListener(this);
        horizontalScrollViewF.setScrollViewListener(this);
        scrollViewC.setScrollViewListener(this);
        scrollViewD.setScrollViewListener(this);
        addRowToTableA();
        initializeRowForTableB();

        /*
            Till Here.
         */
        /*  There is two unused functions
            Have a look on these functions and try to recreate and use it.
            createCompleteColumn();
            createCompleteRow();
        */

        for(int i=0; i<productDetailsArrayList.size(); i++){
            addColumnsToTableB(productDetailsArrayList.get(i).shortName, i);
        }
        if(productDetailsArrayList.size() < 5){
            for(int i = productDetailsArrayList.size(); i < 5; i++){
                addColumnsToTableB("",i);
            }
        }

        for(int i=0; i<traderDetailsArrayList.size(); i++){
            initializeRowForTableD(i);
            addRowToTableC(traderDetailsArrayList.get(i).name);

            for(int j=0; j<productDetailsArrayList.size(); j++){
                ProductDetails currentProduct = productDetailsArrayList.get(j);
                TraderDetails currentTrader = traderDetailsArrayList.get(i);
                OrderDetails orderDetails = orderDetailsArrayListArray.get(i).get(j);

                if(orderDetails.getTotalKG() == 0 && orderDetails.getTotalBox() == 0 && orderDetails.getRatePerKG() == 0)
                    addColumnToTableAtD(i, "",true,currentTrader._id,currentProduct._id,null);
                else
                    addColumnToTableAtD(i, orderDetails.getTotalBox() + " Box \n"+ orderDetails.getTotalKG() +" Kgs",true,currentTrader._id,currentProduct._id,orderDetails);             //todo:give box and kg here
            }

            for(int j=productDetailsArrayList.size(); j<tableColumnCountB; j++){
                addColumnToTableAtD(i, "",false, 0,0,null);
            }
        }
        if(traderDetailsArrayList.size() < 9){
            for(int i = traderDetailsArrayList.size(); i < 9; i++){
                initializeRowForTableD(i);
                addRowToTableC("");
                for(int j=0; j<tableColumnCountB; j++){
                    addColumnToTableAtD(i, "",false,0,0,null);
//                    addColumnToTableAtD(i, "D "+ i + " " + j,false);
                }
            }
        }

        addRowToTableE();
        updateTableFLayout();


//        for(int i=0; i<10; i++){
//            addColumnsToTableF("Total" + i, i);
//        }
    }

    private void updateTableFLayout() {

        totalOrderByProductArray = copyCursor.copyOrderListFromCursor(dataBaseManager.getOrderTotalByProduct(selectedDate));

        initializeRowForTableF();

        for (int i = 0; i < productDetailsArrayList.size(); i++) {
            String totalBoxAndKg ="" ;
            ProductDetails productDetails = productDetailsArrayList.get(i);
            for(int j=0; j<totalOrderByProductArray.size(); j++){
                OrderDetails totalOrders = totalOrderByProductArray.get(j);
                if (productDetails._id == totalOrders.getProductID()) {
                    totalBoxAndKg = totalOrders.getTotalBox() +" Box \n"+ totalOrders.getTotalKG() + " Kg" ;
                }
            }
            addColumnsToTableF(totalBoxAndKg, i);
        }

        if(totalOrderByProductArray.size() < 5){
            for(int i = totalOrderByProductArray.size(); i < 5; i++){
                addColumnsToTableF("",i);
            }
        }
    }




    private void getScreenDimension(){
        WindowManager wm= (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        SCREEN_WIDTH= size.x;
        SCREEN_HEIGHT = size.y;
//        Log.i("Screen_X", ""+size.x);
//        Log.i("Screen_Y", ""+size.y);
    }

    private void initializeRelativeLayout(){
        relativeLayoutA= new RelativeLayout(getContext());
        relativeLayoutA.setId(R.id.relativeLayoutA);
        relativeLayoutA.setPadding(0,0,0,0);

        relativeLayoutB= new RelativeLayout(getContext());
        relativeLayoutB.setId(R.id.relativeLayoutB);
        relativeLayoutB.setPadding(0,0,0,0);

        relativeLayoutC= new RelativeLayout(getContext());
        relativeLayoutC.setId(R.id.relativeLayoutC);
        relativeLayoutC.setPadding(0,0,0,0);

        relativeLayoutD= new RelativeLayout(getContext());
        relativeLayoutD.setId(R.id.relativeLayoutD);
        relativeLayoutD.setPadding(0,0,0,0);

        relativeLayoutE = new RelativeLayout(getContext());
        relativeLayoutE.setId(R.id.relativeLayoutE);
        relativeLayoutE.setPadding(0, 0, 0, 0);

        relativeLayoutF = new RelativeLayout(getContext());
        relativeLayoutF.setId(R.id.relativeLayoutF);
        relativeLayoutF.setPadding(0, 0, 0, 0);


        relativeLayoutA.setLayoutParams(new RelativeLayout.LayoutParams(SCREEN_WIDTH/5,SCREEN_HEIGHT/20));
        this.relativeLayoutMain.addView(relativeLayoutA);

        RelativeLayout.LayoutParams layoutParamsRelativeLayoutB= new RelativeLayout.LayoutParams(SCREEN_WIDTH- (SCREEN_WIDTH/5), SCREEN_HEIGHT/20);
        layoutParamsRelativeLayoutB.addRule(RelativeLayout.RIGHT_OF, R.id.relativeLayoutA);
        relativeLayoutB.setLayoutParams(layoutParamsRelativeLayoutB);
        this.relativeLayoutMain.addView(relativeLayoutB);

        RelativeLayout.LayoutParams layoutParamsRelativeLayoutC= new RelativeLayout.LayoutParams(SCREEN_WIDTH/5, SCREEN_HEIGHT - (10*SCREEN_WIDTH/20));
        layoutParamsRelativeLayoutC.addRule(RelativeLayout.BELOW, R.id.relativeLayoutA);
        layoutParamsRelativeLayoutC.addRule(RelativeLayout.ABOVE, R.id.relativeLayoutE);
        relativeLayoutC.setLayoutParams(layoutParamsRelativeLayoutC);
        this.relativeLayoutMain.addView(relativeLayoutC);

        RelativeLayout.LayoutParams layoutParamsRelativeLayoutD= new RelativeLayout.LayoutParams(SCREEN_WIDTH- (SCREEN_WIDTH/5), SCREEN_HEIGHT - (10*SCREEN_WIDTH/20));
        layoutParamsRelativeLayoutD.addRule(RelativeLayout.BELOW, R.id.relativeLayoutB);
        layoutParamsRelativeLayoutD.addRule(RelativeLayout.ABOVE, R.id.relativeLayoutF);
        layoutParamsRelativeLayoutD.addRule(RelativeLayout.RIGHT_OF, R.id.relativeLayoutC);
        relativeLayoutD.setLayoutParams(layoutParamsRelativeLayoutD);
        this.relativeLayoutMain.addView(relativeLayoutD);

        //Newly added for E and F
        RelativeLayout.LayoutParams layoutParamsRelativeLayoutE= new RelativeLayout.LayoutParams(SCREEN_WIDTH/5, SCREEN_HEIGHT/20);
        layoutParamsRelativeLayoutE.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        
//        layoutParamsRelativeLayoutE.addRule(RelativeLayout.BELOW, R.id.relativeLayoutC);
        relativeLayoutE.setLayoutParams(layoutParamsRelativeLayoutE);
        this.relativeLayoutMain.addView(relativeLayoutE);

        RelativeLayout.LayoutParams layoutParamsRelativeLayoutF = new RelativeLayout.LayoutParams(SCREEN_WIDTH- (SCREEN_WIDTH/5), SCREEN_HEIGHT/20);
        layoutParamsRelativeLayoutF.addRule(RelativeLayout.ALIGN_TOP,R.id.relativeLayoutE);
//        layoutParamsRelativeLayoutF.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParamsRelativeLayoutF.addRule(RelativeLayout.RIGHT_OF, R.id.relativeLayoutE);
//        layoutParamsRelativeLayoutF.addRule(RelativeLayout.BELOW, R.id.relativeLayoutD);
        relativeLayoutF.setLayoutParams(layoutParamsRelativeLayoutF);
        this.relativeLayoutMain.addView(relativeLayoutF);

    }

    private void initializeScrollers(){
        horizontalScrollViewB= new HorizontalScroll(getContext());
        horizontalScrollViewB.setPadding(0,0,0,0);


        horizontalScrollViewD= new HorizontalScroll(getContext());
        horizontalScrollViewD.setPadding(0,0,0,0);

        //Newly added for E and F
        horizontalScrollViewF= new HorizontalScroll(getContext());
        horizontalScrollViewF.setPadding(0,0,0,0);
        //////

        scrollViewC= new VerticalScroll(getContext());
        scrollViewC.setPadding(0,0,0,0);

        scrollViewD= new VerticalScroll(getContext());
        scrollViewD.setPadding(0,0,0,0);



        horizontalScrollViewB.setLayoutParams(new ViewGroup.LayoutParams(SCREEN_WIDTH - (SCREEN_WIDTH/5), SCREEN_HEIGHT/20));
        scrollViewC.setLayoutParams(new ViewGroup.LayoutParams(SCREEN_WIDTH/5 ,SCREEN_HEIGHT - (2*SCREEN_HEIGHT/20)));
        scrollViewD.setLayoutParams(new ViewGroup.LayoutParams(SCREEN_WIDTH- (SCREEN_WIDTH/5), SCREEN_HEIGHT - (2*SCREEN_HEIGHT/20) ));
        horizontalScrollViewD.setLayoutParams(new ViewGroup.LayoutParams(SCREEN_WIDTH- (SCREEN_WIDTH/5), SCREEN_HEIGHT - (2*SCREEN_HEIGHT/20) ));

        //Newly added for E and F
        horizontalScrollViewF.setLayoutParams(new ViewGroup.LayoutParams(SCREEN_WIDTH - (SCREEN_WIDTH/5), SCREEN_HEIGHT/20));
        ///////

        this.relativeLayoutB.addView(horizontalScrollViewB);

        this.relativeLayoutC.addView(scrollViewC);
        this.scrollViewD.addView(horizontalScrollViewD);
        this.relativeLayoutD.addView(scrollViewD);

        //Newly added for E and F
        this.relativeLayoutF.addView(horizontalScrollViewF);
        ///////
    }

    private  void initializeTableLayout(){
        tableLayoutA= new TableLayout(getContext());
        tableLayoutA.setPadding(0,0,0,0);
        tableLayoutB= new TableLayout(getContext());
        tableLayoutB.setPadding(0,0,0,0);
        tableLayoutB.setId(R.id.tableLayoutB);
        tableLayoutC= new TableLayout(getContext());
        tableLayoutC.setPadding(0,0,0,0);
        tableLayoutD= new TableLayout(getContext());
        tableLayoutD.setPadding(0,0,0,0);
        //Newly added for E and F
        tableLayoutE= new TableLayout(getContext());
        tableLayoutE.setPadding(0,0,0,0);


        tableLayoutF= new TableLayout(getContext());
        tableLayoutF.setPadding(0,0,0,0);
        tableLayoutF.setId(R.id.tableLayoutF);
        /////

        TableLayout.LayoutParams layoutParamsTableLayoutA= new TableLayout.LayoutParams(SCREEN_WIDTH/5, SCREEN_HEIGHT/20);
        tableLayoutA.setLayoutParams(layoutParamsTableLayoutA);
        tableLayoutA.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        this.relativeLayoutA.addView(tableLayoutA);

        TableLayout.LayoutParams layoutParamsTableLayoutB= new TableLayout.LayoutParams(SCREEN_WIDTH -(SCREEN_WIDTH/5), SCREEN_HEIGHT/20);
        tableLayoutB.setLayoutParams(layoutParamsTableLayoutB);
        tableLayoutB.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        this.horizontalScrollViewB.addView(tableLayoutB);

        TableLayout.LayoutParams layoutParamsTableLayoutC= new TableLayout.LayoutParams(SCREEN_WIDTH/5, SCREEN_HEIGHT - (2*SCREEN_HEIGHT/20));
        tableLayoutC.setLayoutParams(layoutParamsTableLayoutC);
        tableLayoutC.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        this.scrollViewC.addView(tableLayoutC);

        TableLayout.LayoutParams layoutParamsTableLayoutD= new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
        tableLayoutD.setLayoutParams(layoutParamsTableLayoutD);
        this.horizontalScrollViewD.addView(tableLayoutD);

        //Newly added for E and F
        TableLayout.LayoutParams layoutParamsTableLayoutE= new TableLayout.LayoutParams(SCREEN_WIDTH/5, SCREEN_HEIGHT/20);
        tableLayoutE.setLayoutParams(layoutParamsTableLayoutE);
        tableLayoutE.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        this.relativeLayoutE.addView(tableLayoutE);

        TableLayout.LayoutParams layoutParamsTableLayoutF= new TableLayout.LayoutParams(SCREEN_WIDTH -(SCREEN_WIDTH/5), SCREEN_HEIGHT/20);
        tableLayoutF.setLayoutParams(layoutParamsTableLayoutF);
        tableLayoutF.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        this.horizontalScrollViewF.addView(tableLayoutF);
//        this.relativeLayoutF.addView(tableLayoutF);

        //////

    }

    @Override
    public void onScrollChanged(HorizontalScroll scrollView, int x, int y, int oldx, int oldy) {
        if(scrollView == horizontalScrollViewB){
            horizontalScrollViewD.scrollTo(x,y);
            //Newly added for E and F
            horizontalScrollViewF.scrollTo(x, y);
            ////
        } else if (scrollView == horizontalScrollViewF) {
            horizontalScrollViewB.scrollTo(x, y);
            horizontalScrollViewD.scrollTo(x, y);
        }
        else if(scrollView == horizontalScrollViewD){
            horizontalScrollViewB.scrollTo(x, y);
            //Newly added for E and F
            horizontalScrollViewF.scrollTo(x, y);
            ////
        }
    }

    @Override
    public void onScrollChanged(VerticalScroll scrollView, int x, int y, int oldx, int oldy) {
        if(scrollView == scrollViewC){
            scrollViewD.scrollTo(x,y);
        }
        else if(scrollView == scrollViewD){
            scrollViewC.scrollTo(x,y);
        }
    }

    private void addRowToTableA(){
        tableRow= new TableRow(getContext());
        TableRow.LayoutParams layoutParamsTableRow= new TableRow.LayoutParams(SCREEN_WIDTH/5, SCREEN_HEIGHT/20);
        tableRow.setLayoutParams(layoutParamsTableRow);
        TextView label_date = new TextView(getContext());
        label_date.setText("Name/Product");
        tableRow.setGravity(Gravity.CENTER);
//        tableRow.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
        label_date.setTextSize(getResources().getDimension(R.dimen.cell_text_size));
        tableRow.addView(label_date);
        this.tableLayoutA.addView(tableRow);
    }

    private void initializeRowForTableB(){
        tableRowB= new TableRow(getContext());
        tableRow.setPadding(0,0,0,0);
        this.tableLayoutB.addView(tableRowB);
    }

    private synchronized void addColumnsToTableB(String text, final int id){
        tableRow= new TableRow(getContext());
        TableRow.LayoutParams layoutParamsTableRow= new TableRow.LayoutParams(SCREEN_WIDTH/5, SCREEN_HEIGHT/20);
        tableRow.setPadding(3,3,3,4);
        tableRow.setLayoutParams(layoutParamsTableRow);
        TextView label_date = new TextView(getContext());
        label_date.setText(text);
        label_date.setTextSize(getResources().getDimension(R.dimen.cell_text_size));
        this.tableRow.addView(label_date);
        this.tableRow.setGravity(Gravity.CENTER);
        this.tableRow.setTag(id);
        this.tableRowB.addView(tableRow);
        tableColumnCountB++;
    }

    private synchronized void addRowToTableC(String text){
        TableRow tableRow1= new TableRow(getContext());
        TableRow.LayoutParams layoutParamsTableRow1= new TableRow.LayoutParams(SCREEN_WIDTH/5, SCREEN_HEIGHT/20);
        tableRow1.setPadding(3,3,3,4);
        tableRow1.setGravity(Gravity.CENTER);
        tableRow1.setLayoutParams(layoutParamsTableRow1);
        TextView label_date = new TextView(getContext());
        label_date.setText(text);
        label_date.setTextSize(getResources().getDimension(R.dimen.cell_text_size));
        tableRow1.addView(label_date);

        TableRow tableRow= new TableRow(getContext());
        TableRow.LayoutParams layoutParamsTableRow= new TableRow.LayoutParams(SCREEN_WIDTH/5, SCREEN_HEIGHT/20);
        tableRow.setPadding(0,0,0,0);
        tableRow.setLayoutParams(layoutParamsTableRow);
        tableRow.setGravity(Gravity.CENTER);
        tableRow.addView(tableRow1);
        this.tableLayoutC.addView(tableRow, tableRowCountC);
        tableRowCountC++;
    }

    private synchronized void initializeRowForTableD(int pos){
        TableRow tableRowB= new TableRow(getContext());
        TableRow.LayoutParams layoutParamsTableRow= new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, SCREEN_HEIGHT/20);
        tableRowB.setPadding(0,0,0,0);
        tableRowB.setLayoutParams(layoutParamsTableRow);
        this.tableLayoutD.addView(tableRowB, pos);
    }

    private synchronized void updateColumntToTableAtD(final int rowPos, String text, boolean isClickListenerSet, long traderID, long productID, OrderDetails currentOrderDetails){

        int cellIndex = -1;
        for(int i=0; i<productDetailsArrayList.size(); i++){
            if (productDetailsArrayList.get(i)._id == productID) {
                cellIndex = i;
                break;
            }
        }

        TableRow tableRowToUpdate = (TableRow) tableLayoutD.getChildAt(rowPos);
        TableRow cellToUpdate = (TableRow) tableRowToUpdate.getVirtualChildAt(cellIndex);

        if (cellIndex == -1 || cellToUpdate == null) {
            Toast.makeText(getContext(), "No product found for this order", Toast.LENGTH_SHORT).show();
            return;
        }

        TextView label_date = (TextView) cellToUpdate.getTag();
        label_date.setText(text);
        label_date.setTextSize(getResources().getDimension(R.dimen.cell_text_size));
        label_date.setPadding(20,0,0,0);
        cellToUpdate.setTag(label_date);

        cellToUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.dialog_order_details, null);

                OrderDetails orderDetails = new OrderDetails();


                TextView alertMessage = v.findViewById(R.id.dialog_order_message_txt);

                EditText dialogOrderName = v.findViewById(R.id.order_name_edit);
                EditText dialogOrderProduct = v.findViewById(R.id.order_item_edit);
                EditText dialogOrderDate = v.findViewById(R.id.order_date_edit);
                EditText dialogOrderBox = v.findViewById(R.id.order_box_edit);
                EditText dialogOrderKgs = v.findViewById(R.id.order_kgs_edit);
                EditText dialogOrderRate = v.findViewById(R.id.order_rate_edit);

                ImageButton dialogOrderDeleteBtn = v.findViewById(R.id.dialog_add_order_delete_img_btn);

                Button okBtn = v.findViewById(R.id.order_dialog_ok_btn);
                Button cancelBtn = v.findViewById(R.id.order_dialog_cancel_btn);


                TraderDetails traderDetailsfinal = new CopyCursor().copyTraderFromCursor(dataBaseManager.getTraderInTraderTableByID(traderID));
                ProductDetails productDetailsfinal = new CopyCursor().copyProductFromCursor(dataBaseManager.getProductFromProductTableByID(productID));

                dialogOrderDeleteBtn.setVisibility(View.GONE);


                if (traderID != 0 || productID != 0) {
                    dialogOrderName.setText(""+traderDetailsfinal.name);
                    dialogOrderProduct.setText(""+productDetailsfinal.productName);
                }

                if(currentOrderDetails != null){
                    dialogOrderBox.setText("" + currentOrderDetails.getTotalBox());
                    dialogOrderKgs.setText(""+currentOrderDetails.getTotalKG());
                    dialogOrderRate.setText(""+currentOrderDetails.getRatePerKG());
                    dialogOrderDeleteBtn.setVisibility(View.VISIBLE);
                }

                dialogOrderDate.setText(selectedDate);


                if (currentOrderDetails != null && (currentOrderDetails.isBilled() || currentOrderDetails.getBillID() > 0)) {
                    BillDetails billDetails = copyCursor.copyBillFromCursor(dataBaseManager.getBillFromBillTableByID(currentOrderDetails.getBillID()));
                    String alertMessageString = getResources().getString(R.string.order_added_to_bill) +  billDetails.getBillNo() + " on " + billDetails.getBillDate();

                    alertMessage.setText(alertMessageString);

                    dialogOrderBox.setFocusable(false);
                    dialogOrderKgs.setFocusable(false);
                    dialogOrderRate.setFocusable(false);

                    alertMessage.setVisibility(View.VISIBLE);
                    okBtn.setVisibility(View.GONE);
                    dialogOrderDeleteBtn.setVisibility(View.GONE);
                }

                dialogOrderDeleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(getContext());

                        deleteDialog.setTitle("Delete Order");

                        TextView messageText = new TextView(getContext());

                        messageText.setText("Are you sure want to delete order?");
                        messageText.setPadding(50,50,10,20);

                        deleteDialog.setView(messageText);

                        deleteDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dataBaseManager.deleteOrderByID(currentOrderDetails.get_id());
                                Toast.makeText(getContext(), "Order Deleted", Toast.LENGTH_SHORT).show();
                                bottomSheetDialog.dismiss();
                                getFragmentManager().beginTransaction().detach(currentFragment).attach(currentFragment).commit();
                            }
                        });

                        deleteDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });



                        deleteDialog.show();
                    }
                });




                okBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String orderBoxString = dialogOrderBox.getText().toString();
                        String orderKGString = dialogOrderKgs.getText().toString();
                        String orderRateString = dialogOrderRate.getText().toString();
                        String orderKGPerBoxString = "35.0"; //TODO: add 35kg/box in the settings page
                        String errorMessage = ValidateDetails.ValidateOrderDetails(getContext(),orderBoxString, orderKGString, orderRateString);

                        if (errorMessage != null) {
                            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                            return;
                        }

                        if(currentOrderDetails !=null){
                            currentOrderDetails.setTotalBox(Integer.parseInt(orderBoxString));
                            currentOrderDetails.setTotalKG(Float.parseFloat(orderKGString));
                            currentOrderDetails.setRatePerKG(Float.parseFloat(orderRateString));
                            currentOrderDetails.setKgPerBox(Float.parseFloat(orderKGPerBoxString));
                            dataBaseManager.updateOrderInOrderTable(currentOrderDetails);
                            Toast.makeText(getContext(),R.string.order_updated,Toast.LENGTH_LONG).show();
                            updateColumntToTableAtD(rowPos, currentOrderDetails.getTotalBox() + " aaBox \n"+ currentOrderDetails.getTotalKG() +" Kgs",true,traderID,productID,currentOrderDetails);
                        }else{
                            orderDetails.setOrderDate(dialogOrderDate.getText().toString());
                            orderDetails.setProductID(productID);
                            orderDetails.setTraderID(traderID);
                            orderDetails.setTotalBox(Integer.parseInt(orderBoxString));
                            orderDetails.setTotalKG(Float.parseFloat(orderKGString));
                            orderDetails.setRatePerKG(Float.parseFloat(orderRateString));
                            orderDetails.setKgPerBox(Float.parseFloat(orderKGPerBoxString));

                            long savedID = dataBaseManager.addOrderInOrderTableReturnID(orderDetails);
                            orderDetails.set_id(savedID);
                            Toast.makeText(getContext(),R.string.order_saved,Toast.LENGTH_LONG).show();
                            updateColumntToTableAtD(rowPos, orderDetails.getTotalBox() + " Box \n"+ orderDetails.getTotalKG() +" Kgs",true,traderID,productID,orderDetails);
                        }




                        bottomSheetDialog.dismiss();
                    }
                });

                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();

                    }
                });


                bottomSheetDialog.setContentView(v);
                bottomSheetDialog.show();
//                Toast.makeText(getContext(),tempText,Toast.LENGTH_SHORT).show();
            }
        });

        if(!isClickListenerSet) cellToUpdate.setOnClickListener(null);

        updateTableFLayout();

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private synchronized void addColumnToTableAtD(final int rowPos, String text,boolean isClickListenerSet, final long traderID, final long productID,OrderDetails currentOrderDetails){

//        final long traderIDRow = traderID;
//        final long productIDColumn = productID;

        TableRow tableRowAdd= (TableRow) this.tableLayoutD.getChildAt(rowPos);
        tableRow= new TableRow(getContext());
        TableRow.LayoutParams layoutParamsTableRow= new TableRow.LayoutParams(SCREEN_WIDTH/5, SCREEN_HEIGHT/20);
        tableRow.setPadding(3,3,3,4);
        tableRow.setBackground(getResources().getDrawable(R.drawable.cell_bacground));
        tableRow.setLayoutParams(layoutParamsTableRow);
        TextView label_date = new TextView(getContext());
        label_date.setText(text);
        label_date.setTextSize(getResources().getDimension(R.dimen.cell_text_size));
        label_date.setPadding(20,0,0,0);
        tableRow.setTag(label_date);
        this.tableRow.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
        this.tableRow.addView(label_date);

        tableRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.dialog_order_details, null);

                OrderDetails orderDetails = new OrderDetails();


                TextView alertMessage = v.findViewById(R.id.dialog_order_message_txt);

                EditText dialogOrderName = v.findViewById(R.id.order_name_edit);
                EditText dialogOrderProduct = v.findViewById(R.id.order_item_edit);
                EditText dialogOrderDate = v.findViewById(R.id.order_date_edit);
                EditText dialogOrderBox = v.findViewById(R.id.order_box_edit);
                EditText dialogOrderKgs = v.findViewById(R.id.order_kgs_edit);
                EditText dialogOrderRate = v.findViewById(R.id.order_rate_edit);

                ImageButton dialogOrderDeleteBtn = v.findViewById(R.id.dialog_add_order_delete_img_btn);

                Button okBtn = v.findViewById(R.id.order_dialog_ok_btn);
                Button cancelBtn = v.findViewById(R.id.order_dialog_cancel_btn);


                TraderDetails traderDetailsfinal = new CopyCursor().copyTraderFromCursor(dataBaseManager.getTraderInTraderTableByID(traderID));
                ProductDetails productDetailsfinal = new CopyCursor().copyProductFromCursor(dataBaseManager.getProductFromProductTableByID(productID));

                dialogOrderDeleteBtn.setVisibility(View.GONE);


                if (traderID != 0 || productID != 0) {
                    dialogOrderName.setText(""+traderDetailsfinal.name);
                    dialogOrderProduct.setText(""+productDetailsfinal.productName);
                }

                if(currentOrderDetails != null){
                    dialogOrderBox.setText("" + currentOrderDetails.getTotalBox());
                    dialogOrderKgs.setText(""+currentOrderDetails.getTotalKG());
                    dialogOrderRate.setText(""+currentOrderDetails.getRatePerKG());
                    dialogOrderDeleteBtn.setVisibility(View.VISIBLE);
                }

                dialogOrderDate.setText(selectedDate);


                if (currentOrderDetails != null && (currentOrderDetails.isBilled() || currentOrderDetails.getBillID() > 0)) {
                    BillDetails billDetails = copyCursor.copyBillFromCursor(dataBaseManager.getBillFromBillTableByID(currentOrderDetails.getBillID()));
                    String alertMessageString = getResources().getString(R.string.order_added_to_bill) +  billDetails.getBillNo() + " on " + billDetails.getBillDate();

                    alertMessage.setText(alertMessageString);

                    dialogOrderBox.setFocusable(false);
                    dialogOrderKgs.setFocusable(false);
                    dialogOrderRate.setFocusable(false);

                    alertMessage.setVisibility(View.VISIBLE);
                    okBtn.setVisibility(View.GONE);
                    dialogOrderDeleteBtn.setVisibility(View.GONE);
                }

                dialogOrderDeleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(getContext());

                        deleteDialog.setTitle("Delete Order");

                        TextView messageText = new TextView(getContext());

                        messageText.setText("Are you sure want to delete order?");
                        messageText.setPadding(50,50,10,20);

                        deleteDialog.setView(messageText);

                        deleteDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dataBaseManager.deleteOrderByID(currentOrderDetails.get_id());
                                Toast.makeText(getContext(), "Order Deleted", Toast.LENGTH_SHORT).show();
                                bottomSheetDialog.dismiss();
//                                getFragmentManager().beginTransaction().detach(currentFragment).attach(currentFragment).commit();
                                updateColumntToTableAtD(rowPos, "",true,traderID,productID,null);
                            }
                        });

                        deleteDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });



                        deleteDialog.show();
                    }
                });




                okBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String orderBoxString = dialogOrderBox.getText().toString();
                        String orderKGString = dialogOrderKgs.getText().toString();
                        String orderRateString = dialogOrderRate.getText().toString();
                        String orderKGPerBoxString = "35.0"; //TODO: add 35kg/box in the settings page
                        String errorMessage = ValidateDetails.ValidateOrderDetails(getContext(),orderBoxString, orderKGString, orderRateString);

                        if (errorMessage != null) {
                            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                            return;
                        }

                        if(currentOrderDetails !=null){
                            currentOrderDetails.setTotalBox(Integer.parseInt(orderBoxString));
                            currentOrderDetails.setTotalKG(Float.parseFloat(orderKGString));
                            currentOrderDetails.setRatePerKG(Float.parseFloat(orderRateString));
                            currentOrderDetails.setKgPerBox(Float.parseFloat(orderKGPerBoxString));
                            dataBaseManager.updateOrderInOrderTable(currentOrderDetails);
                            Toast.makeText(getContext(),R.string.order_updated,Toast.LENGTH_LONG).show();
                            updateColumntToTableAtD(rowPos, currentOrderDetails.getTotalBox() + " Box \n"+ currentOrderDetails.getTotalKG() +" Kgs",true,traderID,productID,currentOrderDetails);
                        }else{
                            orderDetails.setOrderDate(dialogOrderDate.getText().toString());
                            orderDetails.setProductID(productID);
                            orderDetails.setTraderID(traderID);
                            orderDetails.setTotalBox(Integer.parseInt(orderBoxString));
                            orderDetails.setTotalKG(Float.parseFloat(orderKGString));
                            orderDetails.setRatePerKG(Float.parseFloat(orderRateString));
                            orderDetails.setKgPerBox(Float.parseFloat(orderKGPerBoxString));

                            long savedID = dataBaseManager.addOrderInOrderTableReturnID(orderDetails);
                            orderDetails.set_id(savedID);
                            Toast.makeText(getContext(),R.string.order_saved,Toast.LENGTH_LONG).show();
                            updateColumntToTableAtD(rowPos, orderDetails.getTotalBox() + " Box \n"+ orderDetails.getTotalKG() +" Kgs",true,traderID,productID,orderDetails);
                        }



                        bottomSheetDialog.dismiss();
                    }
                });

                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();

                    }
                });


                bottomSheetDialog.setContentView(v);
                bottomSheetDialog.show();
//                Toast.makeText(getContext(),tempText,Toast.LENGTH_SHORT).show();
            }
        });

        if(!isClickListenerSet) tableRow.setOnClickListener(null);

        tableRowAdd.addView(tableRow);
    }



    private void addRowToTableE(){
        tableRow= new TableRow(getContext());
        TableRow.LayoutParams layoutParamsTableRow= new TableRow.LayoutParams(SCREEN_WIDTH/5, SCREEN_HEIGHT/20);
        tableRow.setLayoutParams(layoutParamsTableRow);
        TextView label_date = new TextView(getContext());
        label_date.setText("Total");
        tableRow.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
        label_date.setTextSize(getResources().getDimension(R.dimen.cell_text_size));
        tableRow.addView(label_date);
        this.tableLayoutE.addView(tableRow);
    }

    private void initializeRowForTableF(){
        this.tableLayoutF.removeAllViews();
        tableRowF= new TableRow(getContext());
        tableRowF.setPadding(0,0,0,0);
        this.tableLayoutF.addView(tableRowF);

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private synchronized void addColumnsToTableF(String text, final int id){
        tableRow= new TableRow(getContext());
        TableRow.LayoutParams layoutParamsTableRow= new TableRow.LayoutParams(SCREEN_WIDTH/5, SCREEN_HEIGHT/20);
        tableRow.setPadding(3,3,3,4);
        tableRow.setLayoutParams(layoutParamsTableRow);
//        tableRow.setBackground(getResources().getDrawable(R.drawable.cell_bacground));
        TextView label_date = new TextView(getContext());
        label_date.setText(text);
        label_date.setTextSize(getResources().getDimension(R.dimen.cell_text_size));
        this.tableRow.addView(label_date);
        this.tableRow.setTag(id);
        this.tableRow.setGravity(Gravity.CENTER);
        this.tableRowF.addView(tableRow);
        tableColumnCountF++;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void createCompleteColumn(String value){
        int i=0;
        int j=tableRowCountC-1;
        for(int k=i; k<=j; k++){
//            addColumnToTableAtD(k, value);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void createCompleteRow(String value){
        initializeRowForTableD(0);
        int i=0;
        int j=tableColumnCountB-1;
        int pos= tableRowCountC-1;
        for(int k=i; k<=j; k++){
//            addColumnToTableAtD(pos, value);
        }
    }
}
