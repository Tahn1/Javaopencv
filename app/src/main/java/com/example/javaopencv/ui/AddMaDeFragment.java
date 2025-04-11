package com.example.javaopencv.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.ExamCodeEntry;
import com.example.javaopencv.viewmodel.ExamCodeViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AddMaDeFragment extends Fragment {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ImageButton btnBack, btnSave;
    private MaDeViewPagerAdapter viewPagerAdapter;
    private ExamCodeViewModel examCodeViewModel;

    private int examId;
    private int questionCount;
    private String code;
    private String answers;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate layout; đảm bảo rằng file fragment_add_ma_de.xml chứa tab_layout, view_pager, btn_back, btn_save
        View view = inflater.inflate(R.layout.fragment_add_ma_de, container, false);

        btnBack = view.findViewById(R.id.btn_back);
        btnSave = view.findViewById(R.id.btn_save);
        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);

        btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        // Nhận dữ liệu từ Bundle
        Bundle args = getArguments();
        if (args != null) {
            examId = args.getInt("examId", 0);
            questionCount = args.getInt("questionCount", 0);
            // Nếu questionCount bằng 0, gán giá trị mặc định (ví dụ 10)
            if (questionCount == 0) {
                questionCount = 10;
            }
            code = args.getString("code", "");
            answers = args.getString("answers", "");
            Log.d("AddMaDeFragment", "examId=" + examId + " questionCount=" + questionCount +
                    " code=" + code + " answers=" + answers);
            // Cập nhật lại Bundle để truyền cho các tab của adapter
            args.putInt("questionCount", questionCount);
        } else {
            Toast.makeText(getContext(), "Missing exam data!", Toast.LENGTH_SHORT).show();
        }

        // Khởi tạo ViewModel
        examCodeViewModel = new ViewModelProvider(this).get(ExamCodeViewModel.class);

        // Tạo adapter cho ViewPager2: truyền Bundle để state được giữ lại ở cả 2 tab (MÃ ĐỀ và ĐÁP ÁN)
        viewPagerAdapter = new MaDeViewPagerAdapter(this, args);
        viewPager.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "MÃ ĐỀ" : "ĐÁP ÁN");
        }).attach();

        btnSave.setOnClickListener(v -> {
            // Lấy dữ liệu từ hai tab
            MaDeTabFragment maDeTab = viewPagerAdapter.getMaDeTabFragment();
            DapAnTabFragment dapAnTab = viewPagerAdapter.getDapAnTabFragment();
            String newCode = (maDeTab != null) ? maDeTab.getMaDe() : "";
            // Sử dụng phương thức buildAnswersListString() từ DapAnTabFragment (đảm bảo rằng nó được định nghĩa trong DapAnTabFragment)
            String newAnswers = (dapAnTab != null) ? dapAnTab.buildAnswersListString() : "";

            if (!newCode.isEmpty()) {
                // Giả sử constructor của ExamCodeEntry theo thứ tự: examId, code, answers, questionCount
                ExamCodeEntry updatedEntry = new ExamCodeEntry(examId, newCode, newAnswers, questionCount);
                examCodeViewModel.updateExamCodeEntry(updatedEntry);
                Toast.makeText(getContext(), "Lưu mã đề thành công", Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(this).navigateUp();
            } else {
                Toast.makeText(getContext(), "Chưa nhập đủ mã đề", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
