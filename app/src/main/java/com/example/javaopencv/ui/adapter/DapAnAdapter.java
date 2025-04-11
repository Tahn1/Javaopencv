package com.example.javaopencv.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import com.example.javaopencv.R;

public class DapAnAdapter extends RecyclerView.Adapter<DapAnAdapter.ViewHolder> {

    private final List<String> maDeList = new ArrayList<>();

    @NonNull
    @Override
    public DapAnAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ma_de, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DapAnAdapter.ViewHolder holder, int position) {
        holder.tvMaDe.setText(maDeList.get(position));
    }

    @Override
    public int getItemCount() {
        return maDeList.size();
    }

    public void addMaDe(String maDe) {
        maDeList.add(maDe);
        notifyItemInserted(maDeList.size() - 1);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaDe;

        ViewHolder(View itemView) {
            super(itemView);
            tvMaDe = itemView.findViewById(R.id.tv_ma_de);
        }
    }
}
