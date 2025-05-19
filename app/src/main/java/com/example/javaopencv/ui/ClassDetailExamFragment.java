package com.example.javaopencv.ui;

import android.os.Bundle;
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
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;
import com.example.javaopencv.ui.adapter.ExamAdapter;
import com.example.javaopencv.viewmodel.ExamViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Collections;


public class ClassDetailExamFragment extends Fragment {
    private static final String ARG_CLASS_ID = "classId";
    private int classId;
    private ExamViewModel vm;

    public static ClassDetailExamFragment newInstance(int classId) {
        Bundle args = new Bundle();
        args.putInt(ARG_CLASS_ID, classId);
        ClassDetailExamFragment fragment = new ClassDetailExamFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_class_detail_exam, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            classId = getArguments().getInt(ARG_CLASS_ID, -1);
        }

        vm = new ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())
        ).get(ExamViewModel.class);

        // RecyclerView & Adapter
        RecyclerView rvExams = view.findViewById(R.id.rvExams);
        rvExams.setLayoutManager(new LinearLayoutManager(requireContext()));
        ExamAdapter adapter = new ExamAdapter();

        adapter.setOnExamItemClickListener(exam -> {
            Bundle args = new Bundle();
            args.putInt("examId",        exam.getId());
            args.putInt("questionCount", exam.getSoCau());
            args.putInt("classId",       classId);
            Navigation.findNavController(view)
                    .navigate(
                            R.id.action_classDetailFragment_to_examDetailFragment,
                            args
                    );
        });

        adapter.setOnExamItemLongClickListener(exam -> {
            String[] opts = {"Chỉnh sửa bài thi", "Xóa bài thi"};
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Chọn hành động")
                    .setItems(opts, (dlg, which) -> {
                        if (which == 0) {
                            NewExamDialogFragment
                                    .newInstanceForEdit(exam)
                                    .show(getParentFragmentManager(), "EditExam");
                        } else {
                            new MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("Xóa bài thi")
                                    .setMessage("Bạn có chắc muốn xóa bài thi này không?")
                                    .setNegativeButton("Hủy", null)
                                    .setPositiveButton("Xóa", (d2, w2) -> {
                                        vm.deleteExam(exam);
                                        Toast.makeText(requireContext(),
                                                        "Đã xóa bài thi", Toast.LENGTH_SHORT)
                                                .show();
                                    })
                                    .show();
                        }
                    })
                    .show();
        });

        rvExams.setAdapter(adapter);

        vm.getExamsForClass(classId)
                .observe(getViewLifecycleOwner(), exams ->
                        adapter.setExamList(exams != null ? exams : Collections.emptyList())
                );

        FloatingActionButton fab = view.findViewById(R.id.fab_add_exam);
        fab.setOnClickListener(v ->
                NewExamDialogFragment
                        .newInstanceForCreate(classId)
                        .show(getParentFragmentManager(), "NewExam")
        );
    }
}