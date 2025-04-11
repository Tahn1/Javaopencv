package com.example.javaopencv.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;

import java.util.List;

public class SingleColumnAdapter extends RecyclerView.Adapter<SingleColumnAdapter.ViewHolder> {

    private List<String> digitList;
    private int selectedPosition = -1;  // Vị trí số đang chọn

    public SingleColumnAdapter(List<String> digitList) {
        this.digitList = digitList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_digit, parent, false);  // item_digit.xml chứa TextView
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String digit = digitList.get(position);
        holder.tvDigit.setText(digit);

        // Highlight số được chọn
        holder.tvDigit.setSelected(position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousSelected); // Cập nhật lại item cũ
            notifyItemChanged(selectedPosition); // Cập nhật item mới
        });
    }

    @Override
    public int getItemCount() {
        return digitList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDigit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDigit = itemView.findViewById(R.id.tv_digit);
        }
    }

    // ✅ Hàm lấy số đang được chọn
    public String getSelectedDigit() {
        if (selectedPosition >= 0 && selectedPosition < digitList.size()) {
            return digitList.get(selectedPosition);
        }
        return null;
    }

    // ✅ Hàm mới: Đặt chọn số theo giá trị
    public void setSelectedDigit(String digit) {
        if (digitList == null) return;
        int index = digitList.indexOf(digit);
        if (index != -1) {
            selectedPosition = index;
            notifyDataSetChanged();
        }
    }
}
