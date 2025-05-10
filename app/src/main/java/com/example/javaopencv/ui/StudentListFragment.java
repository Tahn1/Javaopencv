package com.example.javaopencv.ui;

import android.os.Bundle;
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
import com.example.javaopencv.data.entity.StudentResult;
import com.example.javaopencv.ui.adapter.StudentResultAdapter;
import com.example.javaopencv.viewmodel.ExamViewModel;
import com.example.javaopencv.viewmodel.GradeResultViewModel;
import com.example.javaopencv.viewmodel.StudentViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentListFragment extends Fragment {
    private int examId;
    private RecyclerView rvStudents;
    private StudentResultAdapter adapter;
    private StudentViewModel studentVm;
    private GradeResultViewModel gradeVm;
    private ExamViewModel examVm;

    // Giữ nguyên danh sách gốc để lọc
    private final List<StudentResult> fullList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // bật menu
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

        // Lấy examId
        if (getArguments() != null) {
            examId = getArguments().getInt("examId", -1);
        }

        // Setup RecyclerView + Adapter
        rvStudents = view.findViewById(R.id.rvStudents);
        rvStudents.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new StudentResultAdapter();
        rvStudents.setAdapter(adapter);

        // ViewModel
        studentVm = new ViewModelProvider(this).get(StudentViewModel.class);
        gradeVm   = new ViewModelProvider(this).get(GradeResultViewModel.class);
        examVm    = new ViewModelProvider(this).get(ExamViewModel.class);

        // Quan sát exam → lấy classId → mix students + grades
        examVm.getExamById(examId).observe(getViewLifecycleOwner(), exam -> {
            if (exam == null || exam.getClassId() == null) {
                fullList.clear();
                adapter.submitList(fullList);
                Toast.makeText(requireContext(),
                        "Bài thi chưa gán lớp!", Toast.LENGTH_SHORT).show();
                return;
            }
            int classId = exam.getClassId();
            studentVm.getStudentsForClass(classId)
                    .observe(getViewLifecycleOwner(), students -> {
                        gradeVm.getResultsForExam(examId)
                                .observe(getViewLifecycleOwner(), results -> {
                                    // build fullList
                                    fullList.clear();
                                    Map<String, com.example.javaopencv.data.entity.GradeResult> gradeMap = new HashMap<>();
                                    for (com.example.javaopencv.data.entity.GradeResult gr : results) {
                                        if (gr.sbd != null) gradeMap.put(gr.sbd.trim(), gr);
                                    }
                                    for (com.example.javaopencv.data.entity.Student s : students) {
                                        com.example.javaopencv.data.entity.GradeResult gr =
                                                gradeMap.get(s.getStudentNumber());
                                        Double score = gr != null ? gr.score : null;
                                        String note  = gr != null ? gr.note  : "";
                                        fullList.add(new StudentResult(
                                                s.getId(),
                                                s.getName(),
                                                s.getStudentNumber(),
                                                score,
                                                note
                                        ));
                                    }
                                    // hiển thị đầy đủ trước khi lọc
                                    adapter.submitList(new ArrayList<>(fullList));
                                });
                    });
        });
    }

    // Inflate menu search
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu,
                                    @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_student_list, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setQueryHint("Tìm tên/Mã SV...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    // Lọc danh sách dựa trên tên hoặc mã SV
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
}
