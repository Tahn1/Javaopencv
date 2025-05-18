package com.example.javaopencv.ui.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.GradeResult;

import java.util.List;

public class WrongSbdAdapter
        extends RecyclerView.Adapter<WrongSbdAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(GradeResult gr);
    }

    private final List<GradeResult> items;
    private final OnItemClickListener listener;

    // Thêm listener vào constructor
    public WrongSbdAdapter(List<GradeResult> items,
                           OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_wrong_sbd, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {
        GradeResult gr = items.get(position);

        holder.tvSbd.setText("SBD: " + (gr.getSbd() != null ? gr.getSbd() : ""));
        holder.tvScore.setText("Điểm: " + gr.getScore());
        holder.tvMaDe.setText("Mã đề: " + (gr.getMaDe() != null ? gr.getMaDe() : ""));

        // Bind ảnh nếu có
        String path = gr.getImagePath();
        if (path != null && !path.isEmpty()) {
            Bitmap bmp = BitmapFactory.decodeFile(path);
            if (bmp != null) holder.ivResult.setImageBitmap(bmp);
            else holder.ivResult.setImageResource(R.drawable.ic_placeholder);
        } else {
            holder.ivResult.setImageResource(R.drawable.ic_placeholder);
        }

        // Đăng ký click
        holder.itemView.setOnClickListener(v -> listener.onItemClick(gr));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivResult;
        TextView tvSbd, tvScore, tvMaDe;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivResult = itemView.findViewById(R.id.ivResult);
            tvSbd    = itemView.findViewById(R.id.tvSbd);
            tvScore  = itemView.findViewById(R.id.tvScore);
            tvMaDe   = itemView.findViewById(R.id.tvMaDe);
        }
    }
}
