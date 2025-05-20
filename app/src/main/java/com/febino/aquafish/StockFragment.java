package com.febino.aquafish;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.febino.DatabaseManager.DataBaseManager;
import com.febino.aquafish.R;
import com.febino.dataclass.ProductDetails;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class StockFragment extends Fragment {

    Button stockIndividualTabBtn,stockTableTabBtn,stockEntryTabBtn;
    ViewPager viewPager;
    ImageButton stockAddBtn;

    DataBaseManager db;

    StockIndividualFragment stockIndividualFragment;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

    }

    @Override
    public View onCreateView(final LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstance) {
        SpannableString s = new SpannableString(getResources().getString(R.string.stock).toUpperCase());
        s.setSpan(new RelativeSizeSpan(0.8f), 0,s.length(), 0);
        s.setSpan(new TypefaceSpan(getContext(), "unicode.futurab.ttf"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        final View view = layoutInflater.inflate(R.layout.fragment_stock, container, false);
        getActivity().setTitle(s);

        db = new DataBaseManager(getContext());


        stockEntryTabBtn = view.findViewById(R.id.stock_entry_tab);
        stockIndividualTabBtn = view.findViewById(R.id.stock_individual_tab);
        stockTableTabBtn = view.findViewById(R.id.stock_table_tab);

        stockAddBtn = view.findViewById(R.id.stock_add_button);

        viewPager = view.findViewById(R.id.stock_view_pager);

        stockIndividualFragment = new StockIndividualFragment();

        final ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPagerAdapter.addFragment(stockIndividualFragment,"Stock Individual Fragement");
//        viewPagerAdapter.addFragment(new StockTableFragment(),"Stock Table Fragement");
//        viewPagerAdapter.addFragment(new StockEntryFragment(),"Stock Entry Fragement");

        viewPager.setAdapter(viewPagerAdapter);

        stockEntryTabBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                selectEntryTab();
                viewPager.setCurrentItem(2,true);
            }
        });

        stockTableTabBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                selectTableTab();
                viewPager.setCurrentItem(1,true);
            }
        });

        stockIndividualTabBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                selectIndividualTab();
                viewPager.setCurrentItem(0,true);

            }
        });

        stockAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                int viewpagerID = viewPager.getCurrentItem();
                if(viewpagerID == 0){
                    v = layoutInflater.inflate(R.layout.dialog_stock_breed_details, null);
                    EditText breedName = v.findViewById(R.id.stock_entry_name_edit);
                    EditText shortName = v.findViewById(R.id.stock_short_name_edit);
                    EditText description = v.findViewById(R.id.stock_entry_description_edit);

                    Button okBtn = v.findViewById(R.id.stock_dialog_ok_btn);
                    Button cancelBtn = v.findViewById(R.id.stock_dialog_cancel_btn);

                    okBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ProductDetails productDetails = new ProductDetails();
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



                            if(db.checkProductShortNameExist(shortNameString)){
                                Toast.makeText(getContext(), R.string.short_name_already_there, Toast.LENGTH_LONG).show();
                                return;
                            }

                            productDetails.productName = breedNameString;
                            productDetails.shortName = shortNameString;
                            productDetails.description = descriptionString;
                            long productID = db.addProductInProductTableReturnID(productDetails);

                            if(productID > 0) {
                                productDetails._id = productID;
                                stockIndividualFragment.updateList(productDetails);
                                Log.i(MainActivity.TAG_NAME+"-StockFragment", "Product Added");
                                bottomSheetDialog.dismiss();
                            }else{
                                Toast.makeText(getContext(), R.string.error_in_product_data, Toast.LENGTH_LONG).show();
//                                Log.i(MainActivity.TAG_NAME+"-StockFragment", "Product not added.");
                            }



                        }
                    });

                    cancelBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            bottomSheetDialog.dismiss();
                        }
                    });
                }else{
                    v = layoutInflater.inflate(R.layout.dialog_stock_entry_details, null);


                }


                bottomSheetDialog.setContentView(v);

//                FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
//                if (bottomSheet != null) {
//                    BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
//                    behavior.setDraggable(false);
//                    behavior.setHideable(false);
//                }
                bottomSheetDialog.show();
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0:
                        selectIndividualTab();
                        break;
                    case 1:
                        selectTableTab();
                        break;
                    case 2:
                        selectEntryTab();
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void selectIndividualTab(){
        stockIndividualTabBtn.setBackground(getContext().getResources().getDrawable(R.drawable.order_tab_table_bg_selected));
        stockIndividualTabBtn.setTextColor(getContext().getResources().getColor(R.color.white));
        stockTableTabBtn.setBackground(getContext().getResources().getDrawable(R.drawable.stock_table_bg_unselected));
        stockTableTabBtn.setTextColor(getContext().getResources().getColor(R.color.colorPrimaryDark));
        stockEntryTabBtn.setBackground(getContext().getResources().getDrawable(R.drawable.order_tab_list_bg_unselected));
        stockEntryTabBtn.setTextColor(getContext().getResources().getColor(R.color.colorPrimaryDark));
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void selectTableTab(){
        stockIndividualTabBtn.setBackground(getContext().getResources().getDrawable(R.drawable.order_tab_table_bg_unselected));
        stockIndividualTabBtn.setTextColor(getContext().getResources().getColor(R.color.colorPrimaryDark));
        stockTableTabBtn.setBackground(getContext().getResources().getDrawable(R.drawable.stock_table_bg_selected));
        stockTableTabBtn.setTextColor(getContext().getResources().getColor(R.color.white));
        stockEntryTabBtn.setBackground(getContext().getResources().getDrawable(R.drawable.order_tab_list_bg_unselected));
        stockEntryTabBtn.setTextColor(getContext().getResources().getColor(R.color.colorPrimaryDark));

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void selectEntryTab(){
        stockIndividualTabBtn.setBackground(getContext().getResources().getDrawable(R.drawable.order_tab_table_bg_unselected));
        stockIndividualTabBtn.setTextColor(getContext().getResources().getColor(R.color.colorPrimaryDark));
        stockTableTabBtn.setBackground(getContext().getResources().getDrawable(R.drawable.stock_table_bg_unselected));
        stockTableTabBtn.setTextColor(getContext().getResources().getColor(R.color.colorPrimaryDark));
        stockEntryTabBtn.setBackground(getContext().getResources().getDrawable(R.drawable.order_tab_list_bg_selected));
        stockEntryTabBtn.setTextColor(getContext().getResources().getColor(R.color.white));

    }
}
