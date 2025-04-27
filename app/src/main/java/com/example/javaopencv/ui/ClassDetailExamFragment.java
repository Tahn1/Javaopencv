package com.example.javaopencv.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.Exam;
import com.example.javaopencv.ui.NewExamDialogFragment;
import com.example.javaopencv.ui.adapter.ExamAdapter;
import com.example.javaopencv.viewmodel.ExamViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class ClassDetailExamFragment extends Fragment {
    private static final String ARG_CLASS_ID = "classId";
    private int classId;

    public static ClassDetailExamFragment newInstance(int classId) {
        Bundle args = new Bundle();
        args.putInt(ARG_CLASS_ID, classId);
        ClassDetailExamFragment f = new ClassDetailExamFragment();
        f.setArguments(args);
        return f;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_class_detail_exam, container, false);
    }

    @Override public void onViewCreated(@NonNull View view,
                                        @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1) Lấy classId từ args
        if (getArguments() != null) {
            classId = getArguments().getInt(ARG_CLASS_ID, -1);
        }

        // 2) Ánh xạ RecyclerView & FAB
        RecyclerView rvExams = view.findViewById(R.id.rvExams);
        FloatingActionButton fab = view.findViewById(R.id.fab_add_exam);

        // 3) Khởi tạo adapter và gán listener
        ExamAdapter adapter = new ExamAdapter();
        adapter.setOnExamItemClickListener(exam -> {
            // nếu cần xử lý click vào exam ở đây
        });
        adapter.setOnExamItemLongClickListener(exam -> {
            // nếu cần xử lý long-click ở đây
        });

        rvExams.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvExams.setAdapter(adapter);

        // 4) ViewModel + observe danh sách exam cho lớp này
        ExamViewModel vm = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(ExamViewModel.class);

        vm.getExamsForClass(classId)
                .observe(getViewLifecycleOwner(), (List<Exam> list) -> {
                    adapter.setExamList(list);
                });

        // 5) Nhấn FAB sẽ mở NewExamDialogFragment, tự gán đúng classId
        fab.setOnClickListener(v -> {
            NewExamDialogFragment
                    .newInstanceForCreate(classId)
                    .show(getParentFragmentManager(), "NewExam");
        });
    }
}
