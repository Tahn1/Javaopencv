package com.example.javaopencv.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.StudentResult;
import com.example.javaopencv.data.entity.Student;
import com.example.javaopencv.data.entity.GradeResult;
import com.example.javaopencv.data.entity.Exam;
import com.example.javaopencv.ui.adapter.StudentResultAdapter;
import com.example.javaopencv.viewmodel.StudentViewModel;
import com.example.javaopencv.viewmodel.GradeResultViewModel;
import com.example.javaopencv.viewmodel.ExamViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class StudentListFragment extends Fragment {
    private int examId;
    private RecyclerView rvStudents;
    private StudentResultAdapter adapter;
    private StudentViewModel studentVm;
    private GradeResultViewModel gradeVm;
    private ExamViewModel examVm;

    private Exam currentExam;
    private final List<StudentResult> fullList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            examId = getArguments().getInt("examId", -1);
        }
        requireActivity().getOnBackPressedDispatcher()
                .addCallback(this, new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        NavHostFragment.findNavController(StudentListFragment.this).navigateUp();
                    }
                });
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvStudents = view.findViewById(R.id.rvStudents);
        rvStudents.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new StudentResultAdapter();
        rvStudents.setAdapter(adapter);

        studentVm = new ViewModelProvider(this).get(StudentViewModel.class);
        gradeVm   = new ViewModelProvider(this).get(GradeResultViewModel.class);
        examVm    = new ViewModelProvider(this).get(ExamViewModel.class);

        examVm.getExamById(examId).observe(getViewLifecycleOwner(), exam -> {
            currentExam = exam;
            if (exam == null || exam.getClassId() == null) {
                fullList.clear();
                adapter.submitList(fullList);
                Toast.makeText(requireContext(), "Bài thi chưa gán lớp!", Toast.LENGTH_SHORT).show();
                return;
            }
            int classId = exam.getClassId();
            studentVm.getStudentsForClass(classId).observe(getViewLifecycleOwner(), students -> {
                gradeVm.getResultsForExam(examId).observe(getViewLifecycleOwner(), results -> {
                    fullList.clear();
                    HashMap<String, GradeResult> gradeMap = new HashMap<>();
                    for (GradeResult gr : results) {
                        if (gr.sbd != null) gradeMap.put(gr.sbd.trim(), gr);
                    }
                    for (Student s : students) {
                        GradeResult gr = gradeMap.get(s.getStudentNumber());
                        Double score = gr != null ? gr.score : null;
                        fullList.add(new StudentResult(
                                s.getId(), s.getName(), s.getStudentNumber(), score, ""));
                    }
                    adapter.submitList(new ArrayList<>(fullList));
                });
            });
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_student_list, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Tìm tên/Mã SV...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { filter(query); return true; }
            @Override public boolean onQueryTextChange(String newText) { filter(newText); return true; }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_export_excel) {
            exportStudentListCsv();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void filter(String text) {
        String lower = text == null ? "" : text.toLowerCase();
        List<StudentResult> filtered = new ArrayList<>();
        for (StudentResult sr : fullList) {
            if (sr.getName().toLowerCase().contains(lower)
                    || sr.getStudentNumber().toLowerCase().contains(lower)) {
                filtered.add(sr);
            }
        }
        adapter.submitList(filtered);
    }

    /**
     * Xuất danh sách học sinh ra CSV (Excel) và chia sẻ,
     * thêm dòng metadata StudentList_<Test>_<Subject>_<dd-MM-yyyy> đầu file
     */
    private void exportStudentListCsv() {
        new Thread(() -> {
            // 1) Lấy metadata
            String testName    = currentExam != null ? currentExam.getTitle()       : "";
            String subjectName = currentExam != null ? currentExam.getSubjectName() : "";
            String dateRaw     = currentExam != null ? currentExam.getDate()        : ""; // dd/MM/yyyy
            String safeTest    = testName.trim().replaceAll("\\s+", "_");
            String safeSubject = subjectName.trim().replaceAll("\\s+", "_");
            String safeDate    = dateRaw.replace("/", "-");
            String metaLine    = "StudentList_" + safeTest + "_" + safeSubject + "_" + safeDate;

            // 2) Xây dựng CSV
            StringBuilder sb = new StringBuilder();
            sb.append(metaLine).append('\n');
            sb.append("Họ và tên,Số Báo Danh,Điểm\n");
            for (StudentResult sr : fullList) {
                sb.append(sr.getName() != null ? sr.getName() : "");
                sb.append(',');
                sb.append("=\"").append(sr.getStudentNumber()).append("\"");
                sb.append(',');
                sb.append(sr.getScore() != null
                        ? String.format(Locale.getDefault(), "%.2f", sr.getScore())
                        : "");
                sb.append('\n');
            }

            // 3) Ghi file với tên metaLine.csv
            String fileName = metaLine + ".csv";
            try {
                File dir = requireContext().getExternalFilesDir("exports");
                if (dir != null && !dir.exists()) dir.mkdirs();
                File csv = new File(dir, fileName);
                try (FileOutputStream fos = new FileOutputStream(csv);
                     OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                     BufferedWriter bw = new BufferedWriter(osw)) {
                    fos.write(0xEF); fos.write(0xBB); fos.write(0xBF);
                    bw.write(sb.toString()); bw.flush();
                }
                Uri uri = FileProvider.getUriForFile(requireContext(),
                        requireContext().getPackageName() + ".fileprovider", csv);
                Intent share = new Intent(Intent.ACTION_SEND)
                        .setType("text/csv")
                        .putExtra(Intent.EXTRA_STREAM, uri)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                requireActivity().runOnUiThread(() ->
                        startActivity(Intent.createChooser(share, "Chia sẻ danh sách")));
            } catch (IOException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Lỗi xuất danh sách", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void navigateUp() {
        NavHostFragment.findNavController(this).navigateUp();
    }
}
