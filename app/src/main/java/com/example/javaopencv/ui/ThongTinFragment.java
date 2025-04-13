package com.example.javaopencv.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.javaopencv.R;
import com.example.javaopencv.data.AppDatabase;
import com.example.javaopencv.data.dao.ExamDao;
import com.example.javaopencv.data.dao.ExamStatsDao;
import com.example.javaopencv.data.entity.Exam;
import com.example.javaopencv.data.entity.ExamStats;
import com.example.javaopencv.viewmodel.DapAnViewModel;

public class ThongTinFragment extends Fragment {

    private ImageButton btnBack;
    private TextView tvExamTitle, tvExamPhieu, tvExamSoCau;
    private TextView tvExamSoBaiCham, tvExamSoDapAn;
    private TextView tvExamDTB, tvExamMin, tvExamMax;

    // examId truyền từ ExamDetailFragment
    private int examId = -1;
    // Số câu của bài thi
    private int soCau = 20;

    // ViewModel của đáp án (chứa danh sách mã đề)
    private DapAnViewModel dapAnViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_thong_tin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ View
        btnBack = view.findViewById(R.id.btn_back);
        tvExamTitle = view.findViewById(R.id.tv_exam_title);
        tvExamPhieu = view.findViewById(R.id.tv_exam_phieu);
        tvExamSoCau = view.findViewById(R.id.tv_exam_socau);
        tvExamSoBaiCham = view.findViewById(R.id.tv_exam_sobaicham);
        tvExamSoDapAn = view.findViewById(R.id.tv_exam_sodapan);
        tvExamDTB = view.findViewById(R.id.tv_exam_dtb);
        tvExamMin = view.findViewById(R.id.tv_exam_min);
        tvExamMax = view.findViewById(R.id.tv_exam_max);

        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        // Lấy dữ liệu examId và questionCount từ Bundle truyền sang
        Bundle args = getArguments();
        if (args != null) {
            examId = args.getInt("examId", -1);
            soCau = args.getInt("questionCount", 20);
        }

        // Khởi tạo dữ liệu từ DB cho Exam và ExamStats
        loadExamInfo();

        // Lấy DapAnViewModel để lấy danh sách mã đề (số đáp án)
        dapAnViewModel = new ViewModelProvider(requireActivity()).get(DapAnViewModel.class);
        // Quan sát LiveData chứa danh sách mã đề, cập nhật Số đáp án theo kích thước
        dapAnViewModel.getMaDeList().observe(getViewLifecycleOwner(), maDeItems -> {
            int soDapAn = (maDeItems != null) ? maDeItems.size() : 0;
            tvExamSoDapAn.setText(String.valueOf(soDapAn));
        });
    }

    /**
     * Load thông tin bài thi từ bảng Exam và thống kê từ ExamStats
     */
    private void loadExamInfo() {
        new Thread(() -> {
            // Lấy instance DB
            AppDatabase db = AppDatabase.getInstance(requireContext());
            ExamDao examDao = db.examDao();
            ExamStatsDao examStatsDao = db.examStatsDao();

            // Lấy dữ liệu bài thi từ bảng Exam (sử dụng phương thức đồng bộ getExamSync)
            final Exam exam = examDao.getExamSync(examId);
            // Lấy dữ liệu thống kê của bài thi từ bảng exam_stats
            final ExamStats stats = examStatsDao.getExamStats(examId);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (exam != null) {
                        tvExamTitle.setText(exam.title);
                        tvExamPhieu.setText(exam.phieu);
                        tvExamSoCau.setText(String.valueOf(exam.soCau));
                    } else {
                        tvExamTitle.setText("N/A");
                        tvExamPhieu.setText("N/A");
                        tvExamSoCau.setText("N/A");
                    }
                    // Nếu stats != null, gán dữ liệu thống kê, nếu không, dùng giá trị mặc định
                    if (stats != null) {
                        tvExamSoBaiCham.setText(String.valueOf(stats.soBaiCham));
                        // Số đáp án hiển thị sẽ được cập nhật thông qua LiveData ở ViewModel (xem bên onViewCreated)
                        tvExamDTB.setText(String.format("%.2f", stats.diemTrungBinh));
                        tvExamMin.setText(String.format("%.2f", stats.diemThapNhat));
                        tvExamMax.setText(String.format("%.2f", stats.diemCaoNhat));
                    } else {
                        tvExamSoBaiCham.setText("0");
                        tvExamDTB.setText("0.00");
                        tvExamMin.setText("0.00");
                        tvExamMax.setText("0.00");
                    }
                });
            }
        }).start();
    }
}
