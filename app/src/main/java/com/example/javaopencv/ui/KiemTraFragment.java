package com.example.javaopencv.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.example.javaopencv.ui.adapter.ExamAdapter;
import com.example.javaopencv.viewmodel.KiemTraViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class KiemTraFragment extends Fragment implements ExamAdapter.OnExamItemClickListener,
        ExamAdapter.OnExamItemLongClickListener {

    private KiemTraViewModel viewModel;
    private RecyclerView rvExams;
    private ExamAdapter examAdapter;
    private FloatingActionButton fabAdd;
    private ImageButton btnMenu, btnSearch, btnFilter;
    private EditText etSearch; // Thanh tìm kiếm ở header

    public KiemTraFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout "fragment_kiem_tra.xml"
        View view = inflater.inflate(R.layout.fragment_kiem_tra, container, false);

        // Bind các View từ layout (đảm bảo rằng các id này khớp với file XML của bạn)
        btnMenu = view.findViewById(R.id.btn_menu);
        btnSearch = view.findViewById(R.id.btn_search);
        btnFilter = view.findViewById(R.id.btn_filter);
        rvExams = view.findViewById(R.id.rv_exams);
        fabAdd = view.findViewById(R.id.fab_add);
        etSearch = view.findViewById(R.id.et_search);

        // Setup RecyclerView và Adapter
        rvExams.setLayoutManager(new LinearLayoutManager(getContext()));
        examAdapter = new ExamAdapter();
        examAdapter.setListener(this);
        examAdapter.setLongClickListener(this);
        rvExams.setAdapter(examAdapter);

        // Lấy ViewModel và quan sát danh sách Exam từ database
        viewModel = new ViewModelProvider(this).get(KiemTraViewModel.class);
        viewModel.getExams().observe(getViewLifecycleOwner(), new Observer<List<Exam>>() {
            @Override
            public void onChanged(List<Exam> examList) {
                examAdapter.setExamList(examList);
            }
        });

        // Sự kiện mở Drawer
        btnMenu.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                DrawerLayout drawer = getActivity().findViewById(R.id.drawer_layout);
                if (drawer != null)
                    drawer.openDrawer(androidx.core.view.GravityCompat.START);
            }
        });

        // Toggling thanh tìm kiếm khi nhấn vào nút Search
        btnSearch.setOnClickListener(v -> {
            if (etSearch.getVisibility() == View.VISIBLE) {
                etSearch.setText("");
                etSearch.setVisibility(View.GONE);
            } else {
                etSearch.setVisibility(View.VISIBLE);
                etSearch.requestFocus();
            }
        });

        // Nút Filter: mở dialog sắp xếp
        btnFilter.setOnClickListener(v -> showSortDialog());

        // Lắng nghe thay đổi text của etSearch để lọc danh sách
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không cần xử lý
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                examAdapter.filter(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {
                // Không cần xử lý
            }
        });

        // FAB mở dialog tạo bài thi mới
        fabAdd.setOnClickListener(v -> showCreateExamDialog());

        return view;
    }

    /**
     * Hiển thị dialog tạo bài thi mới.
     * Yêu cầu file XML "dialog_create_exam.xml" phải có các View với các id:
     * - et_exam_title (EditText nhập tên bài)
     * - spinner_exam_phieu (Spinner cho lựa chọn "Phiếu 20" hoặc "Phiếu 60")
     * - et_exam_socau (EditText nhập số câu; chỉ nhập số và có giới hạn theo lựa chọn)
     */
    private void showCreateExamDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_create_exam, null);

        final EditText etTitle = dialogView.findViewById(R.id.et_exam_title);
        final Spinner spinnerPhieu = dialogView.findViewById(R.id.spinner_exam_phieu);
        final EditText etSoCau = dialogView.findViewById(R.id.et_exam_socau);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.exam_phieu_array, android.R.layout.simple_spinner_dropdown_item);
        spinnerPhieu.setAdapter(spinnerAdapter);

        spinnerPhieu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = spinnerPhieu.getSelectedItem().toString();
                int maxQuestions = selected.equals("Phiếu 20") ? 20 : 60;
                etSoCau.setFilters(new InputFilter[]{new InputFilterMinMax(1, maxQuestions)});
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Tạo bài mới")
                .setView(dialogView)
                .setPositiveButton("TẠO", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String phieu = spinnerPhieu.getSelectedItem().toString().trim();
                    String soCauStr = etSoCau.getText().toString().trim();

                    if (TextUtils.isEmpty(title)) {
                        Toast.makeText(getContext(), "Tên bài không được để trống", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (TextUtils.isEmpty(soCauStr)) {
                        Toast.makeText(getContext(), "Vui lòng nhập số câu", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int soCau = Integer.parseInt(soCauStr);
                    long idExam = System.currentTimeMillis();
                    String date = android.text.format.DateFormat.format("dd-MM-yyyy", new java.util.Date()).toString();

                    Exam newExam = new Exam((int) idExam, title, phieu, soCau, date);
                    viewModel.addExam(newExam);
                })
                .setNegativeButton("HỦY", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    /**
     * Hiển thị dialog sắp xếp bài thi theo 4 tùy chọn:
     * "Tên (A-Z)", "Tên (Z-A)", "Ngày (Tăng dần)", "Ngày (Giảm dần)".
     */
    private void showSortDialog() {
        final String[] sortOptions = {"Tên (A-Z)", "Tên (Z-A)", "Ngày (Tăng dần)", "Ngày (Giảm dần)"};
        final String[] sortCodes = {"name_asc", "name_desc", "date_asc", "date_desc"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Sắp xếp bài thi")
                .setItems(sortOptions, (dialog, which) -> {
                    String option = sortCodes[which];
                    examAdapter.sortByOption(option);
                })
                .setNegativeButton("HỦY", null);
        builder.create().show();
    }

    @Override
    public void onExamItemClick(Exam exam) {
        int questionCount = exam.getSoCau();
        Bundle bundle = new Bundle();
        bundle.putInt("questionCount", questionCount);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_kiemTraFragment_to_examDetailFragment, bundle);




    }
    /**
     * Xử lý long press trên một bài thi, hiển thị dialog lựa chọn: Sửa, Xóa, Sao chép.
     */
    @Override
    public void onExamItemLongClick(Exam exam) {
        final String[] options = {"Sửa", "Xóa", "Sao chép"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Chọn hành động")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Sửa
                            showEditExamDialog(exam);
                            break;
                        case 1: // Xóa
                            viewModel.deleteExam(exam);
                            Toast.makeText(getContext(), "Đã xóa bài", Toast.LENGTH_SHORT).show();
                            break;
                        case 2: // Sao chép
                            Exam copiedExam = new Exam(
                                    (int) System.currentTimeMillis(),
                                    exam.title + " (Copy)",
                                    exam.phieu,
                                    exam.soCau,
                                    exam.date
                            );
                            viewModel.addExam(copiedExam);
                            Toast.makeText(getContext(), "Đã sao chép bài", Toast.LENGTH_SHORT).show();
                            break;
                    }
                })
                .setNegativeButton("HỦY", null);
        builder.create().show();
    }

    /**
     * Hiển thị dialog để sửa bài thi đã chọn.
     */
    private void showEditExamDialog(Exam exam) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_create_exam, null);

        // Lấy tham chiếu các view và điền giá trị hiện tại của bài thi
        final EditText etTitle = dialogView.findViewById(R.id.et_exam_title);
        final Spinner spinnerPhieu = dialogView.findViewById(R.id.spinner_exam_phieu);
        final EditText etSoCau = dialogView.findViewById(R.id.et_exam_socau);

        etTitle.setText(exam.title);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.exam_phieu_array, android.R.layout.simple_spinner_dropdown_item);
        spinnerPhieu.setAdapter(spinnerAdapter);
        // Chọn mục phù hợp với exam.phieu
        if (exam.phieu.equals("Phiếu 20") || exam.phieu.equals("Phiếu 60")) {
            int pos = spinnerAdapter.getPosition(exam.phieu);
            spinnerPhieu.setSelection(pos);
        }

        etSoCau.setText(String.valueOf(exam.soCau));

        // Cập nhật InputFilter dựa theo lựa chọn của Spinner
        spinnerPhieu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = spinnerPhieu.getSelectedItem().toString();
                int maxQuestions = selected.equals("Phiếu 20") ? 20 : 60;
                etSoCau.setFilters(new InputFilter[]{ new InputFilterMinMax(1, maxQuestions) });
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Sửa bài thi")
                .setView(dialogView)
                .setPositiveButton("LƯU", (dialog, which) -> {
                    String newTitle = etTitle.getText().toString().trim();
                    String newPhieu = spinnerPhieu.getSelectedItem().toString().trim();
                    String soCauStr = etSoCau.getText().toString().trim();

                    if (TextUtils.isEmpty(newTitle)) {
                        Toast.makeText(getContext(), "Tên bài không được để trống", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (TextUtils.isEmpty(soCauStr)) {
                        Toast.makeText(getContext(), "Vui lòng nhập số câu", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int newSoCau = Integer.parseInt(soCauStr);
                    String date = android.text.format.DateFormat.format("dd-MM-yyyy", new java.util.Date()).toString();

                    // Tạo object Exam mới với các giá trị cập nhật (giữ nguyên id của bài thi)
                    Exam updatedExam = new Exam(exam.id, newTitle, newPhieu, newSoCau, date);
                    viewModel.updateExam(updatedExam);
                    Toast.makeText(getContext(), "Đã cập nhật bài thi", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("HỦY", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    /**
     * Helper class để giới hạn giá trị nhập cho EditText từ min đến max.
     * Phương thức filter được implement theo interface InputFilter.
     */
    public static class InputFilterMinMax implements InputFilter {
        private final int min, max;

        public InputFilterMinMax(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            try {
                String newVal = dest.toString().substring(0, dstart)
                        + source.toString()
                        + dest.toString().substring(dend);
                int input = Integer.parseInt(newVal);
                if (input >= min && input <= max)
                    return null;
            } catch (NumberFormatException nfe) {
                // Nếu không phải số, không cho nhập
            }
            return "";
        }
    }
}
