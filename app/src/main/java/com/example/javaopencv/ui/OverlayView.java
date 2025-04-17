package com.example.javaopencv.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import org.opencv.core.MatOfPoint;
import com.example.javaopencv.omr.MarkerUtils;

import java.util.ArrayList;
import java.util.List;

public class OverlayView extends View {
    private final Paint fillPaint;
    private float overlayWidth = 180f;
    private float overlayHeight = 240f;

    public OverlayView(Context context) {
        super(context);
        fillPaint = createPaint();
    }

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        fillPaint = createPaint();
    }

    public OverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        fillPaint = createPaint();
    }

    private Paint createPaint() {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.WHITE);
        p.setAlpha(204); // 80% opacity
        return p;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        float ow = overlayWidth;
        float oh = overlayHeight;

        // Top-left
        canvas.drawRect(0, 0, ow, oh, fillPaint);
        // Top-right
        canvas.drawRect(w - ow, 0, w, oh, fillPaint);

        // Bottom-left & Bottom-right positioned around middle vertically
        float bottomY = (h - oh) / 1.5f;
        canvas.drawRect(0, bottomY, ow, bottomY + oh, fillPaint);
        canvas.drawRect(w - ow, bottomY, w, bottomY + oh, fillPaint);
    }

    /** Trả về 4 vùng RectF tương ứng 4 ô overlay */
    public RectF[] getOverlayRects() {
        float w = getWidth();
        float h = getHeight();
        float ow = overlayWidth;
        float oh = overlayHeight;
        float bottomY = (h - oh) / 2f;
        return new RectF[]{
                new RectF(0, 0, ow, oh),
                new RectF(w - ow, 0, w, oh),
                new RectF(0, bottomY, ow, bottomY + oh),
                new RectF(w - ow, bottomY, w, bottomY + oh)
        };
    }

    /**
     * Kiểm tra xem đã phát hiện đủ 4 marker nằm trong 4 ô overlay chưa
     */
    public boolean allCornersHaveMarker(List<MatOfPoint> markers) {
        List<org.opencv.core.Point> centers = new ArrayList<>();
        for (MatOfPoint m : markers) {
            centers.add(MarkerUtils.centerOf(m));
        }
        RectF[] rects = getOverlayRects();
        int match = 0;
        for (RectF rect : rects) {
            for (org.opencv.core.Point p : centers) {
                if (rect.contains((float) p.x, (float) p.y)) {
                    match++;
                    break;
                }
            }
        }
        return match >= 4;
    }

    /** Cho phép điều chỉnh kích thước overlay từ code (px) */
    public void setOverlaySize(float widthPx, float heightPx) {
        this.overlayWidth = widthPx;
        this.overlayHeight = heightPx;
        invalidate();
    }
}
