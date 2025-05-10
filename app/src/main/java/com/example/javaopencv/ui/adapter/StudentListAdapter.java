package com.example.javaopencv.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.Student;

public class StudentListAdapter
        extends ListAdapter<Student, StudentListAdapter.VH> {

    public interface OnItemClickListener {
        void onItemClick(@NonNull Student student);
    }
    private OnItemClickListener clickListener;
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    private static final DiffUtil.ItemCallback<Student> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Student>() {
                @Override
                public boolean areItemsTheSame(@NonNull Student a, @NonNull Student b) {
                    return a.getId() == b.getId();
                }
                @Override
                public boolean areContentsTheSame(@NonNull Student a, @NonNull Student b) {
                    return a.getName().equals(b.getName())
                            && a.getStudentNumber().equals(b.getStudentNumber());
                }
            };

    public StudentListAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_table, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int pos) {
        Student s = getItem(pos);
        // Cột STT
        holder.tvIndex.setText(String.valueOf(pos + 1));
        // Cột Tên
        holder.tvName.setText(s.getName());
        // Cột Mã SV
        holder.tvCode.setText(s.getStudentNumber());
        // Cột Điểm cố định
        holder.tvScore.setText("--");
        // Cột Ghi chú (EditText) — mặc định giữ trống
        holder.etNote.setText("");

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(s);
            }
        });
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvIndex, tvName, tvCode, tvScore;
        final EditText etNote;

        VH(@NonNull View itemView) {
            super(itemView);
            tvIndex = itemView.findViewById(R.id.tvIndex);
            tvName  = itemView.findViewById(R.id.tvName);
            tvCode  = itemView.findViewById(R.id.tvCode);
            tvScore = itemView.findViewById(R.id.tvScore);
            etNote  = itemView.findViewById(R.id.etNote);
        }
    }
}
