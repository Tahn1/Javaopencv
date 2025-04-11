package com.example.javaopencv.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;

import java.util.ArrayList;
import java.util.List;

public class MaDeTabFragment extends Fragment {

    private RecyclerView rvColumn1, rvColumn2, rvColumn3;
    private SingleColumnAdapter adapterCol1, adapterCol2, adapterCol3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ma_de_tab, container, false);

        rvColumn1 = view.findViewById(R.id.rv_column_1);
        rvColumn2 = view.findViewById(R.id.rv_column_2);
        rvColumn3 = view.findViewById(R.id.rv_column_3);

        // Tạo danh sách số từ 0 đến 9
        List<String> digits = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            digits.add(String.valueOf(i));
        }

        // Thiết lập RecyclerView cho mỗi cột
        // Dùng LinearLayoutManager dọc, vì mỗi cột chỉ có 10 item.
        rvColumn1.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapterCol1 = new SingleColumnAdapter(digits);
        rvColumn1.setAdapter(adapterCol1);

        rvColumn2.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapterCol2 = new SingleColumnAdapter(digits);
        rvColumn2.setAdapter(adapterCol2);

        rvColumn3.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapterCol3 = new SingleColumnAdapter(digits);
        rvColumn3.setAdapter(adapterCol3);

        return view;
    }

    // Phương thức trả về mã đề ghép từ các cột được chọn
    public String getMaDe() {
        String d1 = adapterCol1.getSelectedDigit();
        String d2 = adapterCol2.getSelectedDigit();
        String d3 = adapterCol3.getSelectedDigit();

        // Kiểm tra nếu chưa chọn đủ
        if (d1 == null || d2 == null || d3 == null) {
            return null;
        }

        // Ghép thành mã đề: ví dụ "d1d2d3"
        return d1 + d2 + d3;
    }
}
