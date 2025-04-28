package com.example.javaopencv.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.ClassWithCount;
import com.example.javaopencv.data.entity.SchoolClass;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.VH> {

    private List<ClassWithCount> list = new ArrayList<>();
    private final OnItemClickListener clickListener;
    private final OnItemLongClickListener longClickListener;

    public interface OnItemClickListener { void onClick(SchoolClass sc); }
    public interface OnItemLongClickListener { void onLongClick(ClassWithCount cc); }

    public ClassAdapter(OnItemClickListener click, OnItemLongClickListener longClick) {
        this.clickListener = click;
        this.longClickListener = longClick;
    }

    public void submitList(List<ClassWithCount> data) {
        list = data != null ? data : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_class, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Context ctx = h.itemView.getContext();
        ClassWithCount cc = list.get(pos);
        SchoolClass sc = cc.getKlass(); // use getter

        // 1) Tên lớp
        h.tvClassName.setText(sc.getName());

        // 2) Số học sinh: dùng getter và plurals
        int count = cc.getStudentCount();
        String studentCountStr = ctx.getResources().getQuantityString(
                R.plurals.student_count,
                count,
                count
        );
        h.tvStudentCount.setText(studentCountStr);

        // 3) Ngày tạo: parse rồi format
        try {
            SimpleDateFormat inFmt = new SimpleDateFormat("d/M/yyyy", new Locale("vi"));
            Date date = inFmt.parse(sc.getDateCreated());
            SimpleDateFormat outFmt = new SimpleDateFormat("d-'Thg' M-yyyy", new Locale("vi"));
            h.tvDateValue.setText(outFmt.format(date));
        } catch (Exception e) {
            h.tvDateValue.setText(sc.getDateCreated());
        }

        // 4) Click / Long-click
        h.itemView.setOnClickListener(v -> clickListener.onClick(sc));
        h.itemView.setOnLongClickListener(v -> {
            longClickListener.onLongClick(cc);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvClassName, tvStudentCount, tvDateValue;

        VH(@NonNull View itemView) {
            super(itemView);
            tvClassName = itemView.findViewById(R.id.tvClassName);
            tvStudentCount = itemView.findViewById(R.id.tvStudentCount);
            tvDateValue = itemView.findViewById(R.id.tvDateValue);
        }
    }
}
