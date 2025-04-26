package com.example.javaopencv.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.MainActivity;
import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.Exam;
import com.example.javaopencv.data.entity.SchoolClass;
import com.example.javaopencv.ui.adapter.ExamAdapter;
import com.example.javaopencv.viewmodel.ClassViewModel;
import com.example.javaopencv.viewmodel.KiemTraViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class KiemTraFragment extends Fragment
        implements ExamAdapter.OnExamItemClickListener,
        ExamAdapter.OnExamItemLongClickListener {

    private KiemTraViewModel viewModel;
    private RecyclerView rvExams;
    private ExamAdapter examAdapter;
    private FloatingActionButton fabAdd;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Cho biết fragment này sẽ thêm menu vào Toolbar
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_kiem_tra, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1) Lấy Toolbar từ Activity và inflate menu riêng
        Toolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.menu_kiem_tra);
        toolbar.setTitle("Kiểm Tra");
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));

        // 2) Bắt sự kiện menu item
        // 2.1) SearchView
        MenuItem searchItem = toolbar.getMenu().findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Tìm bài thi...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                examAdapter.filter(newText);
                return true;
            }
        });

        // 2.2) Filter
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_filter) {
                showSortDialog();
                return true;
            }
            return false;
        });

        // 3) RecyclerView + Adapter
        rvExams = view.findViewById(R.id.rv_exams);
        rvExams.setLayoutManager(new LinearLayoutManager(requireContext()));
        examAdapter = new ExamAdapter();
        examAdapter.setListener(this);
        examAdapter.setLongClickListener(this);
        rvExams.setAdapter(examAdapter);

        // 4) ViewModel + LiveData
        viewModel = new ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())
        ).get(KiemTraViewModel.class);

        viewModel.getExams().observe(getViewLifecycleOwner(), exams -> {
            Log.d("KiemTraFragment", "LiveData emit, size = " + (exams == null ? 0 : exams.size()));
            examAdapter.setExamList(exams);
        });

        // 5) FAB thêm bài thi
        fabAdd = view.findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v -> showCreateExamDialog());
    }

    private void showSortDialog() {
        final String[] options = {
                "Tên (A-Z)", "Tên (Z-A)", "Ngày (Tăng dần)", "Ngày (Giảm dần)"
        };
        final String[] codes = {
                "name_asc", "name_desc", "date_asc", "date_desc"
        };
        new AlertDialog.Builder(requireContext())
                .setTitle("Sắp xếp bài thi")
                .setItems(options, (dlg, which) ->
                        examAdapter.sortByOption(codes[which])
                )
                .setNegativeButton("HỦY", null)
                .show();
    }

    private void showCreateExamDialog() {
        View dlgView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_create_exam, null);

        // Spinner chọn lớp
        Spinner spClass = dlgView.findViewById(R.id.spinner_exam_class);
        ArrayAdapter<String> classNames = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new ArrayList<>()
        );
        classNames.add("None");
        spClass.setAdapter(classNames);

        // Load danh sách lớp qua ClassViewModel
        ClassViewModel classVm = new ViewModelProvider(
                this,
                new ClassViewModel.Factory(
                        requireActivity().getApplication(), 0 /*subjectId=0*/
                )
        ).get(ClassViewModel.class);

        classVm.getClasses().observe(getViewLifecycleOwner(),
                classes -> {
                    classNames.clear();
                    classNames.add("None");
                    for (SchoolClass c : classes) {
                        classNames.add(c.name);
                    }
                    classNames.notifyDataSetChanged();
                }
        );

        // Các View còn lại
        Spinner spPhieu = dlgView.findViewById(R.id.spinner_exam_phieu);
        EditText etTitle = dlgView.findViewById(R.id.et_exam_title);
        EditText etSoCau = dlgView.findViewById(R.id.et_exam_socau);

        ArrayAdapter<CharSequence> phieuAdapter =
                ArrayAdapter.createFromResource(
                        requireContext(),
                        R.array.exam_phieu_array,
                        android.R.layout.simple_spinner_dropdown_item
                );
        spPhieu.setAdapter(phieuAdapter);
        spPhieu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                int max = spPhieu.getSelectedItem().toString().equals("Phiếu 20") ? 20 : 60;
                etSoCau.setFilters(new InputFilter[]{ new InputFilterMinMax(1, max) });
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        new AlertDialog.Builder(requireContext())
                .setTitle("Tạo bài mới")
                .setView(dlgView)
                .setPositiveButton("TẠO", (dlg, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String phieu = spPhieu.getSelectedItem().toString();
                    String soCauStr = etSoCau.getText().toString().trim();

                    if (TextUtils.isEmpty(title)) {
                        Toast.makeText(requireContext(),
                                "Tên bài không được để trống",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (TextUtils.isEmpty(soCauStr)) {
                        Toast.makeText(requireContext(),
                                "Vui lòng nhập số câu",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int soCau = Integer.parseInt(soCauStr);
                    long now = System.currentTimeMillis();
                    String date = android.text.format.DateFormat
                            .format("dd-MM-yyyy", now).toString();

                    // Xác định classId
                    int sel = spClass.getSelectedItemPosition();
                    int classId = 0; // default None
                    if (sel > 0) classId = sel; // assumes lớp với id tương ứng index; hoặc bạn map đúng id

                    Exam exam = new Exam(classId, title, phieu, soCau, date);
                    viewModel.insertExam(exam);

                })
                .setNegativeButton("HỦY", null)
                .show();
    }

    @Override
    public void onExamItemClick(Exam exam) {
        Bundle args = new Bundle();
        args.putInt("questionCount", exam.getSoCau());
        args.putInt("examId", exam.getId());
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_kiemTraFragment_to_examDetailFragment, args);
    }

    // onLongClick Exam item
    @Override
    public void onExamItemLongClick(Exam exam) {
        final String[] opts = {"Sửa", "Xóa", "Sao chép"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn hành động")
                .setItems(opts, (dlg, which) -> {
                    switch (which) {
                        case 0: showEditExamDialog(exam); break;
                        case 1:
                            viewModel.deleteExam(exam);
                            Toast.makeText(requireContext(),
                                    "Đã xóa bài", Toast.LENGTH_SHORT
                            ).show();
                            break;
                        case 2:
                            int oldClassId = exam.getClassId();
                            Exam copiedExam = new Exam(
                                    oldClassId,
                                    exam.getTitle() + " (Copy)",
                                    exam.getPhieu(),
                                    exam.getSoCau(),
                                    exam.getDate()
                            );
                            viewModel.insertExam(copiedExam);
                            Toast.makeText(requireContext(),
                                    "Đã sao chép bài", Toast.LENGTH_SHORT
                            ).show();
                            break;
                    }
                })
                .setNegativeButton("HỦY", null)
                .show();
    }

    // dialog Sửa
    private void showEditExamDialog(Exam exam) {
        LayoutInflater inf = LayoutInflater.from(requireContext());
        View dlgView = inf.inflate(R.layout.dialog_create_exam, null);

        EditText etTitle = dlgView.findViewById(R.id.et_exam_title);
        Spinner spPhieu = dlgView.findViewById(R.id.spinner_exam_phieu);
        EditText etSo = dlgView.findViewById(R.id.et_exam_socau);

        etTitle.setText(exam.title);
        ArrayAdapter<CharSequence> adp = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.exam_phieu_array,
                android.R.layout.simple_spinner_dropdown_item
        );
        spPhieu.setAdapter(adp);
        spPhieu.setSelection(adp.getPosition(exam.phieu));
        spPhieu.setEnabled(false);
        etSo.setText(String.valueOf(exam.soCau));
        etSo.setEnabled(false);

        new AlertDialog.Builder(requireContext())
                .setTitle("Sửa tên bài thi")
                .setView(dlgView)
                .setPositiveButton("LƯU", (dlg, which) -> {
                    String newTitle = etTitle.getText().toString().trim();
                    if (TextUtils.isEmpty(newTitle)) {
                        Toast.makeText(requireContext(),
                                "Tên bài không được để trống",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }
                    Exam up = new Exam(
                            exam.getId(),         // giữ nguyên ID để Room biết cập nhật row nào
                            exam.getClassId(),    // giữ nguyên classI
                            newTitle,
                            exam.getPhieu(),
                            exam.getSoCau(),
                            exam.getDate()
                    );
                    viewModel.updateExam(up);
                    Toast.makeText(requireContext(),
                            "Đã cập nhật", Toast.LENGTH_SHORT
                    ).show();
                })
                .setNegativeButton("HỦY", null)
                .show();
    }

    // input filter chỉ cho số trong khoảng
    public static class InputFilterMinMax implements InputFilter {
        private final int min, max;
        public InputFilterMinMax(int min, int max) {
            this.min = min; this.max = max;
        }
        @Override public CharSequence filter(
                CharSequence src, int start, int end,
                Spanned   dest,  int dstart,  int dend
        ) {
            try {
                String result = dest.toString()
                        .substring(0, dstart)
                        + src.toString()
                        + dest.toString()
                        .substring(dend);
                int val = Integer.parseInt(result);
                if (val >= min && val <= max) return null;
            } catch (NumberFormatException e) { }
            return "";
        }
    }
}
