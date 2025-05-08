package com.example.javaopencv.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.ClassWithCount;
import com.example.javaopencv.data.entity.SchoolClass;
import com.example.javaopencv.databinding.DialogAddClassBinding;
import com.example.javaopencv.databinding.FragmentClassBinding;
import com.example.javaopencv.ui.adapter.ClassAdapter;
import com.example.javaopencv.viewmodel.ClassViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ClassFragment extends Fragment {
    private FragmentClassBinding binding;
    private ClassAdapter adapter;
    private ClassViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentClassBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1) Thiết lập RecyclerView + Adapter
        adapter = new ClassAdapter(
                // onClick: mở chi tiết lớp
                sc -> {
                    Bundle args = new Bundle();
                    args.putInt("classId", sc.getId());
                    Navigation.findNavController(view)
                            .navigate(R.id.action_classFragment_to_classDetailFragment, args);
                },
                // onLongClick: sửa hoặc xóa
                cc -> {
                    SchoolClass sc = cc.getKlass();
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Chọn hành động")
                            .setItems(new String[]{"Chỉnh sửa", "Xóa"}, (dlg, which) -> {
                                if (which == 0) {
                                    showEditDialog(sc);
                                } else {
                                    viewModel.deleteClass(sc);
                                }
                            })
                            .show();
                }
        );
        binding.rvClasses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvClasses.setAdapter(adapter);

        // 2) Khởi tạo ViewModel
        viewModel = new ViewModelProvider(
                this,
                new ClassViewModel.Factory(requireActivity().getApplication())
        ).get(ClassViewModel.class);

        // 3) Quan sát dữ liệu và submitList
        viewModel.getClassesWithCount()
                .observe(getViewLifecycleOwner(), adapter::submitList);

        // 4) FAB thêm lớp mới
        binding.fabAddClass.setOnClickListener(v -> showAddDialog());
    }

    private void showAddDialog() {
        DialogAddClassBinding dlg = DialogAddClassBinding.inflate(getLayoutInflater());
        new MaterialAlertDialogBuilder(requireContext())
                .setView(dlg.getRoot())
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Tạo", (d, which) -> {
                    String name = dlg.etClassName.getText().toString().trim();
                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(requireContext(),
                                "Vui lòng nhập tên lớp",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String dateCreated = new SimpleDateFormat("d/M/yyyy", Locale.getDefault())
                            .format(new Date());
                    SchoolClass sc = new SchoolClass(0, name, dateCreated);
                    viewModel.insertClass(sc);
                })
                .show();
    }

    private void showEditDialog(SchoolClass sc) {
        DialogAddClassBinding dlg = DialogAddClassBinding.inflate(getLayoutInflater());
        dlg.etClassName.setText(sc.getName());
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Chỉnh sửa tên lớp")
                .setView(dlg.getRoot())
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", (d, which) -> {
                    String newName = dlg.etClassName.getText().toString().trim();
                    if (TextUtils.isEmpty(newName)) {
                        Toast.makeText(requireContext(),
                                "Tên lớp không được để trống",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    SchoolClass updated = new SchoolClass(
                            sc.getId(),
                            newName,
                            sc.getDateCreated()
                    );
                    viewModel.updateClass(updated);
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
