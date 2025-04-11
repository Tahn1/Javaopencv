package com.example.javaopencv.ui;

import android.os.Bundle;
import android.util.Log;
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
    private int questionCount;
    private String code; // Ví dụ: "936"

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ma_de_tab, container, false);

        rvColumn1 = view.findViewById(R.id.rv_column_1);
        rvColumn2 = view.findViewById(R.id.rv_column_2);
        rvColumn3 = view.findViewById(R.id.rv_column_3);

        // Lấy dữ liệu từ Bundle
        Bundle args = getArguments();
        if (args != null) {
            questionCount = args.getInt("questionCount", 0);
            code = args.getString("code", "");
            Log.d("MaDeTabFragment", "questionCount = " + questionCount + ", code = " + code);
        }

        // Tạo danh sách số từ 0 đến 9 cho mỗi cột
        List<String> digits = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            digits.add(String.valueOf(i));
        }

        rvColumn1.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapterCol1 = new SingleColumnAdapter(digits);
        rvColumn1.setAdapter(adapterCol1);

        rvColumn2.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapterCol2 = new SingleColumnAdapter(digits);
        rvColumn2.setAdapter(adapterCol2);

        rvColumn3.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapterCol3 = new SingleColumnAdapter(digits);
        rvColumn3.setAdapter(adapterCol3);

        // Nếu có code đã lưu, khôi phục lựa chọn cho mỗi cột (giả sử code luôn có 3 chữ số)
        if (code != null && code.length() >= 3) {
            adapterCol1.setSelectedDigit(String.valueOf(code.charAt(0)));
            adapterCol2.setSelectedDigit(String.valueOf(code.charAt(1)));
            adapterCol3.setSelectedDigit(String.valueOf(code.charAt(2)));
        }

        return view;
    }

    public String getMaDe() {
        String d1 = adapterCol1.getSelectedDigit();
        String d2 = adapterCol2.getSelectedDigit();
        String d3 = adapterCol3.getSelectedDigit();
        if (d1 == null || d2 == null || d3 == null)
            return "";
        return d1 + d2 + d3;
    }
}
