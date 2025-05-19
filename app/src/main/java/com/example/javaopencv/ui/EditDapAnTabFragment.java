package com.example.javaopencv.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import androidx.gridlayout.widget.GridLayout;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.javaopencv.R;

public class EditDapAnTabFragment extends Fragment {
    private static final String TAG = "EditDapAnTab";
    private static final String ARG_TOTAL = "totalQuestions";
    private static final String ARG_CSV   = "daCsv";

    private int totalQuestions;
    private String[] initialAnswers;
    private int[] selectedAnswers;

    private GridLayout gridCol1, gridCol2, gridCol3, gridCol4, gridCol5;

    public interface OnAnswerChangeListener {
        void onAnswersChanged(int[] selectedAnswers);
    }
    private OnAnswerChangeListener listener;
    public void setOnAnswerChangeListener(OnAnswerChangeListener l) {
        this.listener = l;
    }

    public static EditDapAnTabFragment newInstance(int totalQuestions, String daCsv) {
        Bundle args = new Bundle();
        args.putInt(ARG_TOTAL, totalQuestions);
        args.putString(ARG_CSV, daCsv);
        EditDapAnTabFragment f = new EditDapAnTabFragment();
        f.setArguments(args);
        return f;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_tab_dap_an, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        gridCol1 = view.findViewById(R.id.grid_da_col1);
        gridCol2 = view.findViewById(R.id.grid_da_col2);
        gridCol3 = view.findViewById(R.id.grid_da_col3);
        gridCol4 = view.findViewById(R.id.grid_da_col4);
        gridCol5 = view.findViewById(R.id.grid_da_col5);

        gridCol1.removeAllViews();
        gridCol2.removeAllViews();
        gridCol3.removeAllViews();
        gridCol4.removeAllViews();
        gridCol5.removeAllViews();

        Bundle args = getArguments();
        totalQuestions = args != null ? args.getInt(ARG_TOTAL, 0) : 0;
        String csv      = args != null ? args.getString(ARG_CSV, "") : "";
        initialAnswers  = csv.isEmpty() ? new String[0] : csv.split("\\s*,\\s*");
        selectedAnswers = new int[totalQuestions];

        Log.d(TAG, "Received CSV = \"" + csv + "\"");
        Log.d(TAG, "initialAnswers = " + java.util.Arrays.toString(initialAnswers));

        gridCol1.setRowCount(totalQuestions);
        gridCol2.setRowCount(totalQuestions);
        gridCol3.setRowCount(totalQuestions);
        gridCol4.setRowCount(totalQuestions);
        gridCol5.setRowCount(totalQuestions);

        for (int row = 0; row < totalQuestions; row++) {
            TextView tvHeader = createCell(String.valueOf(row + 1));
            tvHeader.setBackgroundResource(R.drawable.bg_digit_static);
            gridCol1.addView(tvHeader);

            int init = 0;
            if (row < initialAnswers.length) {
                String ans = initialAnswers[row].trim().toUpperCase();
                if ("A".equals(ans)) init = 1;
                else if ("B".equals(ans)) init = 2;
                else if ("C".equals(ans)) init = 3;
                else if ("D".equals(ans)) init = 4;
            }
            selectedAnswers[row] = init;

            // Cột A–D
            for (int col = 2; col <= 5; col++) {
                final int r = row;
                final int val = col - 1; //
                String label;
                if (val == 1)      label = "A";
                else if (val == 2) label = "B";
                else if (val == 3) label = "C";
                else               label = "D";

                TextView tv = createCell(label);
                tv.setBackgroundResource(
                        selectedAnswers[row] == val
                                ? R.drawable.bg_digit_selected
                                : R.drawable.bg_digit_unselected
                );
                tv.setOnClickListener(v -> {
                    clearRowSelection(r);
                    tv.setBackgroundResource(R.drawable.bg_digit_selected);
                    selectedAnswers[r] = val;
                    if (listener != null) {
                        listener.onAnswersChanged(selectedAnswers);
                    }
                });
                getGridForCol(col).addView(tv);
            }
        }
    }

    private void clearRowSelection(int row) {
        for (int col = 2; col <= 5; col++) {
            GridLayout g = getGridForCol(col);
            View cell = g.getChildAt(row);
            if (cell != null) {
                cell.setBackgroundResource(R.drawable.bg_digit_unselected);
            }
        }
        selectedAnswers[row] = 0;
    }

    private GridLayout getGridForCol(int col) {
        if (col == 2) return gridCol2;
        else if (col == 3) return gridCol3;
        else if (col == 4) return gridCol4;
        else             return gridCol5;
    }


    private TextView createCell(String text) {
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setTextSize(16);
        tv.setGravity(Gravity.CENTER);

        int size = dp(54);
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width  = size;
        lp.height = size;
        int m = dp(4);
        lp.setMargins(m, m, m, m);

        tv.setLayoutParams(lp);
        return tv;
    }

    private int dp(int d) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(d * density);
    }

    public int[] getSelectedAnswers() {
        return selectedAnswers;
    }
}
