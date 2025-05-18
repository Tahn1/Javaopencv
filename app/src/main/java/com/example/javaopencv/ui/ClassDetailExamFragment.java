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
import com.example.javaopencv.data.entity.Exam;
import com.example.javaopencv.ui.adapter.ExamAdapter;
import com.example.javaopencv.viewmodel.ExamViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Collections;

/**
 * Fragment hiển thị danh sách bài thi của một lớp, hỗ trợ thêm, sửa tiêu đề và xóa.
 */
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

        // Lấy classId từ arguments
        if (getArguments() != null) {
            classId = getArguments().getInt(ARG_CLASS_ID, -1);
        }

        // ViewModel
        vm = new ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())
        ).get(ExamViewModel.class);

        // RecyclerView & Adapter
        RecyclerView rvExams = view.findViewById(R.id.rvExams);
        rvExams.setLayoutManager(new LinearLayoutManager(requireContext()));
        ExamAdapter adapter = new ExamAdapter();

        // Click xem chi tiết
        adapter.setOnExamItemClickListener(exam -> {
            Bundle args = new Bundle();
            args.putInt("examId",        exam.getId());
            args.putInt("questionCount", exam.getSoCau());
            args.putInt("classId",       classId);
            // Sử dụng action id để điều hướng và truyền luôn classId
            Navigation.findNavController(view)
                    .navigate(
                            R.id.action_classDetailFragment_to_examDetailFragment,
                            args
                    );
        });

        // Long-press: Chỉnh sửa tiêu đề hoặc xóa bài thi
        adapter.setOnExamItemLongClickListener(exam -> {
            String[] opts = {"Chỉnh sửa bài thi", "Xóa bài thi"};
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Chọn hành động")
                    .setItems(opts, (dlg, which) -> {
                        if (which == 0) {
                            // Mở dialog edit, chỉ sửa tiêu đề
                            NewExamDialogFragment
                                    .newInstanceForEdit(exam)
                                    .show(getParentFragmentManager(), "EditExam");
                        } else {
                            // Xác nhận trước khi xóa
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

        // Quan sát dữ liệu theo classId
        vm.getExamsForClass(classId)
                .observe(getViewLifecycleOwner(), exams ->
                        adapter.setExamList(exams != null ? exams : Collections.emptyList())
                );

        // FAB thêm bài thi
        FloatingActionButton fab = view.findViewById(R.id.fab_add_exam);
        fab.setOnClickListener(v ->
                NewExamDialogFragment
                        .newInstanceForCreate(classId)
                        .show(getParentFragmentManager(), "NewExam")
        );
    }
}