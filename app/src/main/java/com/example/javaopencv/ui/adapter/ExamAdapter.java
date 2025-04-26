package com.example.javaopencv.ui.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
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

    /** Interface cho click đơn */
    public interface OnExamItemClickListener {
        void onExamItemClick(Exam exam);
    }
    /** Interface cho click giữ */
    public interface OnExamItemLongClickListener {
        void onExamItemLongClick(Exam exam);
    }

    /** Đăng ký listener từ Fragment */
    public void setOnExamItemClickListener(OnExamItemClickListener listener) {
        this.clickListener = listener;
    }
    public void setOnExamItemLongClickListener(OnExamItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    /** Cập nhật danh sách mới */
    public void setExamList(List<Exam> list) {
        this.examList = list != null ? list : new ArrayList<>();
        this.examListFull = new ArrayList<>(this.examList);
        notifyDataSetChanged();
    }

    /** Lọc theo query */
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

    /** Sắp xếp theo option */
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
        // inflate layout sử dụng CardView
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exam, parent, false);
        return new ExamViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamViewHolder holder, int position) {
        Exam exam = examList.get(position);
        Log.d("ExamAdapter", "Binding pos=" + position + " title=" + exam.getTitle());        holder.tvExamTitle.setText(exam.getTitle());
        holder.tvExamTitle.setText(exam.getTitle());
        holder.tvExamPhieu.setText(exam.getPhieu());
        holder.tvExamDate.setText(exam.getDate());
        holder.tvExamCountValue.setText(String.valueOf(exam.getSoCau()));
        // tvExamSocau là label "Số câu" cố định trong XML
    }

    @Override
    public int getItemCount() {
        return examList.size();
    }

    class ExamViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        CardView card;
        TextView tvExamTitle, tvExamSocau, tvExamPhieu, tvExamDate, tvExamCountValue;

        public ExamViewHolder(@NonNull View itemView) {
            super(itemView);
            // nếu muốn thao tác với CardView: itemView.findViewById(R.id.card_exam);
            tvExamTitle      = itemView.findViewById(R.id.tv_exam_title);
            tvExamSocau      = itemView.findViewById(R.id.tv_exam_socau);
            tvExamPhieu      = itemView.findViewById(R.id.tv_exam_phieu);
            tvExamDate       = itemView.findViewById(R.id.tv_exam_date);
            tvExamCountValue = itemView.findViewById(R.id.tv_exam_count_value);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null) {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    clickListener.onExamItemClick(examList.get(pos));
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (longClickListener != null) {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    longClickListener.onExamItemLongClick(examList.get(pos));
                    return true;
                }
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
