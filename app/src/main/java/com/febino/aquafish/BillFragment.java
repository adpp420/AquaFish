package com.febino.aquafish;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.febino.aquafish.R;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class BillFragment extends Fragment {
    private Button generateTabBtn,listTabBtn;

    private BillListFragment billListFragment;
    private BillGenerateFragment billGenerateFragment;

    ViewPager viewPager;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

    }


    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstance) {
        SpannableString s = new SpannableString(getResources().getString(R.string.bill).toUpperCase());
        s.setSpan(new RelativeSizeSpan(0.8f), 0,s.length(), 0);
        s.setSpan(new TypefaceSpan(getContext(), "unicode.futurab.ttf"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        View view = layoutInflater.inflate(R.layout.fragment_bill, container, false);
        getActivity().setTitle(s);

        viewPager = view.findViewById(R.id.bill_view_pager);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        billGenerateFragment = new BillGenerateFragment();
        billListFragment = new BillListFragment();

        viewPagerAdapter.addFragment(billGenerateFragment,"Bill Generate Fragment");
        viewPagerAdapter.addFragment(billListFragment,"Bill List Fragement");
        viewPager.setAdapter(viewPagerAdapter);

        generateTabBtn = view.findViewById(R.id.bill_generate_tab_btn);
        listTabBtn = view.findViewById(R.id.bill_list_tab_btn);


        //TODO: add typeface here





        generateTabBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(0, true);
                selectGenerateTab();

            }
        });

        listTabBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1, true);
                selectListTab();
                billListFragment.updateListFragment();
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
                        selectGenerateTab();
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

    public void changeBillFragmentView(){
        if (viewPager != null) {
            viewPager.setCurrentItem(0, true);
            selectGenerateTab();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void selectGenerateTab(){
        generateTabBtn.setBackground(getContext().getResources().getDrawable(R.drawable.bg_tab_start_selected_shape_roundness));
        listTabBtn.setBackground(getContext().getResources().getDrawable(R.drawable.bg_tab_end_unselected_shape_roundness));
        generateTabBtn.setTextColor(getContext().getResources().getColor(R.color.white));
        listTabBtn.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void selectListTab(){
        generateTabBtn.setBackground(getContext().getResources().getDrawable(R.drawable.bg_tab_start_unselected_shape_roundness));
        listTabBtn.setBackground(getContext().getResources().getDrawable(R.drawable.bg_tab_end_selected_shape_roundness));
        generateTabBtn.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
        listTabBtn.setTextColor(getContext().getResources().getColor(R.color.white));
    }
}
