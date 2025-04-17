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

    /** Giao diện click thường */
    public interface OnExamItemClickListener {
        void onExamItemClick(Exam exam);
    }

    /** Giao diện click giữ */
    public interface OnExamItemLongClickListener {
        void onExamItemLongClick(Exam exam);
    }

    public void setClickListener(OnExamItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setLongClickListener(OnExamItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    /** Cập nhật danh sách và bản sao cho filter */
    public void setExamList(List<Exam> list) {
        examList = new ArrayList<>(list);
        examListFull = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    /** Lọc theo title */
    public void filter(String query) {
        if (query == null || query.trim().isEmpty()) {
            examList = new ArrayList<>(examListFull);
        } else {
            String q = query.toLowerCase();
            List<Exam> filtered = new ArrayList<>();
            for (Exam e : examListFull) {
                if (e.title.toLowerCase().contains(q)) {
                    filtered.add(e);
                }
            }
            examList = filtered;
        }
        notifyDataSetChanged();
    }

    /**
     * Sắp xếp:
     * name_asc, name_desc, date_asc, date_desc
     */
    public void sortByOption(String option) {
        if (option == null) return;
        Comparator<Exam> cmp;
        switch (option) {
            case "name_asc":
                cmp = Comparator.comparing(e -> e.title.toLowerCase());
                break;
            case "name_desc":
                cmp = (a,b) -> b.title.toLowerCase().compareTo(a.title.toLowerCase());
                break;
            case "date_asc":
                cmp = Comparator.comparing(e -> e.date);
                break;
            case "date_desc":
                cmp = (a,b) -> b.date.compareTo(a.date);
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
                .inflate(R.layout.item_exam_card, parent, false);
        return new ExamViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamViewHolder holder, int position) {
        Exam e = examList.get(position);
        holder.tvTitle.setText(e.title);
        holder.tvType.setText(e.phieu);
        holder.tvDate.setText(e.date);
        holder.tvCount.setText("Số câu: " + e.soCau);
    }

    @Override
    public int getItemCount() {
        return examList.size();
    }

    class ExamViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvType, tvDate, tvCount;

        public ExamViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvExamTitle);
            tvType  = itemView.findViewById(R.id.tvExamType);
            tvDate  = itemView.findViewById(R.id.tvExamDate);
            tvCount = itemView.findViewById(R.id.tvQuestionCount);

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onExamItemClick(examList.get(getAdapterPosition()));
                }
            });
            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onExamItemLongClick(examList.get(getAdapterPosition()));
                    return true;
                }
                return false;
            });
        }
    }
}
