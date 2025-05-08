package com.example.javaopencv.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.Collections;
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

    // Giữ list gốc và chế độ sắp xếp
    private List<GradeResult> fullResults = new ArrayList<>();
    private int sortMode = 0; // 0 = mặc định, 1 = theo SBD, 2 = theo mã đề

    public XemLaiFragment() {
        super(R.layout.fragment_xem_lai);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            examId = getArguments().getInt("examId", -1);
        }
        classId = null;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu,
                                    @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_xem_lai, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // 1) Nút Home
        if (id == android.R.id.home) {
            requireActivity().onBackPressed();
            return true;
        }

        // 2) Xóa tất cả
        if (id == R.id.action_delete_all) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Xóa tất cả bài thi")
                    .setMessage("Bạn có chắc muốn xóa toàn bộ kết quả chấm của kỳ thi này?")
                    .setNegativeButton("Hủy", null)
                    .setPositiveButton("Xóa", (d, w) -> {
                        vm.deleteAllResultsForExam(examId);
                        Toast.makeText(requireContext(),
                                "Đã xóa tất cả kết quả",
                                Toast.LENGTH_SHORT).show();
                    })
                    .show();
            return true;
        }

        // 3) Sort
        if (id == R.id.action_sort) {
            String[] options = {"Sắp theo SBD ↑", "Sắp theo Mã Đề ↑"};
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Chọn cách sắp xếp")
                    .setSingleChoiceItems(options, sortMode - 1, (dialog, which) -> {
                        sortMode = which + 1;
                    })
                    .setPositiveButton("OK", (d, w) -> applySort())
                    .setNegativeButton("Hủy", null)
                    .show();
            return true;
        }

        // 4) Export
        if (id == R.id.action_export) {
            new Thread(() -> {
                // a) Lấy Exam để đặt tên file
                Exam exam = vm.getExamSync(examId);
                String title = exam != null ? exam.getTitle() : null;
                String date  = exam != null ? exam.getDate()  : "";
                String safeDate  = date.replace("/", "-");
                String safeTitle = title != null
                        ? title.trim().replaceAll("\\s+", "_")
                        : "null";
                String fileName  = String.format(Locale.getDefault(),
                        "Bang_Diem_%s_%s.csv", safeTitle, safeDate);

                // b) Lấy dữ liệu
                List<GradeResult> list = vm.getResultsListSync(examId);

                // c) Xây dựng CSV (ép kiểu Excel bằng ="…")
                StringBuilder sb = new StringBuilder();
                sb.append("Tên bài,Họ Và Tên,Số Báo Danh,Mã Đề,Điểm\n");
                for (GradeResult r : list) {
                    String cellTitle = title != null ? title : "null";
                    String studentName = "";
                    Student s = studentMap.get(r.sbd != null ? r.sbd.trim() : "");
                    if (s != null) studentName = s.getName();
                    String sbdRaw   = r.sbd   != null ? r.sbd.trim()   : "";
                    String maDeRaw  = r.maDe  != null ? r.maDe.trim()  : "";
                    String scoreRaw = String.format(Locale.getDefault(),"%.2f", r.score);

                    sb.append("=\"").append(cellTitle).append("\"").append(",");
                    sb.append("=\"").append(studentName).append("\"").append(",");
                    sb.append("=\"").append(sbdRaw).append("\"").append(",");
                    sb.append("=\"").append(maDeRaw).append("\"").append(",");
                    sb.append("=\"").append(scoreRaw).append("\"").append("\n");
                }

                // d) Ghi file với BOM + UTF-8
                File csv = new File(requireContext()
                        .getExternalCacheDir(), fileName);
                try (FileOutputStream fos = new FileOutputStream(csv);
                     OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                     BufferedWriter bw = new BufferedWriter(osw)) {

                    fos.write(0xEF);
                    fos.write(0xBB);
                    fos.write(0xBF);
                    bw.write(sb.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                // e) Share
                Uri uri = FileProvider.getUriForFile(
                        requireContext(),
                        requireContext().getPackageName() + ".fileprovider",
                        csv
                );
                Intent share = new Intent(Intent.ACTION_SEND)
                        .setType("text/csv")
                        .putExtra(Intent.EXTRA_STREAM, uri)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                requireActivity().runOnUiThread(() ->
                        startActivity(Intent.createChooser(share, "Chia sẻ bảng điểm"))
                );
            }).start();

            Toast.makeText(requireContext(),
                    "Đang xuất file…",
                    Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Áp dụng sắp xếp dựa trên sortMode lên fullResults rồi submit cho adapter
     */
    private void applySort() {
        if (fullResults == null) return;
        List<GradeResult> sorted = new ArrayList<>(fullResults);
        if (sortMode == 1) {
            Collections.sort(sorted, Comparator.comparing(r -> r.sbd != null ? r.sbd.trim() : ""));
        } else if (sortMode == 2) {
            Collections.sort(sorted, Comparator.comparing(r -> r.maDe != null ? r.maDe.trim() : ""));
        }
        adapter.submitList(sorted);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1) Setup RecyclerView & Adapter
        RecyclerView rv = view.findViewById(R.id.rvGradeResults);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new GradeResultAdapter();
        rv.setAdapter(adapter);

        // 2) ViewModel
        vm = new ViewModelProvider(this).get(XemLaiViewModel.class);

        // 3) Quan sát kết quả chấm
        vm.getResultsForExam(examId)
                .observe(getViewLifecycleOwner(), results -> {
                    fullResults = results != null ? results : new ArrayList<>();
                    applySort();
                });

        // 4) Quan sát classId để load & map tên HS
        vm.getClassIdForExam(examId)
                .observe(getViewLifecycleOwner(), cid -> {
                    classId = cid;
                    if (cid != null) {
                        vm.getStudentsForClass(cid)
                                .observe(getViewLifecycleOwner(), students -> {
                                    studentMap.clear();
                                    Map<String, Student> map = new HashMap<>();
                                    for (Student s : students) {
                                        if (s.getStudentNumber() != null) {
                                            map.put(s.getStudentNumber().trim(), s);
                                        }
                                    }
                                    studentMap.putAll(map);
                                    adapter.setStudentMap(map);
                                });
                    } else {
                        studentMap.clear();
                        adapter.setStudentMap(new HashMap<>());
                    }
                });

        // 5) Click / Long-click
        adapter.setOnItemClickListener(item -> {
            Bundle args = new Bundle();
            args.putLong("gradeId", item.id);
            if (classId != null) args.putInt("classId", classId);
            androidx.navigation.fragment.NavHostFragment
                    .findNavController(this)
                    .navigate(R.id.action_xemLaiFragment_to_gradeDetailFragment, args);
        });

        adapter.setOnItemLongClickListener(item -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Xóa kết quả chấm")
                    .setMessage("Bạn có chắc muốn xóa kết quả này không?")
                    .setNegativeButton("Hủy", null)
                    .setPositiveButton("Xóa", (d, w) -> vm.deleteResult(item))
                    .show();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().invalidateOptionsMenu();
    }
}
