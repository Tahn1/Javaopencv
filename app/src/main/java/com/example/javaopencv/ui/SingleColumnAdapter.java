package com.example.javaopencv.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;

import java.util.List;

public class SingleColumnAdapter extends RecyclerView.Adapter<SingleColumnAdapter.ViewHolder> {

    private List<String> digits;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public SingleColumnAdapter(List<String> digits) {
        this.digits = digits;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_circle, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String number = digits.get(position);
        holder.tvNumber.setText(number);

        // Nếu đây là item được chọn, đặt background khác
        if (position == selectedPosition) {
            holder.bgCircle.setBackgroundResource(R.drawable.bg_circle_selected);
        } else {
            holder.bgCircle.setBackgroundResource(R.drawable.bg_circle_gray);
        }

        // Xử lý click: đánh dấu item được chọn
        holder.itemView.setOnClickListener(v -> {
            int oldPos = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            if (oldPos != RecyclerView.NO_POSITION) {
                notifyItemChanged(oldPos);
            }
            notifyItemChanged(selectedPosition);
        });
    }

    @Override
    public int getItemCount() {
        return digits.size();
    }

    public String getSelectedDigit() {
        if (selectedPosition == RecyclerView.NO_POSITION) return null;
        return digits.get(selectedPosition);
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
