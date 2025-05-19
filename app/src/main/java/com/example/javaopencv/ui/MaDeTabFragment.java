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

public class MaDeTabFragment extends Fragment {

    private GridLayout gridCol1, gridCol2, gridCol3, gridCol4;
    private int selectedCol2 = -1, selectedCol3 = -1, selectedCol4 = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ma_de_tab, container, false);

        gridCol1 = view.findViewById(R.id.grid_col1);
        gridCol2 = view.findViewById(R.id.grid_col2);
        gridCol3 = view.findViewById(R.id.grid_col3);
        gridCol4 = view.findViewById(R.id.grid_col4);

        setupStaticGrid(gridCol1);
        setupSelectableGrid(gridCol2, 2);
        setupSelectableGrid(gridCol3, 3);
        setupSelectableGrid(gridCol4, 4);

        if (getArguments() != null && getArguments().containsKey("selectedMaDe")) {
            String selectedMaDe = getArguments().getString("selectedMaDe");
            if (selectedMaDe != null) {
                setSelectedMaDe(selectedMaDe);
            }
        }

        return view;
    }

    private void setupStaticGrid(GridLayout grid) {
        for (int i = 0; i <= 9; i++) {
            TextView textView = createTextView(String.valueOf(i));
            textView.setBackgroundResource(R.drawable.bg_digit_static);
            grid.addView(textView);
        }
    }

    private void setupSelectableGrid(GridLayout grid, int column) {
        for (int i = 0; i <= 9; i++) {
            final int num = i;
            TextView textView = createTextView(String.valueOf(i));
            textView.setBackgroundResource(R.drawable.bg_digit_unselected);
            textView.setOnClickListener(v -> onDigitClicked(column, num, textView));
            grid.addView(textView);
        }
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setTextSize(18);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(24, 24, 24, 24);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = GridLayout.LayoutParams.WRAP_CONTENT;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.setMargins(8, 8, 8, 8);
        textView.setLayoutParams(params);

        return textView;
    }

    private void onDigitClicked(int column, int digit, TextView textView) {
        clearSelections(column);

        textView.setBackgroundResource(R.drawable.bg_digit_selected);

        if (column == 2) selectedCol2 = digit;
        if (column == 3) selectedCol3 = digit;
        if (column == 4) selectedCol4 = digit;
    }

    private void clearSelections(int column) {
        GridLayout grid = null;
        if (column == 2) grid = gridCol2;
        if (column == 3) grid = gridCol3;
        if (column == 4) grid = gridCol4;

        if (grid != null) {
            for (int i = 0; i < grid.getChildCount(); i++) {
                View child = grid.getChildAt(i);
                if (child instanceof TextView) {
                    child.setBackgroundResource(R.drawable.bg_digit_unselected);
                }
            }
        }
    }

    public String getMaDe() {
        if (selectedCol2 == -1 || selectedCol3 == -1 || selectedCol4 == -1) {
            return null;
        }
        return String.valueOf(selectedCol2) + selectedCol3 + selectedCol4;
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

    private void restoreSelection(GridLayout grid, int selectedDigit) {
        if (grid == null) return;
        for (int i = 0; i < grid.getChildCount(); i++) {
            View child = grid.getChildAt(i);
            if (child instanceof TextView) {
                TextView textView = (TextView) child;
                int digit = Integer.parseInt(textView.getText().toString());
                if (digit == selectedDigit) {
                    textView.setBackgroundResource(R.drawable.bg_digit_selected);
                } else {
                    textView.setBackgroundResource(R.drawable.bg_digit_unselected);
                }
            }
        }
    }
}
