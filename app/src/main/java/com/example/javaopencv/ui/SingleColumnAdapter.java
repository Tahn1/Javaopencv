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

    public void setSelectedDigit(String digit) {
        for (int i = 0; i < digits.size(); i++) {
            if (digits.get(i).equals(digit)) {
                selectedPosition = i;
                notifyDataSetChanged();
                return;
            }
        }
        selectedPosition = RecyclerView.NO_POSITION;
        notifyDataSetChanged();
    }

    public String getSelectedDigit() {
        return selectedPosition != RecyclerView.NO_POSITION ? digits.get(selectedPosition) : null;
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
        if (position == selectedPosition) {
            holder.bgCircle.setBackgroundResource(R.drawable.bg_circle_selected);
        } else {
            holder.bgCircle.setBackgroundResource(R.drawable.bg_circle_gray);
        }
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
