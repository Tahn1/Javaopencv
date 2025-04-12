package com.example.javaopencv.ui;

import android.os.Bundle;
import android.util.Log;
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
    // Số câu hỏi – mặc định 20; sẽ được cập nhật thông qua setter từ màn hình cha
    private int questionCount = 20;
    private List<String> answerListToEdit; // Danh sách đáp án cũ dùng để phục hồi khi chỉnh sửa

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dap_an_tab, container, false);
        rvDapAnGrid = view.findViewById(R.id.rv_dap_an_grid);

        // Không đọc questionCount từ Bundle; chúng ta dựa vào giá trị được setter cập nhật
        Log.d("DapAnTabFragment", "Using questionCount = " + questionCount);

        setupGrid();

        // Nếu có danh sách đáp án cũ, set lại (adapter sẽ tự highlight lại)
        if (answerListToEdit != null && !answerListToEdit.isEmpty() && adapter != null) {
            adapter.setSelectedAnswers(answerListToEdit);
        }

        return view;
    }

    private void setupGrid() {
        // Thiết lập GridLayout 5 cột
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 5);
        rvDapAnGrid.setLayoutManager(layoutManager);

        // Tạo danh sách item dựa trên questionCount
        List<Integer> itemList = new ArrayList<>();
        int totalItems = questionCount * 5;
        for (int i = 0; i < totalItems; i++) {
            itemList.add(i);
        }

        adapter = new DapAnGridAdapter(itemList, questionCount);
        rvDapAnGrid.setAdapter(adapter);
    }

    // Lấy danh sách đáp án từ adapter
    public List<String> getAnswerList() {
        if (adapter != null) {
            return adapter.buildAnswersList();
        }
        return new ArrayList<>();
    }

    // Dùng để phục hồi danh sách đáp án cũ
    public void setAnswerListToEdit(List<String> answerList) {
        this.answerListToEdit = answerList;
        if (adapter != null && answerList != null) {
            adapter.setSelectedAnswers(answerList);
        }
    }

    // Setter cập nhật số câu hỏi, tái tạo lại adapter với số hàng = questionCount * 5
    public void setQuestionCount(int newQuestionCount) {
        this.questionCount = newQuestionCount;
        Log.d("DapAnTabFragment", "setQuestionCount called, new count = " + questionCount);
        if (rvDapAnGrid != null) {
            List<Integer> newItemList = new ArrayList<>();
            int totalItems = questionCount * 5;
            for (int i = 0; i < totalItems; i++) {
                newItemList.add(i);
            }
            if (adapter != null) {
                adapter.updateData(newItemList, questionCount);
                // Nếu có đáp án cũ, set lại
                if (answerListToEdit != null && !answerListToEdit.isEmpty()) {
                    adapter.setSelectedAnswers(answerListToEdit);
                }
            } else {
                adapter = new DapAnGridAdapter(newItemList, questionCount);
                rvDapAnGrid.setAdapter(adapter);
            }
        }
    }
}
