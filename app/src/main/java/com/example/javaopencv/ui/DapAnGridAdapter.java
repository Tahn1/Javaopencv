package com.example.javaopencv.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.javaopencv.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DapAnGridAdapter extends RecyclerView.Adapter<DapAnGridAdapter.ViewHolder> {

    private List<Integer> itemList; // Tổng số item = questionCount * 5
    private int questionCount;      // Số câu được truyền xuống
    // Mảng lưu đáp án đã chọn: -1 nếu chưa chọn, 1->A, 2->B, 3->C, 4->D
    private int[] selectedAnswer;

    public DapAnGridAdapter(List<Integer> itemList, int questionCount) {
        this.itemList = itemList;
        this.questionCount = questionCount;
        selectedAnswer = new int[questionCount];
        Arrays.fill(selectedAnswer, -1);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_circle, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int row = position / 5;  // Hàng từ 0 đến questionCount - 1
        int col = position % 5;  // Cột từ 0 đến 4

        if (col == 0) {
            // Cột số thứ tự: hiển thị số câu (row + 1)
            holder.tvNumber.setText(String.valueOf(row + 1));
            holder.tvNumber.setTextColor(Color.BLACK);
            holder.bgCircle.setBackgroundResource(R.drawable.bg_circle_white_orange_border);
            holder.itemView.setClickable(false);
            holder.itemView.setEnabled(false);
        } else {
            // Các cột 1-4: hiển thị đáp án "A", "B", "C", "D"
            String label;
            switch (col) {
                case 1: label = "A"; break;
                case 2: label = "B"; break;
                case 3: label = "C"; break;
                case 4: label = "D"; break;
                default: label = "?"; break;
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
                // Cập nhật lại các ô trong hàng đó (từ vị trí row*5 + 1 đến row*5 + 4)
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

    // Trả về danh sách đáp án dạng List<String>
    public List<String> buildAnswersList() {
        String[] answers = new String[questionCount];
        for (int i = 0; i < questionCount; i++) {
            int c = selectedAnswer[i];
            if (c == 1)
                answers[i] = "A";
            else if (c == 2)
                answers[i] = "B";
            else if (c == 3)
                answers[i] = "C";
            else if (c == 4)
                answers[i] = "D";
            else
                answers[i] = null;
        }
        return Arrays.asList(answers);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumber;
        View bgCircle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tv_number);
            bgCircle = itemView.findViewById(R.id.bg_circle);
        }
    }
}
