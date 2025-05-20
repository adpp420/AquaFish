package com.febino.aquafish;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.febino.aquafish.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class OrderFragment extends Fragment {
    private Button tableTabBtn;
    private Button listTabBtn;

    private EditText dateEditText;
    private ImageButton rightArrow;
    private ImageButton leftArrow;
    String dateTimeFormat = "yyyy-MM-dd";
    SimpleDateFormat simpleDateFormat;

    Calendar mcurrentDate;

    Calendar calendarEditTextDate;
    OrderTableFragment orderTableFragment;
    OrderListFragment orderListFragment;
    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstance) {

        SpannableString s = new SpannableString(getResources().getString(R.string.order).toUpperCase());
        s.setSpan(new RelativeSizeSpan(0.8f), 0,s.length(), 0);
        s.setSpan(new TypefaceSpan(getContext(), "unicode.futurab.ttf"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        View view = layoutInflater.inflate(R.layout.fragment_order, container, false);
        getActivity().setTitle(s);

        simpleDateFormat = new SimpleDateFormat(dateTimeFormat);
        mcurrentDate = Calendar.getInstance();

        calendarEditTextDate = mcurrentDate;

        final ViewPager viewPager = view.findViewById(R.id.order_view_pager);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        orderTableFragment = new OrderTableFragment(simpleDateFormat.format(calendarEditTextDate.getTime()));
        orderListFragment = new OrderListFragment(simpleDateFormat.format(calendarEditTextDate.getTime()));

        viewPagerAdapter.addFragment(orderTableFragment,"Order Table Fragement");
        viewPagerAdapter.addFragment(orderListFragment,"Order List Fragement");
        viewPager.setAdapter(viewPagerAdapter);

        tableTabBtn = view.findViewById(R.id.order_table_tab_btn);
        listTabBtn = view.findViewById(R.id.order_list_tab_btn);
        ImageButton addImgBtn = view.findViewById(R.id.order_add_btn);
        dateEditText = view.findViewById(R.id.order_fragment_date_edittext);
        rightArrow = view.findViewById(R.id.order_table_right_arrow);
        leftArrow = view.findViewById(R.id.order_table_left_arrow);



        dateEditText.setText(simpleDateFormat.format(calendarEditTextDate.getTime()));

        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final int[] mYear = {calendarEditTextDate.get(Calendar.YEAR)};
                final int[] mMonth = {calendarEditTextDate.get(Calendar.MONTH)};
                final int[] mDay = {calendarEditTextDate.get(Calendar.DAY_OF_MONTH)};

                DatePickerDialog mDatePicker = new DatePickerDialog(getContext(),R.style.datepicker, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        Calendar myCalendar = Calendar.getInstance();
                        myCalendar.set(Calendar.YEAR, selectedyear);
                        myCalendar.set(Calendar.MONTH, selectedmonth);
                        myCalendar.set(Calendar.DAY_OF_MONTH, selectedday);

                        calendarEditTextDate = myCalendar;
                        String dayString = simpleDateFormat.format(calendarEditTextDate.getTime());
                        dateEditText.setText(dayString);
                        updateFragment(dayString);
                        mDay[0] = selectedday;
                        mMonth[0] = selectedmonth;
                        mYear[0] = selectedyear;
                    }
                }, mYear[0], mMonth[0], mDay[0]);
                //mDatePicker.setTitle("Select date");
                mDatePicker.show();
            }
        });

        rightArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                calendarEditTextDate.add(Calendar.DATE,1);
                String dayString = simpleDateFormat.format(calendarEditTextDate.getTime());
                dateEditText.setText(dayString);
                updateFragment(dayString);

            }
        });

        leftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarEditTextDate.add(Calendar.DATE,-1);
                String dayString = simpleDateFormat.format(calendarEditTextDate.getTime());
                dateEditText.setText(dayString);
                updateFragment(dayString);
            }
        });

        //TODO: add typeface here

        addImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.dialog_order_details, null);
                bottomSheetDialog.setContentView(v);

//                FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
//                if (bottomSheet != null) {
//                    BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
//                    behavior.setDraggable(false);
//                }
                //TODO:add typeface for popup dialog fields


                bottomSheetDialog.show();
            }
        });

        tableTabBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(0, true);
                selectTableTab();
//                orderTableFragment.updateTableView();
                viewPager.getAdapter().notifyDataSetChanged();

            }
        });

        listTabBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1, true);
                selectListTab();
                orderListFragment.updateData();
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
                            selectTableTab();
                        break;

                    case 1:
                            selectListTab();
                        break;

                    default:
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        return view;
    }

    public void updateFragment(String selectedDate){
        orderTableFragment.updateSelectedDate(selectedDate);
        orderListFragment.updateSelectedDate(selectedDate);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void selectTableTab(){
        tableTabBtn.setBackground(getContext().getResources().getDrawable(R.drawable.bg_tab_start_selected_shape_roundness));
        listTabBtn.setBackground(getContext().getResources().getDrawable(R.drawable.bg_tab_end_unselected_shape_roundness));
        tableTabBtn.setTextColor(getContext().getResources().getColor(R.color.white));
        listTabBtn.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void selectListTab(){
        tableTabBtn.setBackground(getContext().getResources().getDrawable(R.drawable.bg_tab_start_unselected_shape_roundness));
        listTabBtn.setBackground(getContext().getResources().getDrawable(R.drawable.bg_tab_end_selected_shape_roundness));
        tableTabBtn.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
        listTabBtn.setTextColor(getContext().getResources().getColor(R.color.white));
    }
}
