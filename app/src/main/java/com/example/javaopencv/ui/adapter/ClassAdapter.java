package com.example.javaopencv.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.SchoolClass;

public class ClassAdapter extends ListAdapter<SchoolClass, ClassAdapter.ClassViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(SchoolClass item);
    }

    private final OnItemClickListener listener;

    public ClassAdapter(OnItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<SchoolClass> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<SchoolClass>() {
                @Override
                public boolean areItemsTheSame(@NonNull SchoolClass oldItem, @NonNull SchoolClass newItem) {
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull SchoolClass oldItem, @NonNull SchoolClass newItem) {
                    return oldItem.name.equals(newItem.name) && oldItem.subjectId == newItem.subjectId;
                }
            };

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_class, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        SchoolClass schoolClass = getItem(position);
        holder.bind(schoolClass, listener);
    }

    static class ClassViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvClassName;

        public ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClassName = itemView.findViewById(R.id.tvClassName);
        }

        public void bind(SchoolClass schoolClass, OnItemClickListener listener) {
            tvClassName.setText(schoolClass.name);
            itemView.setOnClickListener(v -> listener.onItemClick(schoolClass));
        }
    }
}