package com.example.javaopencv.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.GradeResult;
import com.example.javaopencv.viewmodel.XemLaiViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class ThongKeFragment extends Fragment {
    private static final String ARG_EXAM_ID = "examId";
    private int examId;
    private XemLaiViewModel viewModel;
    private BarChart barChart;
    private PieChart pieChart;

    public static ThongKeFragment newInstance(int examId) {
        Bundle args = new Bundle();
        args.putInt(ARG_EXAM_ID, examId);
        ThongKeFragment f = new ThongKeFragment();
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_thong_ke, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set toolbar và nút back
        Toolbar toolbar = view.findViewById(R.id.toolbar_thong_ke);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white); // icon back
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        barChart = view.findViewById(R.id.barChart);
        pieChart = view.findViewById(R.id.pieChart);

        if (getArguments() != null) {
            examId = getArguments().getInt(ARG_EXAM_ID, -1);
        }

        viewModel = new ViewModelProvider(this).get(XemLaiViewModel.class);
        viewModel.getResultsForExam(examId)
                .observe(getViewLifecycleOwner(), this::updateCharts);
    }

    private void updateCharts(List<GradeResult> results) {
        if (results == null || results.isEmpty()) return;

        int[] countPerScore = new int[11];
        int cntYeu = 0, cntTB = 0, cntKha = 0, cntGioi = 0;

        for (GradeResult gr : results) {
            double score = gr.score;
            int rounded = (int) Math.round(score);
            if (rounded >= 0 && rounded <= 10) {
                countPerScore[rounded]++;
            }

            if (score < 5)        cntYeu++;
            else if (score < 7)   cntTB++;
            else if (score < 8.5) cntKha++;
            else                  cntGioi++;
        }

        setupBarChart(countPerScore);
        setupPieChart(cntYeu, cntTB, cntKha, cntGioi);
    }

    private void setupBarChart(int[] countPerScore) {
        List<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i <= 10; i++) {
            if (countPerScore[i] > 0) {
                entries.add(new BarEntry(i, countPerScore[i]));
            }
        }

        BarDataSet set = new BarDataSet(entries, "");
        set.setColor(Color.parseColor("#FF5722"));
        set.setValueTextSize(12f);
        set.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.0f", value);
            }
        });

        BarData data = new BarData(set);
        data.setBarWidth(0.6f);

        barChart.setData(data);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setAxisMinimum(0f);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(11, false);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int intVal = (int) value;
                if (intVal >= 0 && intVal <= 10 && countPerScore[intVal] > 0) {
                    return String.valueOf(intVal);
                }
                return "";
            }
        });

        // Ẩn description
        Description desc = new Description();
        desc.setText("");
        barChart.setDescription(desc);

        barChart.getLegend().setEnabled(false);
        barChart.setFitBars(true);
        barChart.invalidate();
    }

    private void setupPieChart(int yeu, int tb, int kha, int gioi) {
        List<PieEntry> entries = new ArrayList<>();
        if (yeu > 0) entries.add(new PieEntry(yeu, "Yếu"));
        if (tb > 0)  entries.add(new PieEntry(tb, "Trung Bình"));
        if (kha > 0) entries.add(new PieEntry(kha, "Khá"));
        if (gioi > 0)entries.add(new PieEntry(gioi, "Giỏi"));

        PieDataSet set = new PieDataSet(entries, "");
        set.setSliceSpace(2f);
        set.setColors(
                Color.parseColor("#D81B60"),
                Color.parseColor("#FF9800"),
                Color.parseColor("#CDDC39"),
                Color.parseColor("#4CAF50")
        );
        set.setValueTextSize(12f);
        set.setValueFormatter(new PercentFormatter(pieChart));

        PieData data = new PieData(set);
        pieChart.setData(data);

        Description desc = new Description();
        desc.setText("");
        pieChart.setDescription(desc);

        pieChart.getLegend().setEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.invalidate();
    }
}
