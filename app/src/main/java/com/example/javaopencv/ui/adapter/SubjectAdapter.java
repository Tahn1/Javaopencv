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
import com.example.javaopencv.data.entity.Subject;

public class SubjectAdapter extends ListAdapter<Subject, SubjectAdapter.SubjectViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(Subject item);
    }

    private final OnItemClickListener listener;

    public SubjectAdapter(OnItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Subject> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Subject>() {
                @Override
                public boolean areItemsTheSame(@NonNull Subject oldItem, @NonNull Subject newItem) {
                    return oldItem.id == newItem.id;
                }
                @Override
                public boolean areContentsTheSame(@NonNull Subject oldItem, @NonNull Subject newItem) {
                    return oldItem.name.equals(newItem.name);
                }
            };

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subject, parent, false);
        return new SubjectViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        Subject s = getItem(position);
        holder.bind(s, listener);
    }

    static class SubjectViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvSubjectName);
        }
        public void bind(Subject sub, OnItemClickListener l) {
            tvName.setText(sub.name);
            itemView.setOnClickListener(v -> l.onItemClick(sub));
        }
    }
}
