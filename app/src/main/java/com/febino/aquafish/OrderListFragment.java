package com.febino.aquafish;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.febino.DatabaseManager.CopyCursor;
import com.febino.DatabaseManager.DataBaseManager;
import com.febino.dataclass.OrderDetails;
import com.febino.dataclass.ProductDetails;
import com.febino.dataclass.TraderDetails;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;

import androidx.fragment.app.Fragment;

public class OrderListFragment extends Fragment {
    DataBaseManager dataBaseManager;
    ArrayList<OrderDetails> list;
    OrderListViewAdapter orderListViewAdapter;
    CopyCursor cc;
    ListView orderListView;

    TextView orderCountText;
    private String selectedDate;
    public OrderListFragment(String selectedDate){
        this.selectedDate = selectedDate;
    }

    public void updateSelectedDate(String selectedDate){
        this.selectedDate = selectedDate;
        updateData();
    }
    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater1, ViewGroup viewGroup, Bundle bundle){
        View view = layoutInflater1.inflate(R.layout.order_list_fragment, viewGroup, false);

        orderListView = view.findViewById(R.id.order_list_tab_listview);
        orderCountText = view.findViewById(R.id.order_list_count_txt);
        list = new ArrayList<OrderDetails>();
        dataBaseManager = new DataBaseManager(getContext());
        cc = new CopyCursor();

        list = cc.copyOrderListFromCursor(dataBaseManager.getOrderFromOrderTableByDate(selectedDate));

        orderCountText.setText(""+list.size());

        orderListViewAdapter = new OrderListViewAdapter(list, getContext());
        orderListView.setAdapter(orderListViewAdapter);

        orderListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(getContext(),"Delete - "+list.get(position).getTotalBox()+" "+list.get(position).getTotalKG(),Toast.LENGTH_LONG).show();
                OrderDetails orderDetails = list.get(position);

                ProductDetails productDetails = new CopyCursor().copyProductFromCursor(dataBaseManager.getProductFromProductTableByID(orderDetails.getProductID()));
                TraderDetails traderDetails = new CopyCursor().copyTraderFromCursor(dataBaseManager.getTraderInTraderTableByID(orderDetails.getTraderID()));

//                Toast.makeText(getContext(),"Deleted - "+orderDetails.get_id()+","+orderDetails.getTotalBox()+" "+orderDetails.getTotalKG(),Toast.LENGTH_LONG).show();
//                list.remove(position);


                View warningDialogLayout = getLayoutInflater().inflate(R.layout.dialog_delete_warning,null);

                BottomSheetDialog warningDialogButtom = new BottomSheetDialog(view.getContext());
                warningDialogButtom.setContentView(warningDialogLayout);
//                FrameLayout bottomSheet = warningDialogButtom.findViewById(com.google.android.material.R.id.design_bottom_sheet);
//                if (bottomSheet != null) {
//                    BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
//                    behavior.setDraggable(false);
//                    behavior.setHideable(false);
//                }
                warningDialogButtom.show();

//                AlertDialog.Builder warningAlertDialogBuilder = new AlertDialog.Builder(view.getContext());
//                warningAlertDialogBuilder.setView(warningDialogLayout);
//                AlertDialog warningAlert = warningAlertDialogBuilder.show();

                TextView selectedTraderNameTxt = warningDialogLayout.findViewById(R.id.warning_delete_name_textview);
                selectedTraderNameTxt.setText("\n"+traderDetails.name+" \n"+productDetails.productName+" \n"+orderDetails.getTotalBox()+" Box \n" +orderDetails.getTotalKG()+" Kgs");

                Button yesBtn = warningDialogLayout.findViewById(R.id.warning_dialog_yes_btn);
                Button noBtn = warningDialogLayout.findViewById(R.id.warning_dialog_no_btn);

                yesBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dataBaseManager.deleteOrderByID(orderDetails.get_id());
                        orderListViewAdapter.notifyDataSetChanged();
                        list.remove(position);
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

                return false;
            }
        });

        return view;
    }

    public void updateData(){
        list = cc.copyOrderListFromCursor(dataBaseManager.getOrderFromOrderTableByDate(selectedDate));
        orderListViewAdapter = new OrderListViewAdapter(list, getContext());
        orderListView.setAdapter(orderListViewAdapter);
        orderListViewAdapter.notifyDataSetChanged();
        orderCountText.setText(""+list.size());
    }
}
