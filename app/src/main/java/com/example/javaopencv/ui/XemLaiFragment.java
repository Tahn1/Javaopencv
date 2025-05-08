package com.example.javaopencv.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.Exam;
import com.example.javaopencv.data.entity.GradeResult;
import com.example.javaopencv.data.entity.Student;
import com.example.javaopencv.ui.adapter.GradeResultAdapter;
import com.example.javaopencv.viewmodel.XemLaiViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class XemLaiFragment extends Fragment {
    private XemLaiViewModel vm;
    private GradeResultAdapter adapter;
    private int examId;
    private Integer classId;
    private final Map<String, Student> studentMap = new HashMap<>();

    private List<GradeResult> fullResults = new ArrayList<>();
    private int sortMode = 0; // 0=default,1=SBD,2=Ma de

    private SwipeRefreshLayout swipeRefresh;

    public XemLaiFragment() {
        super(R.layout.fragment_xem_lai);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            examId = getArguments().getInt("examId", -1);
        }
        classId = null;
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateUp();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Toolbar menu
        MenuHost host = requireActivity();
        host.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
                inflater.inflate(R.menu.menu_xem_lai, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == android.R.id.home) {
                    navigateUp();
                    return true;
                } else if (id == R.id.action_delete_all) {
                    confirmDeleteAll();
                    return true;
                } else if (id == R.id.action_sort) {
                    showSortDialog();
                    return true;
                } else if (id == R.id.action_export) {
                    exportCsv();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        swipeRefresh.setOnRefreshListener(this::refreshData);

        RecyclerView rv = view.findViewById(R.id.rvGradeResults);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new GradeResultAdapter();
        rv.setAdapter(adapter);

        vm = new ViewModelProvider(this).get(XemLaiViewModel.class);

        // Auto-update on DB change
        vm.getResultsForExam(examId).observe(getViewLifecycleOwner(), results -> {
            swipeRefresh.setRefreshing(false);
            fullResults = results != null ? results : new ArrayList<>();
            applySort();
        });

        // Map student numbers to names
        vm.getClassIdForExam(examId).observe(getViewLifecycleOwner(), cid -> {
            classId = cid;
            if (cid != null) {
                vm.getStudentsForClass(cid).observe(getViewLifecycleOwner(), list -> {
                    studentMap.clear();
                    for (Student s : list) {
                        if (s.getStudentNumber() != null) {
                            studentMap.put(s.getStudentNumber().trim(), s);
                        }
                    }
                    adapter.setStudentMap(studentMap);
                });
            } else {
                studentMap.clear();
                adapter.setStudentMap(studentMap);
            }
        });

        adapter.setOnItemClickListener(item -> {
            Bundle args = new Bundle();
            args.putLong("gradeId", item.id);
            if (classId != null) args.putInt("classId", classId);
            navigateToDetail(args);
        });
        adapter.setOnItemLongClickListener(this::confirmDelete);

        swipeRefresh.setRefreshing(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        swipeRefresh.setRefreshing(true);
        refreshData();
    }

    private void refreshData() {
        new Thread(() -> {
            List<GradeResult> results = vm.getResultsListSync(examId);
            requireActivity().runOnUiThread(() -> {
                swipeRefresh.setRefreshing(false);
                fullResults = results != null ? results : new ArrayList<>();
                applySort();
            });
        }).start();
    }

    private void applySort() {
        List<GradeResult> sorted = new ArrayList<>(fullResults);
        if (sortMode == 1) {
            sorted.sort(Comparator.comparing(r -> r.sbd != null ? r.sbd.trim() : ""));
        } else if (sortMode == 2) {
            sorted.sort(Comparator.comparing(r -> r.maDe != null ? r.maDe.trim() : ""));
        }
        adapter.submitList(sorted);
    }

    private void confirmDelete(GradeResult item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa kết quả chấm")
                .setMessage("Bạn có chắc muốn xóa kết quả này không?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (d, w) -> vm.deleteResult(item))
                .show();
    }

    private void confirmDeleteAll() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa tất cả bài thi")
                .setMessage("Bạn có chắc muốn xóa toàn bộ kết quả?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (d, w) -> vm.deleteAllResultsForExam(examId))
                .show();
    }

    private void showSortDialog() {
        String[] options = {"Sắp theo SBD ↑", "Sắp theo Mã Đề ↑"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Chọn cách sắp xếp")
                .setSingleChoiceItems(options, sortMode - 1, (dlg, which) -> sortMode = which + 1)
                .setPositiveButton("OK", (d, w) -> applySort())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void exportCsv() {
        new Thread(() -> {
            Exam exam = vm.getExamSync(examId);
            String title = exam != null ? exam.getTitle() : "null";
            String date = exam != null ? exam.getDate() : "";
            String safeDate = date.replace("/", "-");
            String safeTitle = title.trim().replaceAll("\\s+", "_");
            String fileName = String.format(Locale.getDefault(),
                    "Bang_Diem_%s_%s.csv", safeTitle, safeDate);

            List<GradeResult> list = vm.getResultsListSync(examId);
            StringBuilder sb = new StringBuilder();
            sb.append("Tên bài,Họ Và Tên,Số Báo Danh,Mã Đề,Điểm\n");
            for (GradeResult r : list) {
                String studentName = "";
                Student s = studentMap.get(r.sbd != null ? r.sbd.trim() : "");
                if (s != null) studentName = s.getName();
                sb.append("=\"").append(title).append("\""); sb.append(",");
                sb.append("=\"").append(studentName).append("\""); sb.append(",");
                sb.append("=\"").append(r.sbd != null ? r.sbd.trim() : "").append("\""); sb.append(",");
                sb.append("=\"").append(r.maDe != null ? r.maDe.trim() : "").append("\""); sb.append(",");
                sb.append("=\"")
                        .append(String.format(Locale.getDefault(), "%.2f", r.score))
                        .append("\""); sb.append("\n");
            }
            File csv = new File(requireContext().getExternalCacheDir(), fileName);
            try (FileOutputStream fos = new FileOutputStream(csv);
                 OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                 BufferedWriter bw = new BufferedWriter(osw)) {
                fos.write(0xEF); fos.write(0xBB); fos.write(0xBF);
                bw.write(sb.toString());
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Lỗi khi xuất file", Toast.LENGTH_SHORT).show();
                return;
            }
            Uri uri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".fileprovider", csv);
            Intent share = new Intent(Intent.ACTION_SEND)
                    .setType("text/csv")
                    .putExtra(Intent.EXTRA_STREAM, uri)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            requireActivity().runOnUiThread(() ->
                    startActivity(Intent.createChooser(share, "Chia sẻ bảng điểm"))
            );
        }).start();
        Toast.makeText(requireContext(), "Đang xuất file…", Toast.LENGTH_SHORT).show();
    }

    private void navigateUp() {
        NavHostFragment.findNavController(this).navigateUp();
    }

    private void navigateToDetail(Bundle args) {
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_xemLaiFragment_to_gradeDetailFragment, args);
    }
}
