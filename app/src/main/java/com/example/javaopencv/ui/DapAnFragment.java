package com.example.javaopencv.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.ExamCodeEntry;
import com.example.javaopencv.viewmodel.ExamCodeViewModel;

public class DapAnFragment extends Fragment {

    private ImageButton btnBack, btnCamera, btnAdd;
    private TextView tvNoExamCode;
    private RecyclerView rvCodes;
    private ExamCodeAdapter codeAdapter;
    private ExamCodeViewModel examCodeViewModel;
    private int examId = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dap_an, container, false);

        // Ánh xạ view từ layout
        btnBack = view.findViewById(R.id.btn_back);
        btnCamera = view.findViewById(R.id.btn_camera);
        btnAdd = view.findViewById(R.id.btn_add);
        tvNoExamCode = view.findViewById(R.id.tv_no_exam_code);
        rvCodes = view.findViewById(R.id.rv_codes);

        // Xử lý nút Back: quay lại màn hình trước
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        // Xử lý nút Camera: hiển thị Toast (có thể thay bằng mở camera)
        btnCamera.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Mở camera", Toast.LENGTH_SHORT).show();
        });

        // Xử lý nút Add: tạo mới mã đề (nếu cần)
        btnAdd.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putInt("examId", examId);
            // Nếu tạo mới mã đề thì questionCount có thể là 0 hoặc truyền một giá trị mặc định
            bundle.putInt("questionCount", 0);
            NavHostFragment.findNavController(DapAnFragment.this)
                    .navigate(R.id.action_dapAnFragment_to_addMaDeFragment, bundle);
        });

        // Cài đặt RecyclerView và adapter
        rvCodes.setLayoutManager(new LinearLayoutManager(getContext()));
        codeAdapter = new ExamCodeAdapter();
        rvCodes.setAdapter(codeAdapter);

        // Lấy examId từ Bundle được truyền từ màn hình trước (ví dụ từ KiemTraFragment)
        if (getArguments() != null && getArguments().containsKey("examId")) {
            examId = getArguments().getInt("examId", 0);
        } else {
            Toast.makeText(getContext(), "Không nhận được examId", Toast.LENGTH_SHORT).show();
        }

        // Khởi tạo ViewModel và quan sát danh sách mã đề từ database
        examCodeViewModel = new ViewModelProvider(this).get(ExamCodeViewModel.class);
        examCodeViewModel.getExamCodeEntries(examId).observe(getViewLifecycleOwner(), examCodeEntries -> {
            if (examCodeEntries != null && !examCodeEntries.isEmpty()) {
                codeAdapter.setExamCodeList(examCodeEntries);
                tvNoExamCode.setVisibility(View.GONE);
            } else {
                tvNoExamCode.setVisibility(View.VISIBLE);
                tvNoExamCode.setText("Chưa có mã đề");
            }
        });

        // Khi người dùng ấn vào 1 item mã đề, chuyển sang AddMaDeFragment để chỉnh sửa
        codeAdapter.setOnItemClickListener((ExamCodeEntry entry) -> {
            Bundle bundle = new Bundle();
            bundle.putInt("examId", entry.examId);
            bundle.putInt("questionCount", entry.questionCount);
            bundle.putString("code", entry.code);
            bundle.putString("answers", entry.answers); // Nếu có đáp án đã lưu
            NavHostFragment.findNavController(DapAnFragment.this)
                    .navigate(R.id.action_dapAnFragment_to_addMaDeFragment, bundle);
        });

        return view;
    }
}
