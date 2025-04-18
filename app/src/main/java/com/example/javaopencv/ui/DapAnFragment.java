package com.example.javaopencv.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;
import com.example.javaopencv.ui.adapter.ExamCodeAdapter;
import com.example.javaopencv.viewmodel.DapAnViewModel;

import java.util.ArrayList;
import java.util.List;

public class DapAnFragment extends Fragment {

    private ImageButton btnBack, btnCamera, btnAdd;
    private TextView tvNoExamCode;
    private RecyclerView recyclerView;
    private ExamCodeAdapter adapter;
    private DapAnViewModel viewModel;
    private int questionCount = 20;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dap_an, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ view
        btnBack       = view.findViewById(R.id.btn_back);
        btnCamera     = view.findViewById(R.id.btn_camera);
        btnAdd        = view.findViewById(R.id.btn_add);
        tvNoExamCode  = view.findViewById(R.id.tv_no_exam_code);
        recyclerView  = view.findViewById(R.id.recyclerView);

        // Thiết lập RecyclerView + Adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ExamCodeAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Lấy ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(DapAnViewModel.class);

        // Đọc examId và questionCount từ Bundle
        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey("questionCount")) {
                questionCount = args.getInt("questionCount");
                Log.d("DapAnFragment", "Question count: " + questionCount);
            }
            if (args.containsKey("examId")) {
                viewModel.setExamId(args.getInt("examId"));
            }
        }

        // Nút quay lại
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        // Nút thêm mã đề mới
        btnAdd.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putInt("examId", viewModel.getExamId());
            bundle.putInt("questionCount", questionCount);
            AddMaDeFragment fragment = new AddMaDeFragment();
            fragment.setArguments(bundle);
            FragmentTransaction tx = requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction();
            tx.replace(R.id.nav_host_fragment, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // TODO: btnCamera mở camera

        // Quan sát LiveData để cập nhật danh sách mã đề
        viewModel.getMaDeList().observe(getViewLifecycleOwner(), maDeItems -> {
            List<String> codes = new ArrayList<>();
            for (DapAnViewModel.MaDeItem item : maDeItems) {
                codes.add(item.code);
            }
            adapter.updateData(codes);
            updateUI(codes);
        });

        // Đăng ký click listener cho adapter
        adapter.setOnExamCodeClickListener(new ExamCodeAdapter.OnExamCodeClickListener() {
            @Override
            public void onExamCodeClick(int position, String maDe) {
                // Sửa mã đề
                Bundle bundle = new Bundle();
                bundle.putString("maDeToEdit", maDe);
                bundle.putInt("positionToEdit", position);
                bundle.putInt("questionCount", questionCount);
                List<String> oldAnswers = viewModel.getAnswerListByPosition(position);
                if (oldAnswers != null) {
                    bundle.putStringArrayList("oldAnswerList", new ArrayList<>(oldAnswers));
                }
                AddMaDeFragment fragment = new AddMaDeFragment();
                fragment.setArguments(bundle);
                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment, fragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onExamCodeLongClick(int position, String maDe) {
                // Xóa mã đề
                new AlertDialog.Builder(requireContext())
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có muốn xóa mã đề \"" + maDe + "\" không?")
                        .setPositiveButton("Có", (d, w) -> viewModel.removeMaDe(position))
                        .setNegativeButton("Không", null)
                        .show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Mỗi khi fragment quay lại, reload LiveData để cập nhật danh sách ngay
        int examId = viewModel.getExamId();
        if (examId >= 0) {
            viewModel.setExamId(examId);
        }
    }

    private void updateUI(List<String> codes) {
        if (codes.isEmpty()) {
            tvNoExamCode.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvNoExamCode.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
