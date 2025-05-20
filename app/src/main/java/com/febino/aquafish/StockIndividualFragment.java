package com.febino.aquafish;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.febino.DatabaseManager.CopyCursor;
import com.febino.DatabaseManager.DataBaseManager;
import com.febino.dataclass.ProductDetails;
import com.febino.dataclass.StockDetails;
import com.febino.dataclass.TraderDetails;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;

import androidx.fragment.app.Fragment;

public class StockIndividualFragment extends Fragment {

    DataBaseManager db;

    StockIndividualListViewAdapter stockIndividualListViewAdapter;
    ListView breedListView;

    View view;

    ArrayList<ProductDetails> list = new ArrayList<ProductDetails>();
    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);


    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstance) {
        view = layoutInflater.inflate(R.layout.stock_individual_fragment,container,false);
        //TODO: add typeface for fields
        db = new DataBaseManager(getContext());
        list = new CopyCursor().copyProductListFromCurser(db.getAllProductFromProductTable());
        stockIndividualListViewAdapter = new StockIndividualListViewAdapter(list, getContext());

        breedListView = view.findViewById(R.id.stock_individual_listview);
        breedListView.setAdapter(stockIndividualListViewAdapter);
//        breedListView.setDivider(null);

        breedListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ProductDetails productDetails = list.get(position);
                View warningDialogLayout = getLayoutInflater().inflate(R.layout.dialog_delete_warning,null);

                BottomSheetDialog warningDialogButtom = new BottomSheetDialog(view.getContext());
                warningDialogButtom.setContentView(warningDialogLayout);
//
//                FrameLayout bottomSheet = warningDialogButtom.findViewById(com.google.android.material.R.id.design_bottom_sheet);
//                if (bottomSheet != null) {
//                    BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
//                    behavior.setDraggable(false);
//                }

                warningDialogButtom.show();

//                AlertDialog.Builder warningAlertDialogBuilder = new AlertDialog.Builder(view.getContext());
//                warningAlertDialogBuilder.setView(warningDialogLayout);
//                AlertDialog warningAlert = warningAlertDialogBuilder.show();

                TextView selectedTraderNameTxt = warningDialogLayout.findViewById(R.id.warning_delete_name_textview);
                selectedTraderNameTxt.setText("\""+productDetails.productName+"\"");

                Button yesBtn = warningDialogLayout.findViewById(R.id.warning_dialog_yes_btn);
                Button noBtn = warningDialogLayout.findViewById(R.id.warning_dialog_no_btn);

                yesBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        db.deleteProductDetailsByID(productDetails._id);
                        db.deleteOrderByProductID(productDetails._id);

                        list.remove(position);
                        stockIndividualListViewAdapter.notifyDataSetChanged();
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

        breedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                ProductDetails productDetails = list.get(position);
                final String lastProductShortName = productDetails.shortName;

                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);


                v = layoutInflater.inflate(R.layout.dialog_stock_breed_details, null);

                bottomSheetDialog.setContentView(v);



//                FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
//                if (bottomSheet != null) {
//                    BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
//                    behavior.setDraggable(false);
//                    behavior.setHideable(false);
//                }


                EditText breedName = v.findViewById(R.id.stock_entry_name_edit);
                EditText shortName = v.findViewById(R.id.stock_short_name_edit);
                EditText description = v.findViewById(R.id.stock_entry_description_edit);

                Button okBtn = v.findViewById(R.id.stock_dialog_ok_btn);
                Button cancelBtn = v.findViewById(R.id.stock_dialog_cancel_btn);

                breedName.setText(productDetails.productName);
                shortName.setText(productDetails.shortName);
                description.setText(productDetails.description);

                okBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        ProductDetails productDetails = new ProductDetails();
                        String breedNameString = breedName.getText().toString();
                        String shortNameString = shortName.getText().toString();
                        String descriptionString =  description.getText().toString();

                        if(breedNameString.equals("")
                                || breedNameString.equals(null)
                                || breedNameString.equals(" ")
                                || shortNameString.equals("")
                                || shortNameString.equals(null)
                                || shortNameString.equals(" ")
                        ){
                            Toast.makeText(getContext(), R.string.give_input_for_breedname_and_shortname_field, Toast.LENGTH_LONG).show();
                            return;
                        }

                        if(shortNameString.length() > 4 || shortNameString.length() < 3){
                            Toast.makeText(getContext(), R.string.short_name_not_more_than_4, Toast.LENGTH_LONG).show();
                            return;
                        }



                        if(db.checkProductShortNameExist(shortNameString) && !lastProductShortName.equals(shortNameString)){
                            Toast.makeText(getContext(), R.string.short_name_already_there, Toast.LENGTH_LONG).show();
                            return;
                        }

                        productDetails.productName = breedNameString;
                        productDetails.shortName = shortNameString;
                        productDetails.description = descriptionString;
                        db.updateProductInProductTable(productDetails);

                        updateList(productDetails,position);
                        Log.i(MainActivity.TAG_NAME+"-StockFragment", "Product Updated");
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

    public void updateList(ProductDetails productDetails){
        list.add(productDetails);
        stockIndividualListViewAdapter.notifyDataSetChanged();
    }

    public void updateList(ProductDetails productDetails, int position){
        list.set(position,productDetails);
        stockIndividualListViewAdapter.notifyDataSetChanged();
        breedListView.setAdapter(stockIndividualListViewAdapter);
    }


}
