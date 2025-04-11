package com.example.javaopencv.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.example.javaopencv.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AddMaDeFragment extends Fragment {

    private ViewPager2 viewPager;
    private MaDeViewPagerAdapter viewPagerAdapter;
    private TabLayout tabLayout;
    private ImageButton btnBack;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_ma_de, container, false);

        btnBack = view.findViewById(R.id.btn_back);
        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);

        // Xử lý nút back
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        // Khởi tạo adapter ViewPager2 (Bundle được chuyển từ ExamDetail/DapAn qua)
        viewPagerAdapter = new MaDeViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("MÃ ĐỀ");
                    } else {
                        tab.setText("ĐÁP ÁN");
                    }
                }
        ).attach();

        return view;
    }
}
