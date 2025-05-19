package com.example.javaopencv.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;
import com.example.javaopencv.ui.adapter.DapAnGridAdapter;

import java.util.ArrayList;
import java.util.List;

public class DapAnTabFragment extends Fragment {

    private RecyclerView rvDapAnGrid;
    private DapAnGridAdapter adapter;
    private List<String> answerListToEdit;
    private int questionCount = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dap_an_tab, container, false);
        rvDapAnGrid = view.findViewById(R.id.rv_dap_an_grid);

        if (questionCount > 0) {
            setupGrid(questionCount);
            // highlight old answers nếu có
            if (answerListToEdit != null && !answerListToEdit.isEmpty()) {
                adapter.setSelectedAnswers(answerListToEdit);
            }
        }
        return view;
    }


    public void setQuestionCount(int count) {
        this.questionCount = count;
        // nếu view đã tạo, vẽ luôn
        if (rvDapAnGrid != null) {
            setupGrid(count);
            if (answerListToEdit != null && !answerListToEdit.isEmpty()) {
                adapter.setSelectedAnswers(answerListToEdit);
            }
        }
    }


    public void setAnswerListToEdit(List<String> answers) {
        this.answerListToEdit = answers;
        if (adapter != null && answers != null && !answers.isEmpty()) {
            adapter.setSelectedAnswers(answers);
        }
    }


    private void setupGrid(int count) {
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 5);
        rvDapAnGrid.setLayoutManager(layoutManager);

        List<Integer> items = new ArrayList<>(count * 5);
        for (int i = 0; i < count * 5; i++) {
            items.add(i);
        }

        adapter = new DapAnGridAdapter(items, count);
        rvDapAnGrid.setAdapter(adapter);
    }


    public List<String> getAnswerList() {
        return (adapter != null) ? adapter.buildAnswersList() : new ArrayList<>();
    }
}
