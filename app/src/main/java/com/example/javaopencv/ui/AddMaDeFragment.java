package com.example.javaopencv.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
    private DapAnViewModel viewModel;

    private String maDeToEdit;
    private int positionToEdit;
    private List<String> oldAnswerList;
    private int questionCount = 20;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_ma_de, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1) ViewPager2 + Adapter
        viewPager = view.findViewById(R.id.view_pager);
        viewPagerAdapter = new MaDeViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        // 2) TabLayout + ViewPager2
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, pos) -> tab.setText(pos == 0 ? "Mã đề" : "Đáp án")
        ).attach();

        // 3) ViewModel + args
        viewModel = new ViewModelProvider(requireActivity())
                .get(DapAnViewModel.class);

        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey("examId")) {
                viewModel.setExamId(args.getInt("examId"));
            }
            if (args.containsKey("questionCount")) {
                questionCount = args.getInt("questionCount");
                Log.d("AddMaDeFragment", "Question count: " + questionCount);
            }
            maDeToEdit = args.getString("maDeToEdit");
            positionToEdit = args.getInt("positionToEdit", -1);
            oldAnswerList = args.getStringArrayList("oldAnswerList");
        }

        // 4) Thiết lập lại trạng thái cho 2 tab con
        viewPager.post(() -> {
            MaDeTabFragment maDeTab = viewPagerAdapter.getMaDeTabFragment();
            DapAnTabFragment dapAnTab = viewPagerAdapter.getDapAnTabFragment();

            if (maDeTab != null && maDeToEdit != null) {
                maDeTab.setSelectedMaDe(maDeToEdit);
            }
            if (dapAnTab != null) {
                dapAnTab.setQuestionCount(questionCount);
                if (oldAnswerList != null) {
                    dapAnTab.setAnswerListToEdit(oldAnswerList);
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu,
                                    @NonNull MenuInflater inflater) {

        inflater.inflate(R.menu.menu_add_ma_de, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            saveMaDe();
            return true;
        }
        if (id == android.R.id.home) {
            requireActivity().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveMaDe() {
        MaDeTabFragment maDeTab = viewPagerAdapter.getMaDeTabFragment();
        DapAnTabFragment dapAnTab = viewPagerAdapter.getDapAnTabFragment();
        if (maDeTab == null || dapAnTab == null) return;

        String maDe = maDeTab.getMaDe();
        List<String> answers = dapAnTab.getAnswerList();
        if (maDe == null || maDe.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Vui lòng nhập mã đề", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> finalAnswers = new ArrayList<>();
        for (int i = 0; i < questionCount; i++) {
            finalAnswers.add(i < answers.size() ? answers.get(i) : null);
        }

        List<DapAnViewModel.MaDeItem> currentList = viewModel.getMaDeList().getValue();
        boolean isEdit = maDeToEdit != null && positionToEdit != -1;

        if (currentList != null) {
            for (int i = 0; i < currentList.size(); i++) {
                String existing = currentList.get(i).code;
                if (existing.equals(maDe) && (!isEdit || i != positionToEdit)) {
                    Toast.makeText(requireContext(),
                            "Mã đề đã tồn tại. Vui lòng nhập mã đề khác.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        if (isEdit) {

            new AlertDialog.Builder(requireContext())
                    .setTitle("Xác nhận")
                    .setMessage("Bạn chắc chắn muốn cập nhật mã đề không?")
                    .setPositiveButton("Có", (d, w) -> {
                        viewModel.updateMaDe(positionToEdit, maDe, finalAnswers, questionCount);
                        requireActivity().onBackPressed();
                    })
                    .setNegativeButton("Không", null)
                    .show();
        } else {
            viewModel.addMaDe(maDe, finalAnswers, questionCount);
            requireActivity().onBackPressed();
        }
    }
}