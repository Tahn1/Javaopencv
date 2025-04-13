package com.example.javaopencv.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.example.javaopencv.R;

public class GiayThiFragment extends Fragment {

    private Toolbar toolbar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate layout từ fragment_giay_thi.xml
        View view = inflater.inflate(R.layout.fragment_giay_thi, container, false);

        // Lấy Toolbar và thiết lập hành động cho nút back
        toolbar = view.findViewById(R.id.toolbar_giay_thi);
        toolbar.setNavigationOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigateUp();  // Quay lại fragment trước (ví dụ: KiemTraFragment)
        });

        return view;
    }
}
