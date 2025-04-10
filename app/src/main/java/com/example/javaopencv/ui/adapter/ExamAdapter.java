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
    // Bản sao đầy đủ của danh sách, dùng cho việc lọc
    private List<Exam> examListFull = new ArrayList<>();

    // Listener cho sự kiện click
    private OnExamItemClickListener clickListener;
    // Listener cho sự kiện long click (nhấn giữ)
    private OnExamItemLongClickListener longClickListener;

    // Interface cho sự kiện click (ngắn)
    public interface OnExamItemClickListener {
        void onExamItemClick(Exam exam);

        void onExamItemLongClick(Exam exam);
    }

    // Interface cho sự kiện long click
    public interface OnExamItemLongClickListener {
        void onExamItemLongClick(Exam exam);
    }

    public void setListener(OnExamItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setLongClickListener(OnExamItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    // Hàm set danh sách Exam (với full copy dùng cho việc lọc)
    public void setExamList(List<Exam> examList) {
        this.examList = examList;
        this.examListFull = new ArrayList<>(examList);
        notifyDataSetChanged();
    }

    /**
     * Phương thức filter danh sách theo tiêu đề (không phân biệt chữ hoa chữ thường)
     */
    public void filter(String query) {
        if (query == null || query.trim().isEmpty()) {
            examList = new ArrayList<>(examListFull);
        } else {
            List<Exam> filteredList = new ArrayList<>();
            String lowerQuery = query.toLowerCase();
            for (Exam exam : examListFull) {
                // Giả sử trường title trong Exam được truy cập trực tiếp
                if (exam.title.toLowerCase().contains(lowerQuery)) {
                    filteredList.add(exam);
                }
            }
            examList = filteredList;
        }
        notifyDataSetChanged();
    }

    /**
     * Phương thức sắp xếp danh sách dựa vào option:
     * "name_asc": theo tên tăng dần.
     * "name_desc": theo tên giảm dần.
     * "date_asc": theo ngày tạo tăng dần.
     * "date_desc": theo ngày tạo giảm dần.
     */
    public void sortByOption(String option) {
        if (option == null) return;
        Comparator<Exam> comparator;
        switch (option) {
            case "name_asc":
                comparator = (a, b) -> a.title.compareToIgnoreCase(b.title);
                break;
            case "name_desc":
                comparator = (a, b) -> b.title.compareToIgnoreCase(a.title);
                break;
            case "date_asc":
                comparator = (a, b) -> a.date.compareTo(b.date);
                break;
            case "date_desc":
                comparator = (a, b) -> b.date.compareTo(a.date);
                break;
            default:
                return;
        }
        Collections.sort(examList, comparator);
        Collections.sort(examListFull, comparator);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exam, parent, false);
        return new ExamViewHolder(itemView);
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

    class ExamViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView tvExamTitle, tvExamPhieu, tvExamDate, tvExamSocau;

        public ExamViewHolder(@NonNull View itemView) {
            super(itemView);
            // Lấy tham chiếu đến các TextView ở layout item_exam.xml (đảm bảo các id này khớp với file XML của bạn)
            tvExamTitle = itemView.findViewById(R.id.tv_exam_title);
            tvExamPhieu = itemView.findViewById(R.id.tv_exam_phieu);
            tvExamDate = itemView.findViewById(R.id.tv_exam_date);
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
}
