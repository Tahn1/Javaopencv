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
    private int questionCount; // Số lượng câu hỏi
    private List<String> answerListToEdit; // ✅ Danh sách đáp án để phục hồi nếu chỉnh sửa

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dap_an_tab, container, false);
        rvDapAnGrid = view.findViewById(R.id.rv_dap_an_grid);

        // ✅ Lấy số câu hỏi từ Bundle (nếu có)
        if (getArguments() != null && getArguments().containsKey("questionCount")) {
            questionCount = getArguments().getInt("questionCount");
            Log.d("DapAnTabFragment", "questionCount = " + questionCount);
        } else {
            questionCount = 20; // Giá trị mặc định
            Log.e("DapAnTabFragment", "No questionCount provided, defaulting to 20");
        }

        // Thiết lập GridLayout 5 cột
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 5);
        rvDapAnGrid.setLayoutManager(layoutManager);

        // Khởi tạo danh sách item (tổng số item = questionCount * 5)
        List<Integer> itemList = new ArrayList<>();
        int totalItems = questionCount * 5;
        for (int i = 0; i < totalItems; i++) {
            itemList.add(i);
        }

        adapter = new DapAnGridAdapter(itemList, questionCount);
        rvDapAnGrid.setAdapter(adapter);

        // ✅ Nếu đã có đáp án cũ thì set vào adapter để tự động highlight lại
        if (answerListToEdit != null && !answerListToEdit.isEmpty()) {
            adapter.setSelectedAnswers(answerListToEdit);
        }

        return view;
    }

    // ✅ Lấy danh sách đáp án hiện tại
    public List<String> getAnswerList() {
        if (adapter != null) {
            return adapter.buildAnswersList();
        }
        return new ArrayList<>();
    }

    // ✅ NEW: Truyền đáp án cũ từ bên ngoài vào fragment này để highlight lại
    public void setAnswerListToEdit(List<String> answerList) {
        this.answerListToEdit = answerList;
        if (adapter != null && answerList != null) {
            adapter.setSelectedAnswers(answerList);
        }
    }
}
