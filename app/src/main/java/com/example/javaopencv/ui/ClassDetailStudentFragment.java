package com.example.javaopencv.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
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
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment hiển thị danh sách học sinh, sử dụng App Bar của Activity để
 * hiển thị Search & Filter. Hỗ trợ tìm kiếm, sắp xếp, thêm, sửa, xóa.
 */
public class ClassDetailStudentFragment extends Fragment {
    private static final String ARG_CLASS_ID = "classId";
    private int classId;
    private StudentViewModel vm;
    private StudentAdapter adapter;

    public static ClassDetailStudentFragment newInstance(int classId) {
        Bundle args = new Bundle();
        args.putInt(ARG_CLASS_ID, classId);
        ClassDetailStudentFragment fragment = new ClassDetailStudentFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Cho phép fragment quản lý menu trên App Bar
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_class_detail_student, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Lấy classId từ arguments
        if (getArguments() != null) {
            classId = getArguments().getInt(ARG_CLASS_ID, -1);
        }

        // Khởi tạo ViewModel
        vm = new ViewModelProvider(this).get(StudentViewModel.class);

        // Thiết lập RecyclerView và Adapter
        RecyclerView rv = view.findViewById(R.id.rvStudents);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new StudentAdapter(
                student -> {
                    // Xử lý click nếu cần
                },
                student -> showEditDeleteDialog(student)
        );
        rv.setAdapter(adapter);

        // Quan sát LiveData và submit list
        vm.getStudentsForClass(classId)
                .observe(getViewLifecycleOwner(), adapter::submitList);

        // Nút thêm học sinh
        FloatingActionButton fab = view.findViewById(R.id.fab_add_student);
        fab.setOnClickListener(v -> showAddOrEditDialog(null));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // Inflate menu lên App Bar của Activity
        inflater.inflate(R.menu.menu_class_detail_student, menu);

        // Cấu hình SearchView
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Tìm kiếm học sinh...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });
        searchView.setOnCloseListener(() -> {
            adapter.filter("");
            return false;
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Xử lý Filter
        if (item.getItemId() == R.id.action_filter) {
            CharSequence[] options = {"Theo Chữ A→Z", "Theo Chữ Z→A"};
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Chọn chiều sắp xếp")
                    .setItems(options, (dialog, which) -> {
                        boolean asc = (which == 0);
                        adapter.setSortOrder(asc);
                        Toast.makeText(requireContext(),
                                asc ? "Sắp xếp A→Z" : "Sắp xếp Z→A",
                                Toast.LENGTH_SHORT).show();
                    })
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Hiện dialog thêm hoặc chỉnh sửa học sinh
     */
    private void showAddOrEditDialog(@Nullable Student student) {
        boolean isEdit = student != null;
        View dlgView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_student, null);
        TextInputEditText etName = dlgView.findViewById(R.id.etStudentName);
        TextInputEditText etSbd = dlgView.findViewById(R.id.etStudentSbd);

        if (isEdit) {
            etName.setText(student.getName());
            etSbd.setText(student.getStudentNumber());
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(isEdit ? "Chỉnh sửa học sinh" : "Thêm học sinh")
                .setView(dlgView)
                .setNegativeButton("Hủy", null)
                .setPositiveButton(isEdit ? "Lưu" : "Thêm", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String sbd = etSbd.getText().toString().trim();
                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(sbd)) {
                        Toast.makeText(requireContext(),
                                "Nhập đầy đủ tên và SBD", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (sbd.length() != 6 || !TextUtils.isDigitsOnly(sbd)) {
                        Toast.makeText(requireContext(),
                                "SBD phải gồm đúng 6 chữ số", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (isEdit) {
                        Student updated = new Student(
                                student.getId(), name, sbd,
                                student.getClassId(), student.getDateCreated());
                        vm.updateStudent(updated);
                    } else {
                        String currentDate = new SimpleDateFormat(
                                "d/M/yyyy", new Locale("vi")).format(new Date());
                        Student newStd = new Student(name, sbd,
                                classId == -1 ? null : classId, currentDate);
                        vm.insertStudent(newStd);
                    }
                })
                .show();
    }

    /**
     * Hiện menu Edit/Delete khi long-press
     */
    private void showEditDeleteDialog(Student student) {
        String[] opts = {"Chỉnh sửa", "Xóa"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Chọn hành động")
                .setItems(opts, (dlg, which) -> {
                    if (which == 0) {
                        showAddOrEditDialog(student);
                    } else {
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Xóa học sinh")
                                .setMessage("Bạn có chắc muốn xóa học sinh này không?")
                                .setNegativeButton("Hủy", null)
                                .setPositiveButton("Xóa", (d2, w2) -> {
                                    vm.deleteStudent(student);
                                    Toast.makeText(requireContext(),
                                            "Đã xóa", Toast.LENGTH_SHORT).show();
                                }).show();
                    }
                }).show();
    }
}