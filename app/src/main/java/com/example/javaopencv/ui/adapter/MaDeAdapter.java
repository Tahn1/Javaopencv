package com.example.javaopencv.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;

import java.util.List;

public class MaDeAdapter extends RecyclerView.Adapter<MaDeAdapter.MaDeViewHolder> {

    private List<String> maDeList;

    public MaDeAdapter(List<String> maDeList) {
        this.maDeList = maDeList;
    }

    @NonNull
    @Override
    public MaDeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ma_de, parent, false);
        return new MaDeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MaDeViewHolder holder, int position) {
        String maDe = maDeList.get(position);
        holder.tvMaDe.setText(maDe);
    }

    @Override
    public int getItemCount() {
        return maDeList.size();
    }

    static class MaDeViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaDe;

        public MaDeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaDe = itemView.findViewById(R.id.tv_ma_de);
        }
    }
}
