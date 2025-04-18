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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.javaopencv.R;
import com.example.javaopencv.ui.adapter.MaDeViewPagerAdapter;
import com.example.javaopencv.viewmodel.DapAnViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class AddMaDeFragment extends Fragment {

    private ViewPager2 viewPager;
    private MaDeViewPagerAdapter viewPagerAdapter;
    private ImageButton btnBack, btnSave;
    private DapAnViewModel viewModel;

    private String maDeToEdit = null;
    private int positionToEdit = -1;
    private List<String> oldAnswerList = null;
    // Số câu được truyền từ Bundle, mặc định là 20
    private int questionCount = 20;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_ma_de, container, false);

        // 1) Ánh xạ view
        btnBack   = view.findViewById(R.id.btn_back);
        btnSave   = view.findViewById(R.id.btn_save);
        viewPager = view.findViewById(R.id.view_pager);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);

        // 2) Khởi tạo ViewPager + TabLayout
        viewPagerAdapter = new MaDeViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Mã đề" : "Đáp án");
        }).attach();

        // 3) Khởi tạo ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(DapAnViewModel.class);

        // 4) Đọc arguments và set examId cũng như questionCount
        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey("examId")) {
                int examId = args.getInt("examId");
                viewModel.setExamId(examId);
                Log.d("AddMaDeFragment", "ExamId đã set vào ViewModel: " + examId);
            }
            if (args.containsKey("questionCount")) {
                questionCount = args.getInt("questionCount");
                Log.d("AddMaDeFragment", "Question count received: " + questionCount);
            }
            maDeToEdit     = args.getString("maDeToEdit", null);
            positionToEdit = args.getInt("positionToEdit", -1);
            oldAnswerList  = args.getStringArrayList("oldAnswerList");
            Log.d("AddMaDeFragment", "Received maDeToEdit: " + maDeToEdit +
                    ", positionToEdit: " + positionToEdit);
            if (oldAnswerList != null) {
                Log.d("AddMaDeFragment", "Old answer list: " + oldAnswerList);
            }
        }

        // 5) Truyền questionCount xuống DapAnTabFragment
        if (viewPagerAdapter.getDapAnTabFragment() != null) {
            viewPagerAdapter.getDapAnTabFragment().setQuestionCount(questionCount);
        }

        // 6) Xử lý nút Back / Save
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        btnSave.setOnClickListener(v -> saveMaDe());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Sau khi layout xong, khôi phục lại state nếu đang sửa
        viewPager.post(() -> {
            if (viewPagerAdapter.getDapAnTabFragment() != null) {
                viewPagerAdapter.getDapAnTabFragment().setQuestionCount(questionCount);
            }
            MaDeTabFragment maDeTab = viewPagerAdapter.getMaDeTabFragment();
            DapAnTabFragment dapAnTab = viewPagerAdapter.getDapAnTabFragment();
            if (maDeTab != null && maDeToEdit != null) {
                maDeTab.setSelectedMaDe(maDeToEdit);
            }
            if (dapAnTab != null && positionToEdit != -1) {
                List<String> answers = viewModel.getAnswerListByPosition(positionToEdit);
                if (answers != null && !answers.isEmpty()) {
                    dapAnTab.setAnswerListToEdit(answers);
                }
            }
        });
    }

    private void saveMaDe() {
        MaDeTabFragment maDeTab = viewPagerAdapter.getMaDeTabFragment();
        DapAnTabFragment dapAnTab = viewPagerAdapter.getDapAnTabFragment();
        if (maDeTab == null || dapAnTab == null) return;

        final String maDe = maDeTab.getMaDe();
        final List<String> answers = dapAnTab.getAnswerList();

        if (maDe == null || maDe.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập mã đề", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chuẩn hóa độ dài answers thành questionCount
        List<String> finalAnswers = new ArrayList<>();
        for (int i = 0; i < questionCount; i++) {
            finalAnswers.add(i < answers.size() ? answers.get(i) : null);
        }

        if (maDeToEdit != null && positionToEdit != -1) {
            // Chế độ sửa
            new AlertDialog.Builder(requireContext())
                    .setTitle("Xác nhận")
                    .setMessage("Bạn có chắc chắn muốn thay thế mã đề này không?")
                    .setPositiveButton("Có", (dlg, w) -> {
                        viewModel.updateMaDe(positionToEdit, maDe, finalAnswers, questionCount);
                        requireActivity().onBackPressed();
                    })
                    .setNegativeButton("Không", null)
                    .show();
        } else {
            // Chế độ thêm mới, kiểm tra trùng
            List<DapAnViewModel.MaDeItem> list = viewModel.getMaDeList().getValue();
            if (list != null) {
                for (DapAnViewModel.MaDeItem item : list) {
                    if (item.code.equals(maDe)) {
                        Toast.makeText(getContext(),
                                "Mã đề đã tồn tại. Vui lòng nhập mã đề khác.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
            // Thêm mới
            viewModel.addMaDe(maDe, finalAnswers, questionCount);
            requireActivity().onBackPressed();
        }
    }
}
