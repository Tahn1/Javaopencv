package com.example.javaopencv.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.GradeResult;
import com.example.javaopencv.ui.adapter.GradeResultAdapter;
import com.example.javaopencv.viewmodel.XemLaiViewModel;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

public class XemLaiFragment extends Fragment {

    public XemLaiFragment() {
        super(R.layout.fragment_xem_lai);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // 1) Toolbar với nút Back
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar_xem_lai);
        toolbar.setNavigationOnClickListener(v ->
                requireActivity().onBackPressed()
        );

        // 2) RecyclerView + Adapter
        RecyclerView rv = view.findViewById(R.id.rvGradeResults);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        GradeResultAdapter adapter = new GradeResultAdapter();
        rv.setAdapter(adapter);

        // 3) ViewModel và examId
        XemLaiViewModel vm = new ViewModelProvider(this).get(XemLaiViewModel.class);
        int examId = requireArguments().getInt("examId", -1);

        // 4) Click thông thường: xem chi tiết
        adapter.setOnItemClickListener(item -> {
            Bundle args = new Bundle();
            args.putLong("gradeId", item.id);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_xemLaiFragment_to_gradeDetailFragment, args);
        });

        // 5) Long‑press: xác nhận xóa
        adapter.setOnItemLongClickListener(item -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Xóa kết quả chấm")
                    .setMessage("Bạn có chắc muốn xóa kết quả này không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        vm.deleteResult(item);
                        Toast.makeText(requireContext(),
                                "Đã xóa kết quả", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        // 6) Quan sát LiveData và đổ dữ liệu
        vm.getResultsForExam(examId)
                .observe(getViewLifecycleOwner(), list -> {
                    adapter.submitList(list);
                });
    }
}
