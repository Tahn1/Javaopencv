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

import java.util.Collections;

public class ClassFragment extends Fragment {
    private RecyclerView rvClasses;
    private FloatingActionButton fabAdd;
    private ClassAdapter adapter;
    private ClassViewModel viewModel;
    private int subjectId;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_class, container, false);
    }

    @Override public void onViewCreated(@NonNull View view,
                                        @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1) Lấy subjectId từ args (0 = tất cả môn)
        if (getArguments() != null) {
            subjectId = getArguments().getInt("subjectId", 0);
        }

        // 2) Ánh xạ RecyclerView & FAB
        rvClasses = view.findViewById(R.id.rvClasses);
        fabAdd    = view.findViewById(R.id.fab_add_class);
        rvClasses.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 3) Adapter với ClassWithCount
        adapter = new ClassAdapter(
                // onClick: mở danh sách sinh viên của lớp
                sc -> {
                    Bundle args = new Bundle();
                    args.putInt("classId", sc.getId());
                    Navigation.findNavController(view)
                            .navigate(R.id.action_classFragment_to_classDetailFragment, args);
                },
                // onLongClick: chọn Chỉnh sửa / Xóa
                cc -> {
                    String[] opts = {"Chỉnh sửa", "Xóa"};
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Chọn hành động")
                            .setItems(opts, (dlg, which) -> {
                                SchoolClass old = cc.getKlass();
                                if (which == 0) {
                                    // CHỈNH SỬA TÊN LỚP
                                    View dlgView = LayoutInflater.from(requireContext())
                                            .inflate(R.layout.dialog_add_class, null);
                                    EditText etName = dlgView.findViewById(R.id.etClassName);
                                    etName.setText(old.getName());
                                    new MaterialAlertDialogBuilder(requireContext())
                                            .setView(dlgView)
                                            .setNegativeButton("Hủy", null)
                                            .setPositiveButton("Lưu", (d, i) -> {
                                                String newName = etName.getText().toString().trim();
                                                if (TextUtils.isEmpty(newName)) {
                                                    Toast.makeText(requireContext(),
                                                            "Tên lớp không được để trống",
                                                            Toast.LENGTH_SHORT).show();
                                                    return;
                                                }
                                                // Cập nhật giữ nguyên id, subjectId, dateCreated
                                                SchoolClass updated = new SchoolClass(
                                                        old.getId(),
                                                        old.getSubjectId(),
                                                        newName,
                                                        old.getDateCreated()
                                                );
                                                viewModel.updateClass(updated);
                                            })
                                            .show();
                                } else {
                                    // XÓA lớp
                                    viewModel.deleteClass(old);
                                }
                            })
                            .show();
                }
        );
        rvClasses.setAdapter(adapter);

        // 4) ViewModel (truyền subjectId để lọc theo môn nếu cần)
        viewModel = new ViewModelProvider(
                this,
                new ClassViewModel.Factory(requireActivity().getApplication(), subjectId)
        ).get(ClassViewModel.class);

        // 5) Quan sát LiveData<ClassWithCount>
        viewModel.getClassesWithCount()
                .observe(getViewLifecycleOwner(),
                        list -> adapter.submitList(
                                list != null ? list : Collections.emptyList()
                        ));

        // 6) FAB thêm lớp mới
        fabAdd.setOnClickListener(v -> {
            View dlgView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.dialog_add_class, null);
            EditText etName = dlgView.findViewById(R.id.etClassName);

            new MaterialAlertDialogBuilder(requireContext())
                    .setView(dlgView)
                    .setNegativeButton("Hủy", (d, w) -> d.dismiss())
                    .setPositiveButton("Tạo", (d, w) -> {
                        String name = etName.getText().toString().trim();
                        if (TextUtils.isEmpty(name)) {
                            Toast.makeText(requireContext(),
                                    "Vui lòng nhập tên lớp",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        SchoolClass sc = new SchoolClass(
                                subjectId == 0 ? null : subjectId,
                                name
                        );
                        viewModel.insertClass(sc);
                    })
                    .show();
        });
    }
}
