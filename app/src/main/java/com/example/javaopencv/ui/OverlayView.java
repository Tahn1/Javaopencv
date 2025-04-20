package com.example.javaopencv.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class OverlayView extends View {
    private final Paint fillPaint, strokePaint, dotPaint;
    private float overlayWidth  = 245f, overlayHeight = 290f;
    private final List<PointF> markerCenters = new ArrayList<>();

    public OverlayView(Context c, AttributeSet a, int def) {
        super(c,a,def);
        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(Color.WHITE);
        fillPaint.setAlpha(204);
        strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(Color.GREEN);
        strokePaint.setStrokeWidth(6f);
        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setColor(Color.GREEN);
    }
    public OverlayView(Context c, AttributeSet a){ this(c,a,0); }
    public OverlayView(Context c){ this(c,null); }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // vẽ nền bán trong suốt cho 4 ô cố định (chỉ ví dụ, có thể bỏ nếu không cần)
        for (RectF r: getOverlayRects()) {
            canvas.drawRect(r, fillPaint);
        }
        // vẽ chấm tại vị trí marker
        for (PointF p: markerCenters) {
            canvas.drawCircle(p.x, p.y, 12f, dotPaint);
        }
    }

    /** Cập nhật vị trí các marker đã detect */
    public void setMarkerCorners(List<org.opencv.core.Point> pts) {
        markerCenters.clear();
        for (org.opencv.core.Point p: pts) {
            markerCenters.add(new PointF((float)p.x, (float)p.y));
        }
        postInvalidate();
    }

    /** Xóa hết overlay */
    public void clear() {
        markerCenters.clear();
        postInvalidate();
    }

    /** 4 ô overlay cố định (nếu muốn) */
    public RectF[] getOverlayRects() {
        float w=getWidth(), h=getHeight();
        float ow=overlayWidth, oh=overlayHeight;
        float by=(h-oh)/1.35f;
        return new RectF[]{
                new RectF(0,    0,   ow,    oh),
                new RectF(w-ow, 0,   w,     oh),
                new RectF(0,    by,  ow,    by+oh),
                new RectF(w-ow, by,  w,     by+oh)
        };
    }

    /** Thay đổi kích thước overlay */
    public void setOverlaySize(float w, float h) {
        overlayWidth = w;
        overlayHeight = h;
        invalidate();
    }
}
