package com.example.javaopencv.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.javaopencv.R;
import com.example.javaopencv.data.AppDatabase;
import com.example.javaopencv.data.dao.ExamDao;
import com.example.javaopencv.data.entity.Exam;
import com.example.javaopencv.data.entity.GradeResult;
import com.example.javaopencv.viewmodel.DapAnViewModel;
import com.example.javaopencv.viewmodel.XemLaiViewModel;

import java.util.List;

public class ThongTinFragment extends Fragment {

    private TextView
            tvExamTitle,
            tvExamClassLabel,
            tvExamClass,
            tvExamSubjectLabel,
            tvExamSubject,
            tvExamPhieu,
            tvExamSoCau,
            tvExamSoBaiCham,
            tvExamSoDapAn,
            tvExamDTB,
            tvExamMin,
            tvExamMax;

    private int examId = -1;
    private int soCau  = 20;

    private DapAnViewModel dapAnViewModel;
    private XemLaiViewModel xemLaiViewModel;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_thong_tin, container, false);
    }

    @Override public void onViewCreated(@NonNull View view,
                                        @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1) Ánh xạ tất cả các TextView
        tvExamTitle         = view.findViewById(R.id.tv_exam_title);
        tvExamClassLabel    = view.findViewById(R.id.tv_exam_class_label);
        tvExamClass         = view.findViewById(R.id.tv_exam_class);
        tvExamSubjectLabel  = view.findViewById(R.id.tv_exam_subject_label);
        tvExamSubject       = view.findViewById(R.id.tv_exam_subject);
        tvExamPhieu         = view.findViewById(R.id.tv_exam_phieu);
        tvExamSoCau         = view.findViewById(R.id.tv_exam_socau);
        tvExamSoBaiCham     = view.findViewById(R.id.tv_exam_sobaicham);
        tvExamSoDapAn       = view.findViewById(R.id.tv_exam_sodapan);
        tvExamDTB           = view.findViewById(R.id.tv_exam_dtb);
        tvExamMin           = view.findViewById(R.id.tv_exam_min);
        tvExamMax           = view.findViewById(R.id.tv_exam_max);

        // 2) Lấy args
        Bundle args = getArguments();
        if (args != null) {
            examId = args.getInt("examId", -1);
            soCau  = args.getInt("questionCount", 20);
        }

        // 3) Load thông tin cơ bản
        loadExamInfo();

        // 4) Quan sát số mã đề (số đáp án)
        dapAnViewModel = new ViewModelProvider(requireActivity())
                .get(DapAnViewModel.class);
        dapAnViewModel.getMaDeList().observe(getViewLifecycleOwner(), maDeItems -> {
            int soDapAn = (maDeItems != null) ? maDeItems.size() : 0;
            tvExamSoDapAn.setText(String.valueOf(soDapAn));
        });

        // 5) Quan sát kết quả đã chấm
        xemLaiViewModel = new ViewModelProvider(requireActivity())
                .get(XemLaiViewModel.class);
        xemLaiViewModel.getResultsForExam(examId)
                .observe(getViewLifecycleOwner(), results -> {
                    int soBaiCham = (results != null) ? results.size() : 0;
                    tvExamSoBaiCham.setText(String.valueOf(soBaiCham));

                    if (results != null && !results.isEmpty()) {
                        double sum = 0, min = Double.MAX_VALUE, max = Double.MIN_VALUE;
                        for (GradeResult gr : results) {
                            double sc = gr.score;
                            sum += sc;
                            if (sc < min) min = sc;
                            if (sc > max) max = sc;
                        }
                        double avg = sum / results.size();
                        tvExamDTB.setText(String.format("%.2f", avg));
                        tvExamMin.setText(String.format("%.2f", min));
                        tvExamMax.setText(String.format("%.2f", max));
                    } else {
                        tvExamDTB.setText("0.00");
                        tvExamMin.setText("0.00");
                        tvExamMax.setText("0.00");
                    }
                });
    }

    /** Load tiêu đề, phiếu, số câu, lớp và môn từ bảng Exam */
    private void loadExamInfo() {
        new Thread(() -> {
            ExamDao examDao = AppDatabase.getInstance(requireContext()).examDao();
            final Exam exam = examDao.getExamSync(examId);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (exam != null) {
                        // Tiêu đề, phiếu, số câu
                        tvExamTitle.setText(exam.getTitle());
                        tvExamPhieu.setText(exam.getPhieu());
                        tvExamSoCau.setText(String.valueOf(exam.getSoCau()));

                        // Lớp học
                        if (exam.getClassName() != null && !exam.getClassName().isEmpty()) {
                            tvExamClassLabel.setVisibility(View.VISIBLE);
                            tvExamClass.setVisibility(View.VISIBLE);
                            tvExamClass.setText(exam.getClassName());
                        } else {
                            tvExamClassLabel.setVisibility(View.GONE);
                            tvExamClass.setVisibility(View.GONE);
                        }

                        // Môn học
                        if (exam.getSubjectName() != null && !exam.getSubjectName().isEmpty()) {
                            tvExamSubjectLabel.setVisibility(View.VISIBLE);
                            tvExamSubject.setVisibility(View.VISIBLE);
                            tvExamSubject.setText(exam.getSubjectName());
                        } else {
                            tvExamSubjectLabel.setVisibility(View.GONE);
                            tvExamSubject.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(requireContext(), "Không tìm thấy bài thi!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }
}
