package com.example.javaopencv.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.example.javaopencv.R;

public class DapAnFragment extends Fragment {

    private ImageButton btnBack, btnCamera, btnAdd;
    private TextView tvNoExamCode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate layout từ fragment_dap_an.xml
        View view = inflater.inflate(R.layout.fragment_dap_an, container, false);

        btnBack = view.findViewById(R.id.btn_back);
        btnCamera = view.findViewById(R.id.btn_camera);
        btnAdd = view.findViewById(R.id.btn_add);
        tvNoExamCode = view.findViewById(R.id.tv_no_exam_code);

        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        btnCamera.setOnClickListener(v -> {
            // Xử lý mở camera nếu cần
        });
        // Khi ấn nút Add, điều hướng sang AddMaDeFragment và chuyển Bundle từ DapAnFragment
        btnAdd.setOnClickListener(v -> {
            Bundle bundle = getArguments(); // Bundle chứa questionCount được truyền từ trước
            NavHostFragment.findNavController(DapAnFragment.this)
                    .navigate(R.id.action_dapAnFragment_to_addMaDeFragment, bundle);
        });

        return view;
    }
}
