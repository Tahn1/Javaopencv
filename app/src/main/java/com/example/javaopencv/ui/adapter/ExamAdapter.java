package com.example.javaopencv.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.Exam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ExamAdapter extends RecyclerView.Adapter<ExamAdapter.ExamViewHolder> {

    private List<Exam> examList = new ArrayList<>();
    private List<Exam> examListFull = new ArrayList<>();

    private OnExamItemClickListener clickListener;
    private OnExamItemLongClickListener longClickListener;

    /** Chỉ có 1 phương thức click */
    public interface OnExamItemClickListener {
        void onExamItemClick(Exam exam);
    }

    public interface OnExamItemLongClickListener {
        void onExamItemLongClick(Exam exam);
    }

    /** Đổi tên cho rõ ràng */
    public void setOnExamItemClickListener(OnExamItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnExamItemLongClickListener(OnExamItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setExamList(List<Exam> examList) {
        this.examList = examList;
        this.examListFull = new ArrayList<>(examList);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        if (query == null || query.trim().isEmpty()) {
            examList = new ArrayList<>(examListFull);
        } else {
            List<Exam> filtered = new ArrayList<>();
            String q = query.toLowerCase();
            for (Exam e : examListFull) {
                if (e.title.toLowerCase().contains(q)) {
                    filtered.add(e);
                }
            }
            examList = filtered;
        }
        notifyDataSetChanged();
    }

    public void sortByOption(String option) {
        Comparator<Exam> cmp;
        switch (option) {
            case "name_asc":
                cmp = (a, b) -> a.title.compareToIgnoreCase(b.title);
                break;
            case "name_desc":
                cmp = (a, b) -> b.title.compareToIgnoreCase(a.title);
                break;
            case "date_asc":
                cmp = (a, b) -> a.date.compareTo(b.date);
                break;
            case "date_desc":
                cmp = (a, b) -> b.date.compareTo(a.date);
                break;
            default:
                return;
        }
        Collections.sort(examList, cmp);
        Collections.sort(examListFull, cmp);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exam, parent, false);
        return new ExamViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamViewHolder holder, int position) {
        Exam exam = examList.get(position);
        holder.tvExamTitle.setText(exam.title);
        holder.tvExamPhieu.setText(exam.phieu);
        holder.tvExamDate.setText(exam.date);
        holder.tvExamSocau.setText("Số câu: " + exam.soCau);
    }

    @Override
    public int getItemCount() {
        return examList.size();
    }

    class ExamViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        TextView tvExamTitle, tvExamPhieu, tvExamDate, tvExamSocau;

        public ExamViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExamTitle = itemView.findViewById(R.id.tv_exam_title);
            tvExamPhieu = itemView.findViewById(R.id.tv_exam_phieu);
            tvExamDate  = itemView.findViewById(R.id.tv_exam_date);
            tvExamSocau = itemView.findViewById(R.id.tv_exam_socau);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null) {
                clickListener.onExamItemClick(examList.get(getAdapterPosition()));
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (longClickListener != null) {
                longClickListener.onExamItemLongClick(examList.get(getAdapterPosition()));
                return true;
            }
            return false;
        }
    }

    // Alias để tương thích với Fragment gọi setListener(...) và setLongClickListener(...)
    public void setListener(OnExamItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setLongClickListener(OnExamItemLongClickListener listener) {
        this.longClickListener = listener;
    }
}
