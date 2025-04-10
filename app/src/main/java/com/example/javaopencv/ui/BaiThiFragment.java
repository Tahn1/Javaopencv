package com.example.javaopencv.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.javaopencv.R;
import com.example.javaopencv.ui.baithi.BaiThiAdapter;
import com.example.javaopencv.ui.baithi.BaiThiMenuItem;

import java.util.ArrayList;
import java.util.List;

public class BaiThiFragment extends Fragment {

    private ImageButton btnBack;
    private RecyclerView rvMenu;
    private BaiThiAdapter adapter;
    private List<BaiThiMenuItem> menuItems;

    // Lấy examId, title, phieu, soCau từ Bundle (nếu cần)
    private int examId;
    private String title;
    private String phieu;
    private int soCau;

    public BaiThiFragment() {
        // Bắt buộc để Fragment có constructor rỗng
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_baithi, container, false);

        // Ánh xạ
        btnBack = view.findViewById(R.id.btn_back);
        rvMenu = view.findViewById(R.id.rv_menu);

        // Lấy arguments nếu có (từ KiemTraFragment truyền sang)
        if (getArguments() != null) {
            examId = getArguments().getInt("examId", -1);
            title  = getArguments().getString("title", "");
            phieu  = getArguments().getString("phieu", "");
            soCau  = getArguments().getInt("soCau", 20);
        }

        // Thiết lập data cho menu
        menuItems = new ArrayList<>();
        menuItems.add(new BaiThiMenuItem(1, "Đáp án", R.drawable.ic_key,       "DapAn"));
        menuItems.add(new BaiThiMenuItem(2, "Chấm bài", R.drawable.ic_camera, "ChamBai"));
        menuItems.add(new BaiThiMenuItem(3, "Xem lại", R.drawable.ic_chatbox, "XemLai"));
        menuItems.add(new BaiThiMenuItem(4, "Thống kê", R.drawable.ic_bar_chart,       "ThongKe"));
        menuItems.add(new BaiThiMenuItem(5, "Thông tin", R.drawable.ic_info, "ThongTin"));

        // Adapter
        adapter = new BaiThiAdapter(menuItems, item -> {
            // Xử lý khi nhấn vào 1 mục
            // Tùy theo `item.screenName` ta điều hướng sang Fragment khác
            // Hoặc viết logic xử lý:
            switch (item.screenName) {
                case "DapAn":
                    // Chuyển sang Màn hình Đáp án
                    // ...
                    break;
                case "ChamBai":
                    // Chuyển sang Màn hình Chấm bài
                    // ...
                    break;
                case "XemLai":
                    // ...
                    break;
                case "ThongKe":
                    // ...
                    break;
                case "ThongTin":
                    // ...
                    break;
            }
        });

        // Thiết lập cho RecyclerView
        rvMenu.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMenu.setAdapter(adapter);

        // Nút Back
        btnBack.setOnClickListener(v -> {
            // Thoát fragment: popBackStack() hoặc requireActivity().onBackPressed()
            requireActivity().onBackPressed();
        });

        return view;
    }
}
