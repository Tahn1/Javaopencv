package com.example.javaopencv.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.ExamCodeEntry;

import java.util.ArrayList;
import java.util.List;

public class ExamCodeAdapter extends RecyclerView.Adapter<ExamCodeAdapter.ViewHolder> {

    private List<ExamCodeEntry> examCodeList = new ArrayList<>();

    // Listener cho sự kiện click item
    public interface OnItemClickListener {
        void onItemClick(ExamCodeEntry entry);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener clickListener) {
        this.listener = clickListener;
    }

    // Cập nhật dữ liệu
    public void setExamCodeList(List<ExamCodeEntry> list) {
        examCodeList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExamCodeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout item_exam_code
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exam_code, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamCodeAdapter.ViewHolder holder, int position) {
        ExamCodeEntry entry = examCodeList.get(position);

        // Label "MÃ ĐỀ" ta đã đặt cứng trong XML,
        // ta chỉ cần setText cho TextView hiển thị code
        holder.tvCode.setText(entry.code != null ? entry.code : "---");

        // Xử lý click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(entry);
            }
        });
    }

    @Override
    public int getItemCount() {
        return examCodeList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCode;
        // tv_ma_de_label đã text cứng "MÃ ĐỀ" trong XML,
        // nên ta có thể bỏ qua, hoặc bind nếu muốn thay đổi
        // TextView tvMaDeLabel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // tvMaDeLabel = itemView.findViewById(R.id.tv_ma_de_label); // cứng sẵn
            tvCode = itemView.findViewById(R.id.tv_code);
        }
    }
}
