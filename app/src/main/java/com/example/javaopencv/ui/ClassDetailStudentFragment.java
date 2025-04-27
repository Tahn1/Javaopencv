package com.example.javaopencv.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.Student;
import com.example.javaopencv.ui.adapter.StudentAdapter;
import com.example.javaopencv.viewmodel.StudentViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ClassDetailStudentFragment extends Fragment {
    private static final String ARG_CLASS_ID = "classId";
    private int classId;

    public static ClassDetailStudentFragment newInstance(int classId) {
        Bundle args = new Bundle();
        args.putInt(ARG_CLASS_ID, classId);
        ClassDetailStudentFragment fragment = new ClassDetailStudentFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_class_detail_student, container, false);
    }

    @Override public void onViewCreated(@NonNull View view,
                                        @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Lấy classId từ arguments
        if (getArguments() != null) {
            classId = getArguments().getInt(ARG_CLASS_ID, -1);
        }

        RecyclerView rv = view.findViewById(R.id.rvStudents);
        FloatingActionButton fab = view.findViewById(R.id.fab_add_student);

        // Cấu hình RecyclerView
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        StudentAdapter adapter = new StudentAdapter(student -> {
            // Xử lý click từng học sinh nếu cần
        });
        rv.setAdapter(adapter);

        // ViewModel và LiveData
        StudentViewModel vm = new ViewModelProvider(this).get(StudentViewModel.class);
        vm.getStudentsForClass(classId)
                .observe(getViewLifecycleOwner(), adapter::submitList);

        // FAB: show dialog thêm học sinh
        fab.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.dialog_add_student, null);
            EditText etName = dialogView.findViewById(R.id.etStudentName);
            EditText etSbd  = dialogView.findViewById(R.id.etStudentSbd);

            new MaterialAlertDialogBuilder(requireContext())
                    .setView(dialogView)
                    .setNegativeButton("Hủy", (d, which) -> d.dismiss())
                    .setPositiveButton("Thêm", (d, which) -> {
                        String name = etName.getText().toString().trim();
                        String sbd  = etSbd.getText().toString().trim();

                        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(sbd)) {
                            Toast.makeText(requireContext(),
                                    "Vui lòng nhập đầy đủ tên và SBD", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (sbd.length() != 6 || !TextUtils.isDigitsOnly(sbd)) {
                            Toast.makeText(requireContext(),
                                    "SBD phải gồm 6 chữ số", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Tạo entity Student và insert
                        Student student = new Student(name, sbd, classId == -1 ? null : classId);
                        vm.insertStudent(student);
                    })
                    .show();
        });
    }
}
