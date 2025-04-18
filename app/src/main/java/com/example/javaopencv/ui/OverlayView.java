// OverlayView.java
package com.example.javaopencv.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OverlayView extends View {
    private final Paint fillPaint, strokePaint;
    private float overlayWidth  = 245f;
    private float overlayHeight = 290f;
    private final boolean[] hasMarker    = new boolean[4];
    private final RectF[]    markerBounds = new RectF[4];

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

        Arrays.fill(markerBounds, null);
    }
    public OverlayView(Context c, AttributeSet a){ this(c,a,0); }
    public OverlayView(Context c){ this(c,null); }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        RectF[] rects = getOverlayRects();
        // 1) nền bán trong suốt
        for (RectF r: rects) canvas.drawRect(r, fillPaint);
        // 2) khung xanh quanh marker
        for (int i=0;i<4;i++){
            if (hasMarker[i] && markerBounds[i]!=null) {
                canvas.drawRect(markerBounds[i], strokePaint);
            }
        }
    }

    /**
     * Cập nhật marker dựa vào Mat nhị phân:
     * - morphological open
     * - tìm contour trong mỗi ô
     * - kiểm tra polygon 4 cạnh gần vuông
     */
    public void updateByThresholdMap(Mat bin) {
        // reset
        Arrays.fill(hasMarker, false);
        for (int i = 0; i < 4; i++) markerBounds[i] = null;

        // kernel nhỏ để mở (loại nhiễu li ti)
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5,5));
        Mat clean = new Mat();
        Imgproc.morphologyEx(bin, clean, Imgproc.MORPH_OPEN, kernel);

        // tỉ lệ view→mat
        float scaleX = clean.cols() / (float)getWidth();
        float scaleY = clean.rows() / (float)getHeight();

        RectF[] overlayRects = getOverlayRects();
        for (int idx = 0; idx < overlayRects.length; idx++) {
            RectF vr = overlayRects[idx];
            int x = Math.max(0, Math.round(vr.left  * scaleX));
            int y = Math.max(0, Math.round(vr.top   * scaleY));
            int w = Math.round(vr.width()  * scaleX);
            int h = Math.round(vr.height() * scaleY);
            if (x + w > clean.cols())  w = clean.cols() - x;
            if (y + h > clean.rows())  h = clean.rows() - y;
            if (w <= 0 || h <= 0) continue;

            Mat roi = new Mat(clean, new Rect(x, y, w, h));
            List<MatOfPoint> contours = new ArrayList<>();
            Imgproc.findContours(roi, contours, new Mat(),
                    Imgproc.RETR_EXTERNAL,
                    Imgproc.CHAIN_APPROX_SIMPLE);

            double roiArea = w * (double)h;
            double bestArea = 0;
            Rect bestBB = null;

            for (MatOfPoint cnt : contours) {
                double area = Imgproc.contourArea(cnt);
                // chỉ lấy area giữa 1% và 80% ROI
                if (area < roiArea * 0.01 || area > roiArea * 0.8) {
                    continue;
                }
                // kiểm tra polygon 4 cạnh
                MatOfPoint2f cnt2f = new MatOfPoint2f(cnt.toArray());
                double peri = Imgproc.arcLength(cnt2f, true);
                MatOfPoint2f approx = new MatOfPoint2f();
                Imgproc.approxPolyDP(cnt2f, approx, 0.02 * peri, true);

                if (approx.total() == 4) {
                    // bounding box
                    Rect bb = Imgproc.boundingRect(new MatOfPoint(approx.toArray()));
                    // kiểm tra gần vuông
                    double ar = bb.width / (double) bb.height;
                    if (ar > 0.8 && ar < 1.2 && area > bestArea) {
                        bestArea = area;
                        bestBB   = bb;
                    }
                }
                cnt2f.release();
                approx.release();
            }

            if (bestBB != null) {
                hasMarker[idx] = true;
                // chuyển về tọa độ view
                float left   = (x + bestBB.x)        / scaleX;
                float top    = (y + bestBB.y)        / scaleY;
                float right  = (x + bestBB.x + bestBB.width)  / scaleX;
                float bottom = (y + bestBB.y + bestBB.height) / scaleY;
                markerBounds[idx] = new RectF(left, top, right, bottom);
            }

            roi.release();
        }

        clean.release();
        kernel.release();
        postInvalidate();
    }


    public RectF[] getOverlayRects() {
        float w=getWidth(), h=getHeight(),
                ow=overlayWidth, oh=overlayHeight,
                by=(h-oh)/1.35f;
        return new RectF[]{
                new RectF(0,0,ow,oh),
                new RectF(w-ow,0,w,oh),
                new RectF(0,by,ow,by+oh),
                new RectF(w-ow,by,w,by+oh)
        };
    }

    public boolean allCornersHaveMarker() {
        return hasMarker[0]&&hasMarker[1]
                &&hasMarker[2]&&hasMarker[3];
    }

    public void setOverlaySize(float widthPx, float heightPx) {
        overlayWidth=widthPx;
        overlayHeight=heightPx;
        invalidate();
    }
}
