package com.example.javaopencv.ui.adapter;

import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.GradeResult;
import com.example.javaopencv.data.entity.Student;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class GradeResultAdapter
        extends ListAdapter<GradeResult, GradeResultAdapter.VH> {

    public interface OnItemClickListener {
        void onItemClick(GradeResult item);
    }
    private OnItemClickListener clickListener;
    public void setOnItemClickListener(OnItemClickListener l) {
        this.clickListener = l;
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(GradeResult item);
    }
    private OnItemLongClickListener longClickListener;
    public void setOnItemLongClickListener(OnItemLongClickListener l) {
        this.longClickListener = l;
    }

    // Map studentNumber -> Student
    private Map<String, Student> studentMap = new HashMap<>();
    /** Phải được gọi sau khi load xong danh sách Student của lớp */
    public void setStudentMap(Map<String, Student> map) {
        this.studentMap = (map != null ? map : new HashMap<>());
        notifyDataSetChanged();
    }

    public GradeResultAdapter() {
        super(new DiffUtil.ItemCallback<GradeResult>() {
            @Override public boolean areItemsTheSame(@NonNull GradeResult a, @NonNull GradeResult b) {
                return a.id == b.id;
            }
            @Override public boolean areContentsTheSame(@NonNull GradeResult a, @NonNull GradeResult b) {
                return a.equals(b);
            }
        });
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grade_result, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        GradeResult r = getItem(pos);

        // reset lại trạng thái
        h.ivResult.setVisibility(View.VISIBLE);
        h.tvStudentNamePreview.setVisibility(View.GONE);

        // 1) Load ảnh debug hoặc placeholder
        if (r.imagePath != null && !r.imagePath.isEmpty()) {
            h.ivResult.setImageURI(Uri.parse(r.imagePath));
        } else {
            h.ivResult.setImageDrawable(new ColorDrawable(Color.LTGRAY));
        }

        // 2) Crop/zoom vùng focus
        final float fx = r.focusX, fy = r.focusY;
        h.ivResult.setScaleType(ImageView.ScaleType.MATRIX);
        h.ivResult.post(() -> {
            Drawable d = h.ivResult.getDrawable();
            if (d == null) return;
            int dw = d.getIntrinsicWidth(), dh = d.getIntrinsicHeight();
            int vw = h.ivResult.getWidth(), vh = h.ivResult.getHeight();
            float base = Math.max(vw/(float)dw, vh/(float)dh);
            float scale = base * 2.69f;
            float cropW = vw/scale, cropH = vh/scale;
            float cx = dw * fx, cy = dh * fy;
            float left = Math.max(0f, Math.min(cx - cropW/1.75f, dw - cropW));
            float top  = Math.max(0f, Math.min(cy - cropH/0.15f, dh - cropH));
            Matrix m = new Matrix();
            m.setScale(scale, scale);
            m.postTranslate(-left * scale, -top * scale);
            h.ivResult.setImageMatrix(m);
        });

        // 3) Số báo danh & điểm
        h.tvSbd.setText(r.sbd);
        h.tvScore.setText(String.format(Locale.getDefault(), "%.2f", r.score));

        // 4) Hiển thị mã đề ngay dưới điểm
        h.tvMaDe.setText("Mã đề: " + r.maDe);

        // 5) Lookup tên HS từ map (nếu có) – thay ảnh bằng tên
        Student stu = studentMap.get(r.sbd != null ? r.sbd.trim() : "");
        if (stu != null) {
            h.ivResult.setVisibility(View.INVISIBLE);
            h.tvStudentNamePreview.setVisibility(View.VISIBLE);
            h.tvStudentNamePreview.setText(stu.getName());
        }

        // 6) Click / Long-click
        h.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onItemClick(r);
        });
        h.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(r);
                return true;
            }
            return false;
        });
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView ivResult;
        final TextView tvStudentNamePreview, tvSbd, tvScore, tvMaDe;

        VH(@NonNull View itemView) {
            super(itemView);
            ivResult             = itemView.findViewById(R.id.ivResult);
            tvStudentNamePreview = itemView.findViewById(R.id.tvStudentNamePreview);
            tvSbd                = itemView.findViewById(R.id.tvSbd);
            tvScore              = itemView.findViewById(R.id.tvScore);
            tvMaDe               = itemView.findViewById(R.id.tvMaDe);    // mới
        }
    }
}
