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
import com.example.javaopencv.data.entity.Student;

public class StudentAdapter extends ListAdapter<Student, StudentAdapter.StudentViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(Student student);
    }

    private final OnItemClickListener listener;

    public StudentAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<Student>() {
            @Override
            public boolean areItemsTheSame(@NonNull Student o, @NonNull Student n) {
                return o.getId() == n.getId();
            }
            @Override
            public boolean areContentsTheSame(@NonNull Student o, @NonNull Student n) {
                return o.getName().equals(n.getName())
                        && o.getStudentNumber().equals(n.getStudentNumber());
            }
        });
        this.listener = listener;
    }

    @NonNull @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName, tvSbd;
        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvStudentName);
            tvSbd  = itemView.findViewById(R.id.tvStudentSbd);
        }
        public void bind(Student s, OnItemClickListener l) {
            tvName.setText(s.getName());
            tvSbd.setText(s.getStudentNumber());
            itemView.setOnClickListener(v -> l.onItemClick(s));
        }
    }
}