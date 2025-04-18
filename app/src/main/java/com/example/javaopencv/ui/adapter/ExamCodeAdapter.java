package com.example.javaopencv.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.javaopencv.R;
import java.util.ArrayList;
import java.util.List;

public class ExamCodeAdapter extends RecyclerView.Adapter<ExamCodeAdapter.ViewHolder> {

    private List<String> examCodeList;
    private OnExamCodeClickListener listener;

    /** Giao diện callback */
    public interface OnExamCodeClickListener {
        void onExamCodeClick(int position, String maDe);
        void onExamCodeLongClick(int position, String maDe);
    }

    /** Constructor không tham số để có thể khởi tạo trước */
    public ExamCodeAdapter() {
        this.examCodeList = new ArrayList<>();
    }

    /** Nếu muốn khởi tạo cùng danh sách ngay từ đầu */
    public ExamCodeAdapter(List<String> examCodeList) {
        this.examCodeList = new ArrayList<>(examCodeList);
    }

    public void setOnExamCodeClickListener(OnExamCodeClickListener listener) {
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exam_code, parent, false);
        return new ViewHolder(view);
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(examCodeList.get(position), listener);
    }

    @Override public int getItemCount() {
        return examCodeList.size();
    }

    /** Gọi khi want refresh data */
    public void updateData(List<String> newExamCodes) {
        examCodeList.clear();
        examCodeList.addAll(newExamCodes);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvExamCode, tvLabel;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLabel    = itemView.findViewById(R.id.tv_label);
            tvExamCode = itemView.findViewById(R.id.tv_exam_code);
        }

        void bind(String examCode, OnExamCodeClickListener listener) {
            tvLabel.setText("Mã đề");
            tvExamCode.setText(examCode);

            itemView.setOnClickListener(v -> {
                if (listener != null)
                    listener.onExamCodeClick(getAdapterPosition(), examCode);
            });
            itemView.setOnLongClickListener(v -> {
                if (listener != null)
                    listener.onExamCodeLongClick(getAdapterPosition(), examCode);
                return true;
            });
        }
    }
}
