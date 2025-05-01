// XemLaiFragment.java
package com.example.javaopencv.ui;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.GradeResult;
import com.example.javaopencv.data.entity.Student;
import com.example.javaopencv.ui.adapter.GradeResultAdapter;
import com.example.javaopencv.viewmodel.XemLaiViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XemLaiFragment extends Fragment {

    public XemLaiFragment() {
        super(R.layout.fragment_xem_lai);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1) Khởi tạo RecyclerView và Adapter
        RecyclerView rv = view.findViewById(R.id.rvGradeResults);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        GradeResultAdapter adapter = new GradeResultAdapter();
        rv.setAdapter(adapter);

        // 2) Lấy ViewModel và examId từ arguments
        XemLaiViewModel vm = new ViewModelProvider(this)
                .get(XemLaiViewModel.class);
        int examId = requireArguments().getInt("examId", -1);

        // 3) Quan sát LiveData danh sách học sinh đã chấm → tạo studentMap
        vm.getStudentsForExam(examId).observe(getViewLifecycleOwner(), students -> {
            Map<String, Student> map = new HashMap<>();
            for (Student s : students) {
                map.put(s.getStudentNumber().trim(), s);
            }
            adapter.setStudentMap(map);
        });
        vm.getResultsForExam(examId).observe(getViewLifecycleOwner(), results -> {
            adapter.submitList(results);
        });

        // 5) Thiết lập sự kiện click để chuyển sang trang chi tiết kết quả
        adapter.setOnItemClickListener(item -> {
            Bundle args = new Bundle();
            args.putLong("gradeId", item.id);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_xemLaiFragment_to_gradeDetailFragment, args);
        });

        // 6) Thiết lập sự kiện long click để xóa kết quả chấm
        adapter.setOnItemLongClickListener(item -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Xóa kết quả chấm")
                    .setMessage("Bạn có chắc muốn xóa kết quả này không?")
                    .setNegativeButton("Hủy", null)
                    .setPositiveButton("Xóa", (d, w) -> {
                        vm.deleteResult(item);
                    })
                    .show();
        });
    }
}
