package com.example.javaopencv.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.ClassWithCount;
import com.example.javaopencv.data.entity.SchoolClass;
import com.example.javaopencv.ui.adapter.ClassAdapter;
import com.example.javaopencv.viewmodel.ClassViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class ClassFragment extends Fragment {
    private RecyclerView rv;
    private FloatingActionButton fab;
    private ClassAdapter adapter;
    private ClassViewModel vm;
    private int subjectId;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf,
                             @Nullable ViewGroup container,
                             @Nullable Bundle saved) {
        return inf.inflate(R.layout.fragment_class, container, false);
    }

    @Override public void onViewCreated(@NonNull View view,
                                        @Nullable Bundle saved) {
        super.onViewCreated(view, saved);
        // Lấy subjectId (0 = tất cả môn)
        if (getArguments() != null) {
            subjectId = getArguments().getInt("subjectId", 0);
        }

        rv  = view.findViewById(R.id.rvClasses);
        fab = view.findViewById(R.id.fab_add_class);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ClassAdapter(
                sc -> {
                    // click thường: chuyển sang ClassDetail
                    Bundle args = new Bundle();
                    args.putInt("classId", sc.getId());
                    Navigation.findNavController(view)
                            .navigate(R.id.action_classFragment_to_classDetailFragment, args);
                },
                cc -> {
                    // long-click: sửa hoặc xóa
                    String[] opts = {"Chỉnh sửa", "Xóa"};
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Chọn hành động")
                            .setItems(opts, (dlg, which) -> {
                                if (which == 0) {
                                    // Chỉnh sửa tên lớp
                                    View dlgView = LayoutInflater.from(requireContext())
                                            .inflate(R.layout.dialog_add_class, null);
                                    EditText et = dlgView.findViewById(R.id.etClassName);
                                    et.setText(cc.klass.getName());
                                    new MaterialAlertDialogBuilder(requireContext())
                                            .setView(dlgView)
                                            .setNegativeButton("Hủy", null)
                                            .setPositiveButton("Lưu", (d,i)->{
                                                String newName = et.getText().toString().trim();
                                                if (newName.isEmpty()) {
                                                    Toast.makeText(requireContext(),
                                                            "Tên không được để trống",
                                                            Toast.LENGTH_SHORT).show();
                                                    return;
                                                }
                                                // Giữ nguyên id, subjectId, dateCreated
                                                SchoolClass u = new SchoolClass(
                                                        cc.klass.getSubjectId(),
                                                        newName
                                                );
                                                u.id = cc.klass.getId();
                                                u.dateCreated = cc.klass.getDateCreated();
                                                vm.updateClass(u);
                                            })
                                            .show();
                                } else {
                                    // Xóa
                                    vm.deleteClass(cc.klass);
                                }
                            })
                            .show();
                }
        );
        rv.setAdapter(adapter);

        vm = new ViewModelProvider(
                this,
                new ClassViewModel.Factory(requireActivity().getApplication(), subjectId)
        ).get(ClassViewModel.class);

        // Observe LiveData<ClassWithCount>
        vm.getClassesWithCount().observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
        });

        // FAB Tạo lớp mới
        fab.setOnClickListener(v -> {
            View dlg = LayoutInflater.from(requireContext())
                    .inflate(R.layout.dialog_add_class, null);
            EditText et = dlg.findViewById(R.id.etClassName);
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Tạo lớp mới")
                    .setView(dlg)
                    .setNegativeButton("Hủy", null)
                    .setPositiveButton("Tạo", (d,i)->{
                        String name = et.getText().toString().trim();
                        if (name.isEmpty()) {
                            Toast.makeText(requireContext(),
                                    "Vui lòng nhập tên lớp", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        SchoolClass sc = new SchoolClass(
                                subjectId == 0 ? null : subjectId,
                                name
                        );
                        vm.insertClass(sc);
                    })
                    .show();
        });
    }
}
