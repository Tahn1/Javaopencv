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
import java.util.ArrayList;
import java.util.List;

public class DapAnTabFragment extends Fragment {

    private RecyclerView rvDapAnGrid;
    private DapAnGridAdapter adapter;
    private int questionCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dap_an_tab, container, false);
        rvDapAnGrid = view.findViewById(R.id.rv_dap_an_grid);

        // Nhận questionCount từ Bundle
        if (getArguments() != null && getArguments().containsKey("questionCount")) {
            questionCount = getArguments().getInt("questionCount");
            Log.d("DapAnTabFragment", "questionCount = " + questionCount);
        } else {
            questionCount = 10; // Giá trị mặc định nếu thiếu
            Log.e("DapAnTabFragment", "No questionCount provided; defaulting to 10");
        }

        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 5);
        rvDapAnGrid.setLayoutManager(layoutManager);

        List<Integer> itemList = new ArrayList<>();
        int totalItems = questionCount * 5;
        for (int i = 0; i < totalItems; i++) {
            itemList.add(i);
        }

        adapter = new DapAnGridAdapter(itemList, questionCount);
        rvDapAnGrid.setAdapter(adapter);

        return view;
    }

    // Đây là phương thức được gọi từ AddMaDeFragment để thu thập đáp án dưới dạng chuỗi
    public String buildAnswersListString() {
        List<String> answersList = adapter.buildAnswersList();
        StringBuilder sb = new StringBuilder();
        for (String ans : answersList) {
            if (ans != null) {
                sb.append(ans);
            } else {
                sb.append("-");
            }
        }
        return sb.toString();
    }
}
