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
    // Số câu hỏi – mặc định 20, sẽ được cập nhật qua setter
    private int questionCount = 20;
    private List<String> answerListToEdit; // Danh sách đáp án cũ được dùng để phục hồi khi chỉnh sửa

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dap_an_tab, container, false);
        rvDapAnGrid = view.findViewById(R.id.rv_dap_an_grid);

        // Sử dụng giá trị questionCount hiện tại
        Log.d("DapAnTabFragment", "onCreateView: using questionCount = " + questionCount);
        setupGrid();

        // Nếu có danh sách đáp án cũ, cập nhật để highlight
        if (answerListToEdit != null && !answerListToEdit.isEmpty()) {
            Log.d("DapAnTabFragment", "onCreateView: setting old answer list: " + answerListToEdit);
            adapter.setSelectedAnswers(answerListToEdit);
        }
        return view;
    }

    private void setupGrid() {
        // Mặc định 5 cột: 1 cột STT + 4 cột A/B/C/D
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 5);
        rvDapAnGrid.setLayoutManager(layoutManager);

        // Tạo danh sách item (questionCount * 5)
        List<Integer> itemList = new ArrayList<>();
        int totalItems = questionCount * 5;
        for (int i = 0; i < totalItems; i++) {
            itemList.add(i);
        }

        // Khởi tạo adapter
        adapter = new DapAnGridAdapter(itemList, questionCount);
        rvDapAnGrid.setAdapter(adapter);
    }

    // Trả về danh sách đáp án hiện tại (luôn độ dài = questionCount)
    public List<String> getAnswerList() {
        return (adapter != null) ? adapter.buildAnswersList() : new ArrayList<>();
    }

    // Gán danh sách đáp án cũ (để highlight lại)
    public void setAnswerListToEdit(List<String> answerList) {
        this.answerListToEdit = answerList;
        if (answerList == null || answerList.isEmpty()) {
            Log.d("DapAnTabFragment", "setAnswerListToEdit: received an empty list.");
            return;
        }
        Log.d("DapAnTabFragment", "setAnswerListToEdit: received list = " + answerList);
        if (adapter != null) {
            adapter.setSelectedAnswers(answerList);
        }
    }

    // Setter cập nhật số câu
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
                // Cập nhật adapter
                adapter.updateData(newItemList, questionCount);
                // Sau khi cập nhật, nếu có answerListToEdit => set highlight lại
                if (answerListToEdit != null && !answerListToEdit.isEmpty()) {
                    Log.d("DapAnTabFragment", "setQuestionCount: re-setting old answers: " + answerListToEdit);
                    adapter.setSelectedAnswers(answerListToEdit);
                }
            } else {
                adapter = new DapAnGridAdapter(newItemList, questionCount);
                rvDapAnGrid.setAdapter(adapter);
            }
        }
    }
}
