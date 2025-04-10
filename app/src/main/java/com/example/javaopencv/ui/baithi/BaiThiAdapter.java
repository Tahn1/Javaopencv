package com.example.javaopencv.ui.baithi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.javaopencv.R;

import java.util.List;

public class BaiThiAdapter extends RecyclerView.Adapter<BaiThiAdapter.ViewHolder> {

    private List<BaiThiMenuItem> menuItems;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(BaiThiMenuItem item);
    }

    public BaiThiAdapter(List<BaiThiMenuItem> menuItems, OnItemClickListener listener) {
        this.menuItems = menuItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BaiThiAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BaiThiAdapter.ViewHolder holder, int position) {
        BaiThiMenuItem item = menuItems.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgLeft, imgRight;
        TextView tvLabel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgLeft = itemView.findViewById(R.id.img_left);
            imgRight = itemView.findViewById(R.id.img_right);
            tvLabel = itemView.findViewById(R.id.tv_label);
        }

        public void bind(final BaiThiMenuItem item, final OnItemClickListener listener) {
            imgLeft.setImageResource(item.iconRes);
            tvLabel.setText(item.label);

            // Khi user nhấn vào item
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}
