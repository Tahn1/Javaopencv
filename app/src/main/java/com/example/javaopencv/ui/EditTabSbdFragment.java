package com.example.javaopencv.ui;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.javaopencv.R;

public class EditTabSbdFragment extends Fragment {

    private GridLayout col1, col2, col3, col4, col5, col6, col7;
    private final int[] selected = new int[8]; // chỉ dùng từ 2..7

    public static EditTabSbdFragment newInstance(String sbd) {
        Bundle b = new Bundle();
        b.putString("sbd", sbd);
        EditTabSbdFragment f = new EditTabSbdFragment();
        f.setArguments(b);
        return f;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inf.inflate(R.layout.fragment_edit_tab_sbd, container, false);
        col1 = v.findViewById(R.id.grid_sbd_col1);
        col2 = v.findViewById(R.id.grid_sbd_col2);
        col3 = v.findViewById(R.id.grid_sbd_col3);
        col4 = v.findViewById(R.id.grid_sbd_col4);
        col5 = v.findViewById(R.id.grid_sbd_col5);
        col6 = v.findViewById(R.id.grid_sbd_col6);
        col7 = v.findViewById(R.id.grid_sbd_col7);

        // Cột 1 (không chọn được)
        for (int i = 0; i <= 9; i++) {
            TextView tv = makeCell(String.valueOf(i));
            tv.setBackgroundResource(R.drawable.bg_digit_static);
            col1.addView(tv);
        }
        // Các cột 2–7 có thể chọn
        setupSelectable(col2, 2);
        setupSelectable(col3, 3);
        setupSelectable(col4, 4);
        setupSelectable(col5, 5);
        setupSelectable(col6, 6);
        setupSelectable(col7, 7);

        // Khôi phục nếu truyền sẵn
        if (getArguments() != null) {
            String sbd = getArguments().getString("sbd");
            setSelectedSbd(sbd);
        }
        return v;
    }

    private void setupSelectable(GridLayout grid, final int col) {
        for (int i = 0; i <= 9; i++) {
            final int num = i;
            TextView tv = makeCell(String.valueOf(i));
            tv.setBackgroundResource(R.drawable.bg_digit_unselected);
            tv.setOnClickListener(x -> {
                clearColumn(grid);
                tv.setBackgroundResource(R.drawable.bg_digit_selected);
                selected[col] = num;
            });
            grid.addView(tv);
        }
    }

    private void clearColumn(GridLayout grid) {
        for (int i = 0; i < grid.getChildCount(); i++) {
            grid.getChildAt(i).setBackgroundResource(R.drawable.bg_digit_unselected);
        }
    }

    /**
     * Tạo ô số cố định kích thước 36dp x 36dp, margin 2dp
     */
    private TextView makeCell(String text) {
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(14);
        int size = dp(36);
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width  = size;
        lp.height = size;
        lp.setMargins(dp(2), dp(2), dp(2), dp(2));
        tv.setLayoutParams(lp);
        return tv;
    }

    /** Trả về chuỗi 6 ký tự số báo danh */
    public String getSoBaoDanh() {
        StringBuilder sb = new StringBuilder();
        for (int c = 2; c <= 7; c++) {
            sb.append(selected[c] >= 0 ? selected[c] : "0");
        }
        return sb.toString();
    }

    /** Khôi phục lựa chọn khi sửa */
    public void setSelectedSbd(String sbd) {
        if (sbd == null || sbd.length() != 6) return;
        for (int i = 0; i < 6; i++) {
            selected[i + 2] = Character.getNumericValue(sbd.charAt(i));
        }
        restore(col2, selected[2]);
        restore(col3, selected[3]);
        restore(col4, selected[4]);
        restore(col5, selected[5]);
        restore(col6, selected[6]);
        restore(col7, selected[7]);
    }

    private void restore(GridLayout grid, int sel) {
        for (int i = 0; i < grid.getChildCount(); i++) {
            TextView tv = (TextView) grid.getChildAt(i);
            int v = Integer.parseInt(tv.getText().toString());
            tv.setBackgroundResource(v == sel
                    ? R.drawable.bg_digit_selected
                    : R.drawable.bg_digit_unselected);
        }
    }

    private int dp(int d) {
        return (int)(d * getResources().getDisplayMetrics().density + 0.5f);
    }
}
