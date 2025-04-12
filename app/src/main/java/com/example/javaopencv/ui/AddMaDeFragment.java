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

    private String maDeToEdit = null;       // Mã đề đang chỉnh sửa (ví dụ "012")
    private int positionToEdit = -1;         // Vị trí mã đề (nếu chỉnh sửa)
    private List<String> oldAnswerList = null;  // Danh sách đáp án cũ (nếu có)
    private int questionCount = 20;         // Số câu (mặc định 20)

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_ma_de, container, false);

        btnBack = view.findViewById(R.id.btn_back);
        btnSave = view.findViewById(R.id.btn_save);
        viewPager = view.findViewById(R.id.view_pager);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);

        // Lấy thông tin từ Bundle: examId, questionCount, maDeToEdit, positionToEdit, oldAnswerList
        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey("examId")) {
                int examId = args.getInt("examId");
                viewModel = new ViewModelProvider(requireActivity()).get(DapAnViewModel.class);
                viewModel.setExamId(examId);
                Log.d("AddMaDeFragment", "ExamId set to: " + examId);
            } else {
                viewModel = new ViewModelProvider(requireActivity()).get(DapAnViewModel.class);
            }
            if (args.containsKey("questionCount")) {
                questionCount = args.getInt("questionCount");
                Log.d("AddMaDeFragment", "Question count received: " + questionCount);
            } else {
                Log.d("AddMaDeFragment", "No questionCount in Bundle, using default: " + questionCount);
            }
            maDeToEdit = args.getString("maDeToEdit", null);
            positionToEdit = args.getInt("positionToEdit", -1);
            oldAnswerList = args.getStringArrayList("oldAnswerList");
            Log.d("AddMaDeFragment", "Received maDeToEdit: " + maDeToEdit + ", positionToEdit: " + positionToEdit);
            if (oldAnswerList != null) {
                Log.d("AddMaDeFragment", "Old answer list: " + oldAnswerList.toString());
            }
        } else {
            viewModel = new ViewModelProvider(requireActivity()).get(DapAnViewModel.class);
        }

        // Tạo adapter cho ViewPager2
        viewPagerAdapter = new MaDeViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        // Sử dụng setter của DapAnTabFragment để truyền số câu (questionCount) đúng
        if (viewPagerAdapter.getDapAnTabFragment() != null) {
            final int qc = questionCount; // biến final
            viewPagerAdapter.getDapAnTabFragment().setQuestionCount(qc);
        }

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Mã đề" : "Đáp án");
        }).attach();

        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        btnSave.setOnClickListener(v -> saveMaDe());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        viewPager.post(() -> {
            // Cập nhật số câu cho DapAnTabFragment (đảm bảo nhất định)
            if (viewPagerAdapter.getDapAnTabFragment() != null) {
                viewPagerAdapter.getDapAnTabFragment().setQuestionCount(questionCount);
            }
            MaDeTabFragment maDeTabFragment = viewPagerAdapter.getMaDeTabFragment();
            DapAnTabFragment dapAnTabFragment = viewPagerAdapter.getDapAnTabFragment();

            if (maDeTabFragment != null && maDeToEdit != null) {
                Log.d("AddMaDeFragment", "Setting selected maDe in MaDeTabFragment: " + maDeToEdit);
                maDeTabFragment.setSelectedMaDe(maDeToEdit);
            }

            if (dapAnTabFragment != null) {
                if (oldAnswerList != null && !oldAnswerList.isEmpty()) {
                    Log.d("AddMaDeFragment", "Setting old answer list in DapAnTabFragment: " + oldAnswerList.toString());
                    dapAnTabFragment.setAnswerListToEdit(oldAnswerList);
                } else if (positionToEdit != -1) {
                    List<String> answerList = viewModel.getAnswerListByPosition(positionToEdit);
                    Log.d("AddMaDeFragment", "Setting answer list from ViewModel in DapAnTabFragment: " + answerList);
                    if (answerList != null) {
                        dapAnTabFragment.setAnswerListToEdit(answerList);
                    }
                }
            }
        });
    }

    private void saveMaDe() {
        MaDeTabFragment maDeTabFragment = viewPagerAdapter.getMaDeTabFragment();
        DapAnTabFragment dapAnTabFragment = viewPagerAdapter.getDapAnTabFragment();

        if (maDeTabFragment == null || dapAnTabFragment == null) return;

        final String maDe = maDeTabFragment.getMaDe();
        final List<String> answers = dapAnTabFragment.getAnswerList();

        if (maDe == null || maDe.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập mã đề", Toast.LENGTH_SHORT).show();
            return;
        }

        final List<String> finalAnswers = new ArrayList<>();
        // Tạo danh sách đáp án sao cho có đúng questionCount phần tử
        for (int i = 0; i < questionCount; i++) {
            if (answers != null && answers.size() > i) {
                finalAnswers.add(answers.get(i));
            } else {
                finalAnswers.add(null);
            }
        }

        if (maDeToEdit != null && positionToEdit != -1) {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Xác nhận")
                    .setMessage("Bạn có chắc chắn muốn thay thế mã đề này không?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        viewModel.updateMaDe(positionToEdit, maDe, finalAnswers);
                        requireActivity().onBackPressed();
                    })
                    .setNegativeButton("Không", null)
                    .show();
        } else {
            List<DapAnViewModel.MaDeItem> currentList = viewModel.getMaDeList().getValue();
            if (currentList != null) {
                boolean exists = false;
                for (DapAnViewModel.MaDeItem item : currentList) {
                    if (item.maDe.equals(maDe)) {
                        exists = true;
                        break;
                    }
                }
                if (exists) {
                    Toast.makeText(getContext(), "Mã đề đã tồn tại. Vui lòng nhập mã đề khác.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            viewModel.addMaDe(maDe, finalAnswers);
            requireActivity().onBackPressed();
        }
    }
}
