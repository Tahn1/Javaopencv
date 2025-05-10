package com.example.javaopencv.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.javaopencv.R;
import com.example.javaopencv.data.AppDatabase;
import com.example.javaopencv.data.dao.AnswerDao;
import com.example.javaopencv.data.entity.Answer;
import com.example.javaopencv.data.entity.GradeResult;
import com.example.javaopencv.ui.EditDapAnTabFragment.OnAnswerChangeListener;
import com.example.javaopencv.viewmodel.GradeResultViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EditGradeFragment extends Fragment implements OnAnswerChangeListener {
    private static final String[] TITLES = {"MÃ ĐỀ", "SỐ BÁO DANH", "ĐÁP ÁN"};

    private ViewPager2 pager;
    private TabLayout tabs;
    private GradeResultViewModel viewModel;
    private GradeResult current;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        NavHostFragment.findNavController(EditGradeFragment.this).navigateUp();
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_grade_tabs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppCompatActivity act = (AppCompatActivity) requireActivity();
        ActionBar ab = act.getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle("Chỉnh sửa kết quả");
        }

        // Menu Save
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
                inflater.inflate(R.menu.menu_edit_grade, menu);
            }
            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == android.R.id.home) {
                    NavHostFragment.findNavController(EditGradeFragment.this).navigateUp();
                    return true;
                } else if (item.getItemId() == R.id.action_save) {
                    confirmAndSave();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        // Tabs & Pager
        tabs = view.findViewById(R.id.tab_layout);
        pager = view.findViewById(R.id.view_pager);
        pager.setOffscreenPageLimit(TITLES.length);

        long gradeId = requireArguments().getLong("gradeId", -1L);
        viewModel = new ViewModelProvider(
                this,
                new GradeResultViewModel.Factory(requireActivity().getApplication(), gradeId)
        ).get(GradeResultViewModel.class);

        viewModel.getGradeResultById().observe(getViewLifecycleOwner(), gr -> {
            if (gr == null) return;
            current = gr;
            if (ab != null) {
                ab.setSubtitle(String.format(Locale.getDefault(),
                        "Đúng %d/%d = %.2f", gr.getCorrectCount(), gr.getTotalQuestions(), gr.getScore()));
            }
            pager.setAdapter(new RealAdapter(this, current));
            new TabLayoutMediator(tabs, pager,
                    (tab, pos) -> tab.setText(TITLES[pos])
            ).attach();
        });
    }

    @Override
    public void onAnswersChanged(int[] selected) {
        // no-op
    }

    private void confirmAndSave() {
        EditMaDeTabFragment f0 = (EditMaDeTabFragment) getChildFragmentManager().getFragments().get(0);
        EditTabSbdFragment f1 = (EditTabSbdFragment) getChildFragmentManager().getFragments().get(1);
        EditDapAnTabFragment f2 = (EditDapAnTabFragment) getChildFragmentManager().getFragments().get(2);
        String newMaDe = f0.getMaDe();
        String newSbd = f1.getSoBaoDanh();
        int[] sel = f2.getSelectedAnswers();
        List<String> parts = new ArrayList<>();
        for (int v : sel) parts.add(v==1?"A":v==2?"B":v==3?"C":v==4?"D":"X");
        String newCsv = TextUtils.join(",", parts);

        new Thread(() -> {
            List<Answer> correctList = AppDatabase
                    .getInstance(requireContext())
                    .answerDao()
                    .getAnswersByExamAndCodeSync(current.getExamId(), newMaDe);
            requireActivity().runOnUiThread(() -> {
                if (correctList == null || correctList.isEmpty()) {
                    Toast.makeText(requireContext(),
                            String.format(Locale.getDefault(), "Mã đề \"%s\" không tồn tại", newMaDe),
                            Toast.LENGTH_LONG).show();
                    return;
                }
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Xác nhận lưu")
                        .setMessage("Bạn có chắc muốn lưu thay đổi không?")
                        .setNegativeButton("Hủy", null)
                        .setPositiveButton("Có", (dialog, which) -> {
                            current.setSbd(newSbd);
                            viewModel.checkDuplicateAndUpdate(current, isDup -> {
                                if (isDup) {
                                    Toast.makeText(requireContext(),
                                            String.format(Locale.getDefault(), "Mã số \"%s\" đã tồn tại!", newSbd),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    current.setMaDe(newMaDe);
                                    current.setAnswersCsv(newCsv);
                                    Map<Integer,String> map = new HashMap<>();
                                    for (Answer a : correctList) map.put(a.cauSo, a.dapAn);
                                    int cnt = 0;
                                    for (int i = 0; i < sel.length; i++) {
                                        if (parts.get(i).equals(map.get(i+1))) cnt++;
                                    }
                                    current.setCorrectCount(cnt);
                                    current.setScore(cnt * 10.0 / sel.length);
                                    viewModel.updateGradeResult(current);
                                    Toast.makeText(requireContext(), "Đã lưu thay đổi", Toast.LENGTH_SHORT).show();
                                    NavHostFragment.findNavController(EditGradeFragment.this).navigateUp();
                                }
                            });
                        })
                        .show();
            });
        }).start();
    }

    private static class RealAdapter extends FragmentStateAdapter {
        private final GradeResult cur;
        private final OnAnswerChangeListener listener;
        RealAdapter(@NonNull Fragment parent, GradeResult cur) {
            super(parent);
            this.cur = cur;
            this.listener = (OnAnswerChangeListener) parent;
        }
        @Override public int getItemCount() { return TITLES.length; }
        @NonNull @Override
        public Fragment createFragment(int pos) {
            if (pos == 0) return EditMaDeTabFragment.newInstance(cur.getMaDe());
            if (pos == 1) return EditTabSbdFragment.newInstance(cur.getSbd());
            EditDapAnTabFragment f = EditDapAnTabFragment
                    .newInstance(cur.getTotalQuestions(), cur.getAnswersCsv());
            f.setOnAnswerChangeListener(listener);
            return f;
        }
    }
}
