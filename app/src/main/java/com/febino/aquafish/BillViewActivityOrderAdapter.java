package com.febino.aquafish;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.febino.DatabaseManager.CopyCursor;
import com.febino.DatabaseManager.DataBaseManager;
import com.febino.dataclass.OrderDetails;
import com.febino.dataclass.ProductDetails;
import com.febino.dependencies.ProjectUtils;

import java.util.ArrayList;

public class BillViewActivityOrderAdapter extends BaseAdapter implements ListAdapter {

    ArrayList<OrderDetails> orderDetailsArrayList;
    Context context;

    String currentDateString = "";
    String oldDateString = "";
    DataBaseManager dataBaseManager;

    CopyCursor copyCursor;

    public BillViewActivityOrderAdapter(ArrayList<OrderDetails> orderDetailsArrayList, Context context, DataBaseManager dataBaseManager) {
        this.orderDetailsArrayList = orderDetailsArrayList;
        this.context = context;
        this.dataBaseManager = dataBaseManager;
        copyCursor = new CopyCursor();


    }
    @Override
    public int getCount() {
        return orderDetailsArrayList.size();
    }

    @Override
    public OrderDetails getItem(int position) {
        return orderDetailsArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return orderDetailsArrayList.get(position).get_id();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.bill_generate_order_adapter, null);
        }

        OrderDetails orderDetails = orderDetailsArrayList.get(position);
        ProductDetails productDetails = copyCursor.copyProductFromCursor(dataBaseManager.getProductFromProductTableByID(orderDetails.getProductID()));

        TextView date = convertView.findViewById(R.id.bill_generate_order_date);
        TextView breed = convertView.findViewById(R.id.bill_generate_order_breed);
        TextView box = convertView.findViewById(R.id.bill_generate_order_box);
        TextView kg = convertView.findViewById(R.id.bill_generate_order_kg);
        TextView rate = convertView.findViewById(R.id.bill_generate_order_rate);
        TextView amount = convertView.findViewById(R.id.bill_generate_order_amount);

        currentDateString = orderDetails.getOrderDate();
        date.setText("");
        if (!oldDateString.equals(currentDateString)) {
            date.setText(ProjectUtils.convertDateFormatToNormal(currentDateString));
        }

        oldDateString = currentDateString;

//        date.setText(""+orderDetails.getOrderDate());
//        Log.i("P-" + productDetails.productName, "" + productDetails.productName.length());
        breed.setText(""+productDetails.productName);
        box.setText(""+orderDetails.getTotalBox());
        kg.setText(""+orderDetails.getTotalKG());
        rate.setText(""+orderDetails.getRatePerKG());
        float totalKg = orderDetails.getTotalKG() + (orderDetails.getTotalBox() * orderDetails.getKgPerBox());
        amount.setText(""+orderDetails.getRatePerKG()*totalKg);

        return convertView;
    }
}
