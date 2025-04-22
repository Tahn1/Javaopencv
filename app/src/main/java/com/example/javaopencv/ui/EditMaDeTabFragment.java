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

public class EditMaDeTabFragment extends Fragment {

    private GridLayout gridCol1, gridCol2, gridCol3, gridCol4;
    private int selectedCol2 = -1, selectedCol3 = -1, selectedCol4 = -1;

    public static EditMaDeTabFragment newInstance(String maDe) {
        Bundle b = new Bundle();
        b.putString("maDe", maDe);
        EditMaDeTabFragment f = new EditMaDeTabFragment();
        f.setArguments(b);
        return f;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf,
                             @Nullable ViewGroup parent,
                             @Nullable Bundle saved) {
        View v = inf.inflate(R.layout.fragment_edit_tab_ma_de, parent, false);

        gridCol1 = v.findViewById(R.id.grid_col1);
        gridCol2 = v.findViewById(R.id.grid_col2);
        gridCol3 = v.findViewById(R.id.grid_col3);
        gridCol4 = v.findViewById(R.id.grid_col4);

        setupStaticGrid(gridCol1);
        setupSelectableGrid(gridCol2, 2);
        setupSelectableGrid(gridCol3, 3);
        setupSelectableGrid(gridCol4, 4);

        // Khôi phục nếu có mã đề cũ
        if (getArguments() != null) {
            String maDe = getArguments().getString("maDe");
            setSelectedMaDe(maDe);
        }
        return v;
    }

    private void setupStaticGrid(GridLayout g) {
        for (int i = 0; i <= 9; i++) {
            TextView tv = createCell(String.valueOf(i));
            tv.setBackgroundResource(R.drawable.bg_digit_static);
            g.addView(tv);
        }
    }

    private void setupSelectableGrid(GridLayout g, final int col) {
        for (int i = 0; i <= 9; i++) {
            final int num = i;
            TextView tv = createCell(String.valueOf(i));
            tv.setBackgroundResource(R.drawable.bg_digit_unselected);
            tv.setOnClickListener(x -> onDigitClicked(col, num, tv));
            g.addView(tv);
        }
    }

    private TextView createCell(String text) {
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setTextSize(18);
        tv.setGravity(Gravity.CENTER);
        int p = dp(12);
        tv.setPadding(p, p, p, p);
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = GridLayout.LayoutParams.WRAP_CONTENT;
        lp.height= GridLayout.LayoutParams.WRAP_CONTENT;
        lp.setMargins(dp(4), dp(4), dp(4), dp(4));
        tv.setLayoutParams(lp);
        return tv;
    }

    private void onDigitClicked(int column, int digit, TextView tv) {
        clearSelections(column);
        tv.setBackgroundResource(R.drawable.bg_digit_selected);
        if (column == 2) selectedCol2 = digit;
        else if (column == 3) selectedCol3 = digit;
        else if (column == 4) selectedCol4 = digit;
    }

    private void clearSelections(int column) {
        GridLayout grid = null;
        if (column == 2) {
            grid = gridCol2;
        } else if (column == 3) {
            grid = gridCol3;
        } else if (column == 4) {
            grid = gridCol4;
        }
        if (grid != null) {
            for (int i = 0; i < grid.getChildCount(); i++) {
                grid.getChildAt(i).setBackgroundResource(R.drawable.bg_digit_unselected);
            }
        }
    }

    public String getMaDe() {
        if (selectedCol2 < 0 || selectedCol3 < 0 || selectedCol4 < 0)
            return null;
        return "" + selectedCol2 + selectedCol3 + selectedCol4;
    }

    public void setSelectedMaDe(String maDe) {
        if (maDe == null || maDe.length() != 3) return;
        selectedCol2 = Character.getNumericValue(maDe.charAt(0));
        selectedCol3 = Character.getNumericValue(maDe.charAt(1));
        selectedCol4 = Character.getNumericValue(maDe.charAt(2));

        restoreSelection(gridCol2, selectedCol2);
        restoreSelection(gridCol3, selectedCol3);
        restoreSelection(gridCol4, selectedCol4);
    }

    private void restoreSelection(GridLayout grid, int sel) {
        if (grid == null) return;
        for (int i = 0; i < grid.getChildCount(); i++) {
            TextView tv = (TextView) grid.getChildAt(i);
            int v = Integer.parseInt(tv.getText().toString());
            if (v == sel) {
                tv.setBackgroundResource(R.drawable.bg_digit_selected);
            } else {
                tv.setBackgroundResource(R.drawable.bg_digit_unselected);
            }
        }
    }

    private int dp(int d) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (d * density + 0.5f);
    }
}
