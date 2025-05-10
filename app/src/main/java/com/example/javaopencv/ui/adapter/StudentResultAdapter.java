package com.example.javaopencv.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.StudentResult;

import java.util.Locale;

public class StudentResultAdapter
        extends ListAdapter<StudentResult, StudentResultAdapter.VH> {

    public StudentResultAdapter() {
        super(new DiffUtil.ItemCallback<StudentResult>() {
            @Override public boolean areItemsTheSame(@NonNull StudentResult a, @NonNull StudentResult b) {
                return a.getStudentId() == b.getStudentId();
            }
            @Override public boolean areContentsTheSame(@NonNull StudentResult a, @NonNull StudentResult b) {
                return a.equals(b);
            }
        });
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_table, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        StudentResult sr = getItem(pos);
        h.tvIndex.setText(String.valueOf(pos + 1));
        h.tvName .setText(sr.getName());
        h.tvCode .setText(sr.getStudentNumber());
        h.tvScore.setText(
                sr.getScore() != null
                        ? String.format(Locale.getDefault(), "%.2f", sr.getScore())
                        : "--"
        );
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvIndex, tvName, tvCode, tvScore;
        VH(@NonNull View itemView) {
            super(itemView);
            tvIndex = itemView.findViewById(R.id.tvIndex);
            tvName  = itemView.findViewById(R.id.tvName);
            tvCode  = itemView.findViewById(R.id.tvCode);
            tvScore = itemView.findViewById(R.id.tvScore);
        }
    }
}