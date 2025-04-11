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

    private List<String> examCodeList = new ArrayList<>();

    public interface OnExamCodeClickListener {
        void onExamCodeClick(int position, String maDe);
        void onExamCodeLongClick(int position, String maDe);
    }

    private OnExamCodeClickListener listener;

    public ExamCodeAdapter(List<String> examCodeList) {
        this.examCodeList = examCodeList;
    }

    public void setOnExamCodeClickListener(OnExamCodeClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExamCodeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exam_code, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamCodeAdapter.ViewHolder holder, int position) {
        holder.bind(examCodeList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return examCodeList.size();
    }

    public void updateData(List<String> newExamCodes) {
        this.examCodeList.clear();
        this.examCodeList.addAll(newExamCodes);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvExamCode, tvLabel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(R.id.tv_label);
            tvExamCode = itemView.findViewById(R.id.tv_exam_code);
        }

        public void bind(String examCode, OnExamCodeClickListener listener) {
            tvLabel.setText("Mã đề");
            tvExamCode.setText(examCode);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExamCodeClick(getAdapterPosition(), examCode);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onExamCodeLongClick(getAdapterPosition(), examCode);
                }
                return true;
            });
        }
    }
}
