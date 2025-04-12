package com.example.javaopencv.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;

import java.util.Arrays;
import java.util.List;

public class DapAnGridAdapter extends RecyclerView.Adapter<DapAnGridAdapter.ViewHolder> {

    private List<Integer> itemList; // Tổng số ô = questionCount * 5
    private int questionCount;
    private int[] selectedAnswer;   // Lưu các đáp án đã chọn, 1-A, 2-B, 3-C, 4-D, -1 chưa chọn

    public DapAnGridAdapter(List<Integer> itemList, int questionCount) {
        this.itemList = itemList;
        this.questionCount = questionCount;
        this.selectedAnswer = new int[questionCount];
        Arrays.fill(selectedAnswer, -1);  // Khởi tạo tất cả chưa chọn
    }

    @NonNull
    @Override
    public DapAnGridAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_circle, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DapAnGridAdapter.ViewHolder holder, int position) {
        int row = position / 5;  // Dòng thứ mấy
        int col = position % 5;  // Cột (0: STT, 1->4: A, B, C, D)

        if (col == 0) {
            // Cột số thứ tự câu hỏi
            holder.tvNumber.setText(String.valueOf(row + 1));
            holder.tvNumber.setTextColor(Color.BLACK);
            holder.bgCircle.setBackgroundResource(R.drawable.bg_circle_white_orange_border);
            holder.itemView.setClickable(false);
            holder.itemView.setEnabled(false);
        } else {
            // Các cột đáp án A/B/C/D
            String label = "";
            switch (col) {
                case 1: label = "A"; break;
                case 2: label = "B"; break;
                case 3: label = "C"; break;
                case 4: label = "D"; break;
            }
            holder.tvNumber.setText(label);
            holder.tvNumber.setTextColor(Color.WHITE);

            if (selectedAnswer[row] == col) {
                holder.bgCircle.setBackgroundResource(R.drawable.bg_circle_selected);
            } else {
                holder.bgCircle.setBackgroundResource(R.drawable.bg_circle_gray);
            }

            holder.itemView.setClickable(true);
            holder.itemView.setEnabled(true);
            holder.itemView.setOnClickListener(v -> {
                selectedAnswer[row] = col;
                int startPos = row * 5 + 1;
                for (int i = startPos; i < startPos + 4; i++) {
                    notifyItemChanged(i);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // ✅ Trả về danh sách đáp án dạng ["A", "C", "B", null, ...]
    public List<String> buildAnswersList() {
        String[] answers = new String[questionCount];
        for (int i = 0; i < questionCount; i++) {
            int c = selectedAnswer[i];
            if (c == 1) answers[i] = "A";
            else if (c == 2) answers[i] = "B";
            else if (c == 3) answers[i] = "C";
            else if (c == 4) answers[i] = "D";
            else answers[i] = null;
        }
        return Arrays.asList(answers);
    }

    // ✅ NEW: Hàm dùng để gán lại đáp án cũ khi sửa
    public void setSelectedAnswers(List<String> oldAnswers) {
        if (oldAnswers == null || oldAnswers.isEmpty()) return;

        for (int i = 0; i < Math.min(oldAnswers.size(), selectedAnswer.length); i++) {
            String ans = oldAnswers.get(i);
            if ("A".equals(ans)) selectedAnswer[i] = 1;
            else if ("B".equals(ans)) selectedAnswer[i] = 2;
            else if ("C".equals(ans)) selectedAnswer[i] = 3;
            else if ("D".equals(ans)) selectedAnswer[i] = 4;
            else selectedAnswer[i] = -1;
        }
        notifyDataSetChanged(); // Cập nhật lại giao diện
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumber;
        View bgCircle;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tv_number);
            bgCircle = itemView.findViewById(R.id.bg_circle);
        }
    }

    public void updateData(List<Integer> newItemList, int newQuestionCount) {
        this.itemList = newItemList;
        this.questionCount = newQuestionCount;
        // Khởi tạo lại mảng lưu trạng thái chọn với kích thước mới
        this.selectedAnswer = new int[newQuestionCount];
        Arrays.fill(selectedAnswer, -1);
        notifyDataSetChanged();
    }

}
