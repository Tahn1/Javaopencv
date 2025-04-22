package com.example.javaopencv.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.javaopencv.R;
import com.example.javaopencv.data.AppDatabase;
import com.example.javaopencv.data.dao.AnswerDao;
import com.example.javaopencv.data.entity.Answer;
import com.example.javaopencv.data.entity.GradeResult;
import com.example.javaopencv.ui.EditDapAnTabFragment.OnAnswerChangeListener;
import com.example.javaopencv.viewmodel.XemLaiViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditGradeFragment extends Fragment implements OnAnswerChangeListener {
    private XemLaiViewModel viewModel;
    private GradeResult current;
    private ViewPager2 pager;
    private MaterialToolbar toolbar;

    public EditGradeFragment() { }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_grade_tabs, container, false);
    }

    @Override public void onViewCreated(@NonNull View view,
                                        @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar = view.findViewById(R.id.toolbar_edit_grade);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        toolbar.inflateMenu(R.menu.menu_edit_grade);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_save) {
                checkMaDeAndConfirm();
                return true;
            }
            return false;
        });

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        pager = view.findViewById(R.id.view_pager);

        viewModel = new ViewModelProvider(this).get(XemLaiViewModel.class);
        long gradeId = requireArguments().getLong("gradeId", -1L);
        viewModel.getGradeResultById(gradeId).observe(getViewLifecycleOwner(), gr -> {
            if (gr == null) return;
            current = gr;
            setupPager(tabLayout, pager);
            // Hiển thị điểm cũ
            updateToolbarScore(current.correctCount, current.totalQuestions, current.score);
        });
    }

    private void setupPager(TabLayout tabLayout, ViewPager2 pager) {
        FragmentStateAdapter adapter = new FragmentStateAdapter(this) {
            @Override public int getItemCount() { return 3; }
            @NonNull @Override
            public Fragment createFragment(int pos) {
                switch (pos) {
                    case 0:
                        return EditMaDeTabFragment.newInstance(current.maDe);
                    case 1:
                        return EditTabSbdFragment.newInstance(current.sbd);
                    default:
                        EditDapAnTabFragment f = EditDapAnTabFragment
                                .newInstance(current.totalQuestions,
                                        current.answersCsv != null
                                                ? current.answersCsv
                                                : "");
                        // Đăng ký listener
                        f.setOnAnswerChangeListener(EditGradeFragment.this);
                        return f;
                }
            }
        };
        pager.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, pager,
                (tab, pos) -> {
                    if (pos == 0)      tab.setText("MÃ ĐỀ");
                    else if (pos == 1) tab.setText("SỐ BÁO DANH");
                    else               tab.setText("ĐÁP ÁN");
                }
        ).attach();
    }

    /** Callback từ EditDapAnTabFragment mỗi khi user chọn đáp án */
    @Override
    public void onAnswersChanged(int[] selectedAnswers) {
        new Thread(() -> {
            // load đáp án đúng
            AnswerDao dao = AppDatabase.getInstance(requireContext()).answerDao();
            List<Answer> answers = dao.getAnswersByExamAndCodeSync(
                    current.examId, current.maDe);

            // build map số câu → đáp án đúng
            Map<Integer, String> correctMap = new HashMap<>();
            for (Answer a : answers) correctMap.put(a.cauSo, a.dapAn);

            // tính số đúng
            int totalQ = selectedAnswers.length;
            int correctCnt = 0;
            for (int i = 0; i < totalQ; i++) {
                String pick = selectedAnswers[i] == 1 ? "A"
                        : selectedAnswers[i] == 2 ? "B"
                        : selectedAnswers[i] == 3 ? "C"
                        : selectedAnswers[i] == 4 ? "D"
                        : "X";
                String truth = correctMap.get(i + 1);
                if (truth != null && truth.equals(pick)) correctCnt++;
            }
            double newScore = ((double) correctCnt / totalQ) * 10.0;

            // Copy vào biến final để dùng trong lambda
            final int   finalCorrectCnt = correctCnt;
            final int   finalTotalQ     = totalQ;
            final double finalNewScore  = newScore;

            requireActivity().runOnUiThread(() ->
                    updateToolbarScore(finalCorrectCnt, finalTotalQ, finalNewScore)
            );
        }).start();
    }


    private void updateToolbarScore(int correct, int total, double score) {
        toolbar.setSubtitle(String.format("Đúng %d/%d = %.2f", correct, total, score));
    }

    // --- Logic kiểm tra mã đề & confirm giữ nguyên ---
    private void checkMaDeAndConfirm() {
        EditMaDeTabFragment f0 = null;
        EditTabSbdFragment  f1 = null;
        EditDapAnTabFragment f2 = null;
        for (Fragment frag : getChildFragmentManager().getFragments()) {
            if      (frag instanceof EditMaDeTabFragment)      f0 = (EditMaDeTabFragment) frag;
            else if (frag instanceof EditTabSbdFragment)       f1 = (EditTabSbdFragment) frag;
            else if (frag instanceof EditDapAnTabFragment)     f2 = (EditDapAnTabFragment) frag;
        }

        final String newMaDe = f0 != null ? f0.getMaDe() : current.maDe;
        final String newSbd  = f1 != null ? f1.getSoBaoDanh() : current.sbd;

        int[] ansArr = f2 != null ? f2.getSelectedAnswers() : new int[0];
        final List<String> parts = new ArrayList<>();
        for (int v : ansArr) {
            parts.add(v==1?"A":v==2?"B":v==3?"C":v==4?"D":"X");
        }
        final String newCsv = TextUtils.join(",", parts);

        new Thread(() -> {
            AnswerDao dao = AppDatabase.getInstance(requireContext()).answerDao();
            List<Answer> answers = dao.getAnswersByExamAndCodeSync(current.examId, newMaDe);
            requireActivity().runOnUiThread(() -> {
                if (answers == null || answers.isEmpty()) {
                    Toast.makeText(requireContext(),
                            "Mã đề “" + newMaDe + "” không tồn tại",
                            Toast.LENGTH_LONG).show();
                } else {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Xác nhận lưu")
                            .setMessage("Bạn có chắc muốn lưu thay đổi không?")
                            .setPositiveButton("Có", (d,w) ->
                                    doSave(newMaDe, newSbd, newCsv, answers))
                            .setNegativeButton("Hủy", null)
                            .show();
                }
            });
        }).start();
    }

    private void doSave(String newMaDe, String newSbd, String newCsv, List<Answer> correctList) {
        new Thread(() -> {
            Map<Integer,String> correctMap = new HashMap<>();
            for (Answer a : correctList) correctMap.put(a.cauSo, a.dapAn);

            String[] arr = newCsv.split("\\s*,\\s*");
            int totalQ = arr.length, newCorrect = 0;
            for (int i = 0; i < totalQ; i++) {
                String pick  = arr[i];
                String truth = correctMap.get(i+1);
                if (truth != null && truth.equals(pick)) newCorrect++;
            }
            double newScore = ((double)newCorrect / totalQ) * 10.0;

            current.maDe         = newMaDe;
            current.sbd          = newSbd;
            current.answersCsv   = newCsv;
            current.correctCount = newCorrect;
            current.score        = newScore;
            viewModel.updateResult(current);

            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(),
                        "Đã lưu thay đổi", Toast.LENGTH_SHORT).show();
                requireActivity().onBackPressed();
            });
        }).start();
    }
}
