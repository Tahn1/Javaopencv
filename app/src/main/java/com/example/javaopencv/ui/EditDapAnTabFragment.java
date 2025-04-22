package com.example.javaopencv.ui;

import android.os.Bundle;
import android.util.Log;
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

public class EditDapAnTabFragment extends Fragment {
    private static final String TAG = "EditDapAnTab";
    private static final String ARG_TOTAL = "totalQuestions";
    private static final String ARG_CSV   = "daCsv";

    private int totalQuestions;
    private String[] initialAnswers;
    private int[] selectedAnswers;

    private GridLayout gridCol1, gridCol2, gridCol3, gridCol4, gridCol5;

    /** Interface để callback khi user thay đổi đáp án */
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

        // 1) bind 5 GridLayout
        gridCol1 = view.findViewById(R.id.grid_da_col1);
        gridCol2 = view.findViewById(R.id.grid_da_col2);
        gridCol3 = view.findViewById(R.id.grid_da_col3);
        gridCol4 = view.findViewById(R.id.grid_da_col4);
        gridCol5 = view.findViewById(R.id.grid_da_col5);

        // 2) clear cũ
        gridCol1.removeAllViews();
        gridCol2.removeAllViews();
        gridCol3.removeAllViews();
        gridCol4.removeAllViews();
        gridCol5.removeAllViews();

        // 3) đọc tham số
        Bundle args = getArguments();
        totalQuestions = args != null ? args.getInt(ARG_TOTAL, 0) : 0;
        String csv      = args != null ? args.getString(ARG_CSV, "") : "";
        initialAnswers  = csv.isEmpty() ? new String[0] : csv.split("\\s*,\\s*");
        selectedAnswers = new int[totalQuestions];

        Log.d(TAG, "Received CSV = \"" + csv + "\"");
        Log.d(TAG, "initialAnswers = " + java.util.Arrays.toString(initialAnswers));

        // 4) setRowCount
        gridCol1.setRowCount(totalQuestions);
        gridCol2.setRowCount(totalQuestions);
        gridCol3.setRowCount(totalQuestions);
        gridCol4.setRowCount(totalQuestions);
        gridCol5.setRowCount(totalQuestions);

        // 5) build từng hàng
        for (int row = 0; row < totalQuestions; row++) {
            // cột 1: STT
            TextView tvHeader = createCell(String.valueOf(row + 1));
            tvHeader.setBackgroundResource(R.drawable.bg_digit_static);
            gridCol1.addView(tvHeader);

            // khôi phục
            int init = 0;
            if (row < initialAnswers.length) {
                String ans = initialAnswers[row].trim().toUpperCase();
                switch (ans) {
                    case "A": init = 1; break;
                    case "B": init = 2; break;
                    case "C": init = 3; break;
                    case "D": init = 4; break;
                    default:
                        try {
                            int v = Integer.parseInt(ans);
                            if (v >= 1 && v <= 4) init = v;
                        } catch (NumberFormatException ignored) {}
                }
            }
            selectedAnswers[row] = init;
            Log.d(TAG, "row=" + row + " init=" + init);

            // cột 2..5: A–D
            for (int col = 2; col <= 5; col++) {
                final int r = row;
                final int value = col - 1;
                String label;
                switch (value) {
                    case 1: label = "A"; break;
                    case 2: label = "B"; break;
                    case 3: label = "C"; break;
                    default: label = "D"; break;
                }
                TextView tv = createCell(label);
                tv.setBackgroundResource(
                        selectedAnswers[row] == value
                                ? R.drawable.bg_digit_selected
                                : R.drawable.bg_digit_unselected
                );
                tv.setOnClickListener(v -> {
                    clearRowSelection(r);
                    tv.setBackgroundResource(R.drawable.bg_digit_selected);
                    selectedAnswers[r] = value;
                    // gọi callback khi value thay đổi
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
        switch (col) {
            case 1: return gridCol1;
            case 2: return gridCol2;
            case 3: return gridCol3;
            case 4: return gridCol4;
            default: return gridCol5;
        }
    }

    private TextView createCell(String text) {
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(16);
        int p = dp(12);
        tv.setPadding(p, p, p, p);
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width  = GridLayout.LayoutParams.WRAP_CONTENT;
        lp.height = GridLayout.LayoutParams.WRAP_CONTENT;
        int m = dp(4);
        lp.setMargins(m, m, m, m);
        tv.setLayoutParams(lp);
        return tv;
    }

    private int dp(int d) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(d * density);
    }

    /** Trả về mảng đáp án đã chọn (1..4), 0 nếu bỏ trống */
    public int[] getSelectedAnswers() {
        return selectedAnswers;
    }
}
