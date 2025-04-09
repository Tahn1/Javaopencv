package com.example.javaopencv.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.Exam;
import java.util.ArrayList;
import java.util.List;

public class ExamAdapter extends RecyclerView.Adapter<ExamAdapter.ExamViewHolder> {
    private List<Exam> examList = new ArrayList<>();

    @NonNull
    @Override
    public ExamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exam, parent, false);
        return new ExamViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamViewHolder holder, int position) {
        Exam currentExam = examList.get(position);
        holder.textViewTitle.setText(currentExam.title);
        // Bạn có thể bind thêm các dữ liệu khác từ currentExam vào view
    }

    @Override
    public int getItemCount() {
        return examList.size();
    }

    public void setExamList(List<Exam> examList) {
        this.examList = examList;
        notifyDataSetChanged();
    }

    static class ExamViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        // Khai báo thêm các view khác nếu cần

        public ExamViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_title);
        }
    }
}
