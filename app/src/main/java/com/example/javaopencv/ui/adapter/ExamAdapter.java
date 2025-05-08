package com.example.javaopencv.ui.adapter;

import android.util.Log;
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

    public interface OnExamItemClickListener {
        void onExamItemClick(Exam exam);
    }
    public interface OnExamItemLongClickListener {
        void onExamItemLongClick(Exam exam);
    }

    public void setOnExamItemClickListener(OnExamItemClickListener listener) {
        this.clickListener = listener;
    }
    public void setOnExamItemLongClickListener(OnExamItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setExamList(List<Exam> list) {
        this.examList = list != null ? list : new ArrayList<>();
        this.examListFull = new ArrayList<>(this.examList);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        if (query == null || query.trim().isEmpty()) {
            examList = new ArrayList<>(examListFull);
        } else {
            String q = query.toLowerCase();
            List<Exam> filtered = new ArrayList<>();
            for (Exam e : examListFull) {
                if (e.getTitle().toLowerCase().contains(q)) {
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
                cmp = (a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle());
                break;
            case "name_desc":
                cmp = (a, b) -> b.getTitle().compareToIgnoreCase(a.getTitle());
                break;
            case "date_asc":
                cmp = (a, b) -> a.getDate().compareTo(b.getDate());
                break;
            case "date_desc":
                cmp = (a, b) -> b.getDate().compareTo(a.getDate());
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
        Log.d("ExamAdapter", "Binding pos=" + position + " title=" + exam.getTitle());

        holder.tvTitle.setText(exam.getTitle());
        holder.tvClass.setText(exam.getClassName() != null ? exam.getClassName() : "");
        holder.tvSocau.setText("Số câu");
        holder.tvPhieu.setText(exam.getPhieu());
        holder.tvDate.setText(exam.getDate());
        holder.tvCount.setText(String.valueOf(exam.getSoCau()));
    }

    @Override
    public int getItemCount() {
        return examList.size();
    }

    class ExamViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        final TextView tvTitle;
        final TextView tvClass;
        final TextView tvSocau;
        final TextView tvPhieu;
        final TextView tvDate;
        final TextView tvCount;

        ExamViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_exam_title);
            tvClass = itemView.findViewById(R.id.tv_exam_class);
            tvSocau = itemView.findViewById(R.id.tv_exam_socau);
            tvPhieu = itemView.findViewById(R.id.tv_exam_phieu);
            tvDate  = itemView.findViewById(R.id.tv_exam_date);
            tvCount = itemView.findViewById(R.id.tv_exam_count_value);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                clickListener.onExamItemClick(examList.get(getAdapterPosition()));
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (longClickListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                longClickListener.onExamItemLongClick(examList.get(getAdapterPosition()));
                return true;
            }
            return false;
        }
    }
    // alias để tương thích với tên cũ
    public void setListener(OnExamItemClickListener listener) {
        setOnExamItemClickListener(listener);
    }
    public void setLongClickListener(OnExamItemLongClickListener listener) {
        setOnExamItemLongClickListener(listener);
    }
}
