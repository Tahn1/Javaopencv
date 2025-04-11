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
    private int questionCount; // Số câu nhận được qua Bundle

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dap_an_tab, container, false);
        rvDapAnGrid = view.findViewById(R.id.rv_dap_an_grid);

        // Nếu muốn kiểm tra dữ liệu, bạn có thể dùng đoạn sau
//        Bundle args = getArguments();
//        if (args == null || !args.containsKey("questionCount")) {
//            throw new IllegalArgumentException("Missing questionCount in arguments");
//        }
//        questionCount = args.getInt("questionCount");
//        Log.d("DapAnTabFragment", "questionCount = " + questionCount);

        // Ví dụ: nếu bạn muốn hiển thị dữ liệu dựa trên số câu, bạn có thể cài đặt tương ứng ở đây.
        // Nếu không, bạn có thể sử dụng một giá trị mặc định hoặc cấu hình khác.
        // Ở đây tôi giữ nguyên biến questionCount (bạn có thể truyền từ Bundle nếu cần)

        // Ví dụ, nếu bạn muốn lấy dữ liệu từ arguments:
        if (getArguments() != null && getArguments().containsKey("questionCount")) {
            questionCount = getArguments().getInt("questionCount");
            Log.d("DapAnTabFragment", "questionCount = " + questionCount);
        } else {
            // Nếu không có, bạn có thể đặt mặc định nếu muốn
            questionCount = 0;
            Log.e("DapAnTabFragment", "No questionCount provided");
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
}
