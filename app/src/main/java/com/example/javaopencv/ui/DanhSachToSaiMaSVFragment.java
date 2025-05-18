package com.example.javaopencv.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.GradeResult;
import com.example.javaopencv.data.entity.Student;
import com.example.javaopencv.ui.adapter.WrongSbdAdapter;
import com.example.javaopencv.viewmodel.GradeResultViewModel;
import com.example.javaopencv.viewmodel.StudentViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Fragment hiển thị danh sách các bài thi có SBD thiếu
 * hoặc không tồn tại trong danh sách sinh viên của lớp.
 */
public class DanhSachToSaiMaSVFragment extends Fragment {
    private GradeResultViewModel viewModel;
    private StudentViewModel studentVm;
    private RecyclerView recyclerView;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_danh_sach_to_sai_ma_sv,
                container, false
        );
        recyclerView = view.findViewById(R.id.recyclerViewWrongSbd);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        return view;
    }

    @Override public void onViewCreated(@NonNull View view,
                                        @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int examId  = requireArguments().getInt("examId",  -1);
        int classId = requireArguments().getInt("classId", -1);

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory
                        .getInstance(requireActivity().getApplication())
        ).get(GradeResultViewModel.class);

        studentVm = new ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory
                        .getInstance(requireActivity().getApplication())
        ).get(StudentViewModel.class);

        // 2) Lấy danh sách SBD hợp lệ của class
        studentVm.getStudentsForClass(classId)
                .observe(getViewLifecycleOwner(), students -> {
                    Set<String> validSbd = new HashSet<>();
                    if (students != null) {
                        for (Student s : students) {
                            validSbd.add(s.getStudentNumber());
                        }
                    }

                    // 3) Quan sát kết quả chấm và lọc SBD sai
                    viewModel.getResultsForExam(examId)
                            .observe(getViewLifecycleOwner(), results -> {
                                List<GradeResult> wrongList = new ArrayList<>();
                                if (results != null) {
                                    for (GradeResult r : results) {
                                        String sbd = r.getSbd();
                                        if (sbd == null || sbd.length() != 6
                                                || !validSbd.contains(sbd)) {
                                            wrongList.add(r);
                                        }
                                    }
                                }

                                // 4) Dùng adapter mới để hiển thị CardView và xử lý click
                                WrongSbdAdapter adapter = new WrongSbdAdapter(
                                        wrongList,
                                        gr -> {
                                            // Khi click---đổi thành truyền Fragment 'this'
                                            Bundle args = new Bundle();
                                            args.putLong("gradeId", gr.getId());
                                            NavHostFragment.findNavController(
                                                    DanhSachToSaiMaSVFragment.this
                                            ).navigate(
                                                    R.id.action_danhSachToSaiMaSVFragment_to_gradeDetailFragment,
                                                    args
                                            );
                                        }
                                );
                                recyclerView.setAdapter(adapter);
                            });
                });
    }
}
