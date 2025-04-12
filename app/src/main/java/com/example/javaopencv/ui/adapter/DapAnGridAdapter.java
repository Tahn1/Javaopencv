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

    private List<Integer> itemList; // chứa index = row*5 + col
    private int questionCount;      // số câu
    private int[] selectedAnswer;   // selectedAnswer[row] = col (1..4), -1 nếu chưa chọn

    public DapAnGridAdapter(List<Integer> itemList, int questionCount) {
        this.itemList = itemList;
        this.questionCount = questionCount;
        selectedAnswer = new int[questionCount];
        Arrays.fill(selectedAnswer, -1); // -1 => chưa chọn
    }

    @Override
    public int getItemCount() {
        return itemList.size(); // = questionCount*5
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
        int row = position / 5;  // dòng
        int col = position % 5;  // cột (0..4)

        if (col == 0) {
            // Cột 0 => STT câu
            holder.tvNumber.setText(String.valueOf(row + 1));
            holder.tvNumber.setTextColor(Color.BLACK);
            holder.bgCircle.setBackgroundResource(R.drawable.bg_circle_white_orange_border);

            holder.itemView.setClickable(false);
            holder.itemView.setEnabled(false);

        } else {
            // cột 1..4 => A/B/C/D
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
                // Ô được chọn => highlight
                holder.bgCircle.setBackgroundResource(R.drawable.bg_circle_selected);
            } else {
                holder.bgCircle.setBackgroundResource(R.drawable.bg_circle_gray);
            }

            holder.itemView.setClickable(true);
            holder.itemView.setEnabled(true);
            holder.itemView.setOnClickListener(v -> {
                // user chọn đáp án col cho row
                selectedAnswer[row] = col;

                // Refresh 4 cột (A,B,C,D) của row
                int startPos = row * 5 + 1;
                for (int i = startPos; i < startPos + 4; i++) {
                    notifyItemChanged(i);
                }
            });
        }
    }

    /**
     * Trả về danh sách độ dài questionCount,
     * mỗi phần tử = "A"/"B"/"C"/"D" hoặc null
     */
    public List<String> buildAnswersList() {
        String[] answers = new String[questionCount];
        for (int i = 0; i < questionCount; i++) {
            int c = selectedAnswer[i];
            switch (c) {
                case 1: answers[i] = "A"; break;
                case 2: answers[i] = "B"; break;
                case 3: answers[i] = "C"; break;
                case 4: answers[i] = "D"; break;
                default: answers[i] = null;
            }
        }
        return Arrays.asList(answers);
    }

    /**
     * Set lại các ô được chọn dựa trên danh sách cũ
     * cỡ danh sách = questionCount.
     */
    public void setSelectedAnswers(List<String> oldAnswers) {
        // fill -1
        Arrays.fill(selectedAnswer, -1);

        for (int i = 0; i < Math.min(oldAnswers.size(), questionCount); i++) {
            String ans = oldAnswers.get(i);
            if ("A".equals(ans)) selectedAnswer[i] = 1;
            else if ("B".equals(ans)) selectedAnswer[i] = 2;
            else if ("C".equals(ans)) selectedAnswer[i] = 3;
            else if ("D".equals(ans)) selectedAnswer[i] = 4;
            else selectedAnswer[i] = -1; // null => -1
        }
        notifyDataSetChanged();
    }

    /**
     * Cập nhật data khi questionCount thay đổi
     */
    public void updateData(List<Integer> newItemList, int newQuestionCount) {
        this.itemList = newItemList;
        this.questionCount = newQuestionCount;
        // Giữ selectedAnswer cũ (nếu row >= newCount => cắt bớt)
        selectedAnswer = Arrays.copyOf(selectedAnswer, newQuestionCount);
        notifyDataSetChanged();
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
