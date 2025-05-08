package com.example.javaopencv.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditGradeFragment extends Fragment implements OnAnswerChangeListener {
    private static final String[] TITLES = {"MÃ ĐỀ", "SỐ BÁO DANH", "ĐÁP ÁN"};

    private ViewPager2      pager;
    private TabLayout       tabs;
    private XemLaiViewModel viewModel;
    private GradeResult     current;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Cho phép fragment sử dụng menu
        setHasOptionsMenu(true);
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate layout chỉ chứa TabLayout + ViewPager2
        return inflater.inflate(R.layout.fragment_edit_grade_tabs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Thiết lập ActionBar chung của Activity
        AppCompatActivity act = (AppCompatActivity) requireActivity();
        ActionBar ab = act.getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle("Chỉnh sửa kết quả");
        }

        tabs = view.findViewById(R.id.tab_layout);
        pager = view.findViewById(R.id.view_pager);

        // Adapter tạm để hiển thị tab ngay
        pager.setAdapter(new FragmentStateAdapter(this) {
            @Override public int getItemCount() { return TITLES.length; }
            @NonNull @Override
            public Fragment createFragment(int pos) {
                if (pos == 0) return EditMaDeTabFragment.newInstance("");
                if (pos == 1) return EditTabSbdFragment.newInstance("");
                return EditDapAnTabFragment.newInstance(0, "");
            }
        });
        new TabLayoutMediator(tabs, pager,
                (tab, pos) -> tab.setText(TITLES[pos])
        ).attach();
        // Ép màu chắc chắn
        tabs.setBackgroundColor(requireContext().getColor(R.color.purple_500));
        tabs.setTabTextColors(requireContext().getColor(R.color.white_50),
                requireContext().getColor(R.color.white));
        tabs.setSelectedTabIndicatorColor(requireContext().getColor(R.color.white));

        // Load GradeResult
        viewModel = new ViewModelProvider(this).get(XemLaiViewModel.class);
        long gradeId = requireArguments().getLong("gradeId", -1L);
        viewModel.getGradeResultById(gradeId)
                .observe(getViewLifecycleOwner(), gr -> {
                    if (gr == null) return;
                    current = gr;
                    // cập nhật subtitle ActionBar
                    if (ab != null) {
                        ab.setSubtitle(
                                String.format("Đúng %d/%d = %.2f",
                                        gr.correctCount, gr.totalQuestions, gr.score)
                        );
                    }
                    // set adapter thật
                    pager.setAdapter(new RealAdapter(this, current));
                    // re-attach mediator để cập nhật text
                    new TabLayoutMediator(tabs, pager,
                            (tab, pos) -> tab.setText(TITLES[pos])
                    ).attach();
                });
    }

    // Inflate menu_save trên AppBar
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_edit_grade, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // Bắt sự kiện khi nhấn Save
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            confirmAndSave();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Adapter thật khi có data
    private static class RealAdapter extends FragmentStateAdapter {
        private final GradeResult cur;
        private final OnAnswerChangeListener listener;

        RealAdapter(EditGradeFragment parent, GradeResult cur) {
            super(parent);
            this.cur = cur;
            this.listener = parent;
        }

        @Override public int getItemCount() { return TITLES.length; }

        @NonNull @Override
        public Fragment createFragment(int pos) {
            if (pos == 0) {
                return EditMaDeTabFragment.newInstance(cur.maDe);
            } else if (pos == 1) {
                return EditTabSbdFragment.newInstance(cur.sbd);
            } else {
                EditDapAnTabFragment f = EditDapAnTabFragment
                        .newInstance(cur.totalQuestions,
                                cur.answersCsv != null ? cur.answersCsv : "");
                f.setOnAnswerChangeListener(listener);
                return f;
            }
        }
    }

    /** Callback khi user thay đổi đáp án */
    @Override
    public void onAnswersChanged(int[] selected) {
        new Thread(() -> {
            AnswerDao dao = AppDatabase.getInstance(requireContext()).answerDao();
            List<Answer> correctList = dao.getAnswersByExamAndCodeSync(
                    current.examId, current.maDe);
            Map<Integer,String> map = new HashMap<>();
            for (Answer a : correctList) map.put(a.cauSo, a.dapAn);

            int cnt = 0;
            for (int i = 0; i < selected.length; i++) {
                String pick;
                switch (selected[i]) {
                    case 1: pick = "A"; break;
                    case 2: pick = "B"; break;
                    case 3: pick = "C"; break;
                    case 4: pick = "D"; break;
                    default: pick = "X"; break;
                }
                if (pick.equals(map.get(i+1))) cnt++;
            }
            final int correct = cnt, total = selected.length;
            final double score = ((double)correct/total)*10.0;

            requireActivity().runOnUiThread(() -> {
                AppCompatActivity act = (AppCompatActivity) requireActivity();
                ActionBar ab = act.getSupportActionBar();
                if (ab != null) {
                    ab.setSubtitle(
                            String.format("Đúng %d/%d = %.2f", correct, total, score)
                    );
                }
            });
        }).start();
    }
    private void confirmAndSave() {
        List<Fragment> frags = getChildFragmentManager().getFragments();
        EditMaDeTabFragment f0 = (EditMaDeTabFragment) frags.get(0);
        EditTabSbdFragment   f1 = (EditTabSbdFragment)   frags.get(1);
        EditDapAnTabFragment f2 = (EditDapAnTabFragment) frags.get(2);

        final String newMaDe = f0.getMaDe();
        final String newSbd  = f1.getSoBaoDanh();
        int[] sel            = f2.getSelectedAnswers();
        List<String> parts   = new ArrayList<>();
        for (int v : sel) {
            switch (v) {
                case 1: parts.add("A"); break;
                case 2: parts.add("B"); break;
                case 3: parts.add("C"); break;
                case 4: parts.add("D"); break;
                default: parts.add("X"); break;
            }
        }
        final String newCsv = TextUtils.join(",", parts);

        new Thread(() -> {
            AnswerDao dao = AppDatabase.getInstance(requireContext()).answerDao();
            List<Answer> correctList = dao.getAnswersByExamAndCodeSync(
                    current.examId, newMaDe
            );
            requireActivity().runOnUiThread(() -> {
                if (correctList == null || correctList.isEmpty()) {
                    Toast.makeText(requireContext(),
                            "Mã đề “" + newMaDe + "” không tồn tại",
                            Toast.LENGTH_LONG).show();
                } else {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Xác nhận lưu")
                            .setMessage("Bạn có chắc muốn lưu thay đổi không?")
                            .setPositiveButton("Có", (d,w) ->
                                    doSave(newMaDe, newSbd, newCsv, correctList))
                            .setNegativeButton("Hủy", null)
                            .show();
                }
            });
        }).start();
    }

    /** Thực tế lưu vào DB */
    private void doSave(String maDe, String sbd, String csv, List<Answer> correctList) {
        new Thread(() -> {
            Map<Integer,String> map = new HashMap<>();
            for (Answer a : correctList) map.put(a.cauSo, a.dapAn);

            String[] arr = csv.split(",");
            int cnt=0;
            for (int i=0; i<arr.length; i++) {
                if (arr[i].equals(map.get(i+1))) cnt++;
            }
            double newScore = ((double)cnt/arr.length)*10.0;

            current.maDe         = maDe;
            current.sbd          = sbd;
            current.answersCsv   = csv;
            current.correctCount = cnt;
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
