package com.example.javaopencv.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.ClassWithCount;
import com.example.javaopencv.data.entity.SchoolClass;

import java.util.ArrayList;
import java.util.List;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.VH> {
    private final List<ClassWithCount> list = new ArrayList<>();
    private final OnItemClickListener clickListener;
    private final OnItemLongClickListener longClickListener;

    public interface OnItemClickListener {
        void onClick(SchoolClass sc);
    }
    public interface OnItemLongClickListener {
        void onLongClick(ClassWithCount cc);
    }

    public ClassAdapter(OnItemClickListener clickListener,
                        OnItemLongClickListener longClickListener) {
        this.clickListener     = clickListener;
        this.longClickListener = longClickListener;
    }

    /**
     * Gọi mỗi khi LiveData emit
     */
    public void submitList(List<ClassWithCount> data) {
        list.clear();
        if (data != null) list.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_class, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        ClassWithCount cc = list.get(pos);
        h.tvClassName.setText(cc.klass.getName());
        h.tvDateValue.setText(cc.klass.getDateCreated());
        h.tvStudentCount.setText(String.valueOf(cc.studentCount));
        h.itemView.setOnClickListener(v -> clickListener.onClick(cc.klass));
        h.itemView.setOnLongClickListener(v -> {
            longClickListener.onLongClick(cc);
            return true;
        });
    }

    @Override public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvClassName, tvDateValue, tvStudentCount;
        VH(@NonNull View itemView) {
            super(itemView);
            tvClassName    = itemView.findViewById(R.id.tvClassName);
            tvDateValue    = itemView.findViewById(R.id.tvDateValue);
            tvStudentCount = itemView.findViewById(R.id.tvStudentCount);
        }
    }
}
