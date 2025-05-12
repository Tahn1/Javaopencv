package com.example.javaopencv.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.GradeResult;

import java.util.List;

/**
 * Adapter hiển thị danh sách GradeResult sai mã đề.
 */
public class DapSaiAdapter extends RecyclerView.Adapter<DapSaiAdapter.VH> {
    private final List<GradeResult> items;

    public DapSaiAdapter(List<GradeResult> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dap_sai, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        GradeResult gr = items.get(position);
        holder.tvSbd.setText("SBD: " + gr.getSbd());
        holder.tvMaDe.setText("Mã đề sai: " + gr.getMaDe());
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvSbd, tvMaDe;

        VH(@NonNull View itemView) {
            super(itemView);
            tvSbd  = itemView.findViewById(R.id.tvSbdWrong);
            tvMaDe = itemView.findViewById(R.id.tvMaDeWrong);
        }
    }
}
