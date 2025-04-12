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
import androidx.appcompat.app.AlertDialog; // <-- Đảm bảo import AlertDialog
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
    // Lưu questionCount (số câu) được truyền từ ExamDetailFragment
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

        btnBack = view.findViewById(R.id.btn_back);
        btnCamera = view.findViewById(R.id.btn_camera);
        btnAdd = view.findViewById(R.id.btn_add);
        tvNoExamCode = view.findViewById(R.id.tv_no_exam_code);
        recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ExamCodeAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(requireActivity()).get(DapAnViewModel.class);

        // Đọc Bundle và cập nhật questionCount cũng như examId
        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey("questionCount")) {
                questionCount = args.getInt("questionCount");
                Log.d("DapAnFragment", "Question count received: " + questionCount);
            }
            if (args.containsKey("examId")) {
                int examId = args.getInt("examId");
                viewModel.setExamId(examId);
            }
        }

        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        // Nút Add chuyển sang AddMaDeFragment để tạo mới mã đề, truyền examId và questionCount
        btnAdd.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            Bundle bundle = new Bundle();
            bundle.putInt("examId", viewModel.getExamId());
            bundle.putInt("questionCount", questionCount);
            AddMaDeFragment fragment = new AddMaDeFragment();
            fragment.setArguments(bundle);
            transaction.replace(R.id.nav_host_fragment, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        btnCamera.setOnClickListener(v -> {
            // TODO: mở Camera
        });

        // Quan sát LiveData để cập nhật danh sách mã đề
        viewModel.getMaDeList().observe(getViewLifecycleOwner(), maDeItemList -> {
            List<String> maDeStrings = new ArrayList<>();
            for (DapAnViewModel.MaDeItem item : maDeItemList) {
                maDeStrings.add(item.code);
            }
            adapter.updateData(maDeStrings);
            updateUI(maDeStrings);
        });

        // Khi nhấn vào mã đề để sửa, truyền thêm examId và questionCount
        adapter.setOnExamCodeClickListener(new ExamCodeAdapter.OnExamCodeClickListener() {
            @Override
            public void onExamCodeClick(int position, String maDe) {
                Bundle bundle = new Bundle();
                bundle.putString("maDeToEdit", maDe);
                bundle.putInt("positionToEdit", position);
                bundle.putInt("questionCount", questionCount);
                List<String> oldAnswerList = viewModel.getAnswerListByPosition(position);
                if (oldAnswerList != null) {
                    bundle.putStringArrayList("oldAnswerList", new ArrayList<>(oldAnswerList));
                }
                AddMaDeFragment fragment = new AddMaDeFragment();
                fragment.setArguments(bundle);
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.nav_host_fragment, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }

            @Override
            public void onExamCodeLongClick(int position, String maDe) {
                // Hiển thị dialog xác nhận xóa
                new AlertDialog.Builder(requireContext())
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có muốn xóa mã đề \"" + maDe + "\" không?")
                        .setPositiveButton("Có", (dialog, which) -> {
                            // User đồng ý => Xóa
                            viewModel.removeMaDe(position);
                        })
                        .setNegativeButton("Không", null)
                        .show();
            }
        });
    }

    private void updateUI(List<String> maDeList) {
        if (maDeList == null || maDeList.isEmpty()) {
            tvNoExamCode.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvNoExamCode.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
