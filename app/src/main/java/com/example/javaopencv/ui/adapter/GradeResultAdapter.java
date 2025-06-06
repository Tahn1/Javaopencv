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

import java.util.Locale;

public class GradeResultAdapter
        extends ListAdapter<GradeResult, GradeResultAdapter.VH> {

    /** 1) Interface để lắng nghe click */
    public interface OnItemClickListener {
        void onItemClick(GradeResult item);
    }
    private OnItemClickListener clickListener;
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    /** 2) Interface để lắng nghe long‑click (xóa) */
    public interface OnItemLongClickListener {
        void onItemLongClick(GradeResult item);
    }
    private OnItemLongClickListener longClickListener;
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
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

        // 1) Load ảnh debug đã chấm (hoặc placeholder màu xám nếu null)
        if (r.imagePath != null && !r.imagePath.isEmpty()) {
            h.ivPreview.setImageURI(Uri.parse(r.imagePath));
        } else {
            h.ivPreview.setImageDrawable(new ColorDrawable(Color.LTGRAY));
        }

        // 2) Thiết lập crop/zoom vùng focus
        final float focusX = r.focusX;
        final float focusY = r.focusY;
        h.ivPreview.setScaleType(ImageView.ScaleType.MATRIX);
        h.ivPreview.post(() -> {
            Drawable d = h.ivPreview.getDrawable();
            if (d == null) return;

            int dw = d.getIntrinsicWidth();
            int dh = d.getIntrinsicHeight();
            int vw = h.ivPreview.getWidth();
            int vh = h.ivPreview.getHeight();

            float baseScale = Math.max(vw / (float) dw, vh / (float) dh);
            float zoomFactor = 2.69f;
            float scale = baseScale * zoomFactor;

            float cropW = vw / scale;
            float cropH = vh / scale;

            float centerX = dw * focusX;
            float centerY = dh * focusY;

            float left = centerX - cropW / 1.75f;
            float top  = centerY - cropH / 0.15f;

            left = Math.max(0f, Math.min(left, dw - cropW));
            top  = Math.max(0f, Math.min(top, dh - cropH));

            Matrix m = new Matrix();
            m.setScale(scale, scale);
            m.postTranslate(-left * scale, -top * scale);
            h.ivPreview.setImageMatrix(m);
        });

        // 3) Hiển thị số báo danh và điểm
        h.tvSbd.setText(r.sbd);
        h.tvScore.setText(String.format(Locale.getDefault(), "%.2f", r.score));

        // 4) Thiết lập click lên toàn item
        h.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(r);
            }
        });

        // 5) Thiết lập long‑click để xóa
        h.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(r);
                return true;
            }
            return false;
        });
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivPreview;
        TextView  tvSbd, tvScore;

        VH(@NonNull View itemView) {
            super(itemView);
            ivPreview = itemView.findViewById(R.id.ivPreview);
            tvSbd     = itemView.findViewById(R.id.tvSbd);
            tvScore   = itemView.findViewById(R.id.tvScore);
        }
    }
}
