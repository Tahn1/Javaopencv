package com.example.javaopencv.omr;

import android.content.Context;
import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.imgproc.Moments;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MarkerUtils {
    private static final String TAG = "MarkerUtils";

    // ---------------------- Marker Large Detection ----------------------
    /**
     * Tìm marker lớn (hình gần tròn, 4 cạnh) trong ảnh, tự động chuyển kênh gray nếu cần.
     */
    public static List<MatOfPoint> findMarkers(
            Mat image,
            double minArea,
            double maxArea,
            double circularityLow,
            double circularityHigh
    ) {
        // 1) Chuẩn bị ảnh gray
        Mat gray = new Mat();
        int ch = image.channels();
        if (ch == 1) {
            gray = image.clone();
        } else if (ch == 3) {
            Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
        } else if (ch == 4) {
            Imgproc.cvtColor(image, gray, Imgproc.COLOR_RGBA2GRAY);
        } else {
            throw new IllegalArgumentException("Unsupported number of channels: " + ch);
        }

        // 2) Threshold với Otsu + đảo
        Mat thresh = new Mat();
        Imgproc.threshold(gray, thresh, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
        Imgcodecs.imwrite("debug_threshold_large.jpg", thresh);

        // 3) Tìm contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(thresh, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Log.d(TAG, "findMarkers: Contours found: " + contours.size());

        // 4) Vẽ debug contours
        Mat debugContoursImg = new Mat();
        Imgproc.cvtColor(gray, debugContoursImg, Imgproc.COLOR_GRAY2BGR);
        Imgproc.drawContours(debugContoursImg, contours, -1, new Scalar(0, 0, 255), 2);
        Imgcodecs.imwrite("debug_initial_contours_large.jpg", debugContoursImg);

        // 5) Lọc theo area, circularity, approxPolyDP
        List<MatOfPoint> markers = new ArrayList<>();
        for (MatOfPoint cnt : contours) {
            double area = Imgproc.contourArea(cnt);
            Log.d(TAG, "Processing contour, area = " + area);
            if (area < minArea || area > maxArea) {
                Log.d(TAG, " - Loại: area không trong [" + minArea + ", " + maxArea + "]");
                continue;
            }
            MatOfPoint2f cnt2f = new MatOfPoint2f(cnt.toArray());
            double perim = Imgproc.arcLength(cnt2f, true);
            if (perim == 0) continue;
            double circ = 4 * Math.PI * area / (perim * perim);
            Log.d(TAG, String.format(" - circularity = %.3f", circ));
            if (circ < circularityLow || circ > circularityHigh) {
                Log.d(TAG, " - Loại: circularity không trong [" + circularityLow + ", " + circularityHigh + "]");
                continue;
            }
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(cnt2f, approx, 0.02 * perim, true);
            if (approx.total() == 4) {
                // chuyển về 4 điểm int
                RotatedRect rect = Imgproc.minAreaRect(cnt2f);
                Point[] box = new Point[4];
                rect.points(box);
                for (int i = 0; i < 4; i++) {
                    box[i].x = Math.round(box[i].x);
                    box[i].y = Math.round(box[i].y);
                }
                markers.add(new MatOfPoint(box));
                Log.d(TAG, " -> Thêm marker: " + Arrays.toString(box));
            }
        }
        Log.d(TAG, "findMarkers: Total markers: " + markers.size());
        return markers;
    }

    /** Tính tâm contour/marker */
    public static Point centerOf(MatOfPoint marker) {
        Moments m = Imgproc.moments(marker);
        return new Point(m.get_m10() / m.get_m00(), m.get_m01() / m.get_m00());
    }

    // ---------------------- Debug Large ----------------------
    public static void debugLargeMarkers(Mat image, double minArea, double maxArea, Context ctx) {
        List<MatOfPoint> markers = findMarkers(image, minArea, maxArea, 0.65, 0.9);
        Log.d(TAG, "debugLargeMarkers: Found " + markers.size());
        Mat debug = image.clone();
        for (int i = 0; i < markers.size(); i++) {
            MatOfPoint m = markers.get(i);
            Imgproc.drawContours(debug, Collections.singletonList(m), -1, new Scalar(0,255,0), 2);
            Point c = centerOf(m);
            Imgproc.circle(debug, c, 8, new Scalar(0,255,0), 2);
            Imgproc.putText(debug, "M"+i, new Point(c.x+5, c.y),
                    Imgproc.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(0,255,0), 2);
        }
        ImageDebugUtils.saveDebugImage(debug, "debug_large_markers.jpg", ctx);
    }

    // ---------------------- Align Using 4 Markers ----------------------
    public static Mat alignImageUsingMarkers(Mat image, double minArea, double maxArea, Context ctx) throws Exception {
        List<MatOfPoint> markers = findMarkers(image, minArea, maxArea, 0.6, 0.99);
        if (markers.size() < 4)
            throw new Exception("Không đủ 4 marker lớn để căn chỉnh");
        Point[] corners = getCornerMarkers(markers);

        // tính kích thước mới
        double w1 = distance(corners[0], corners[1]);
        double w2 = distance(corners[2], corners[3]);
        double h1 = distance(corners[1], corners[2]);
        double h2 = distance(corners[3], corners[0]);
        int maxW = (int)Math.max(w1, w2);
        int maxH = (int)Math.max(h1, h2);

        MatOfPoint2f src = new MatOfPoint2f(corners);
        MatOfPoint2f dst = new MatOfPoint2f(
                new Point(0,0),
                new Point(maxW-1,0),
                new Point(maxW-1,maxH-1),
                new Point(0,maxH-1)
        );
        Mat M = Imgproc.getPerspectiveTransform(src, dst);
        Mat aligned = new Mat();
        Imgproc.warpPerspective(image, aligned, M, new Size(maxW, maxH));
        Log.d(TAG, "Aligned saved");
        return aligned;
    }

    private static double distance(Point a, Point b) {
        return Math.hypot(a.x - b.x, a.y - b.y);
    }

    public static Point[] getCornerMarkers(List<MatOfPoint> markers) {
        List<Point> centers = new ArrayList<>();
        for (MatOfPoint m : markers) centers.add(centerOf(m));
        int n = centers.size();
        double[] sums = new double[n], diffs = new double[n];
        for (int i = 0; i < n; i++) {
            Point p = centers.get(i);
            sums[i] = p.x + p.y;
            diffs[i] = p.y - p.x;
        }
        int idxTL=0, idxBR=0, idxTR=0, idxBL=0;
        for (int i = 1; i < n; i++) {
            if (sums[i] < sums[idxTL]) idxTL = i;
            if (sums[i] > sums[idxBR]) idxBR = i;
            if (diffs[i] < diffs[idxTR]) idxTR = i;
            if (diffs[i] > diffs[idxBL]) idxBL = i;
        }
        return new Point[]{
                centers.get(idxTL),
                centers.get(idxTR),
                centers.get(idxBR),
                centers.get(idxBL)
        };
    }
    // ---------------------- Small Marker Detection ----------------------
    public static List<MatOfPoint> findSmallMarkersOnBChannel(Mat image, double minArea, double maxArea) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        List<MatOfPoint> markers = new ArrayList<>();
        double imageCenterX = image.cols() / 2.0;
        double halfRange = image.cols() * 0.3;
        int contourIndex = 0;
        for (MatOfPoint cnt : contours) {
            double area = Imgproc.contourArea(cnt);
            Rect rect = Imgproc.boundingRect(cnt);
            double aspectRatio = (double) rect.width / rect.height;
            double perimeter = Imgproc.arcLength(new MatOfPoint2f(cnt.toArray()), true);
            double circularity = 4 * Math.PI * area / (perimeter * perimeter);
            Point center = centerOf(cnt);
            Log.d(TAG, "findSmallMarkers: Contour " + contourIndex + ": center.x = " + center.x +
                    " (Image center.x = " + imageCenterX + ")");
            if (Math.abs(center.x - imageCenterX) > halfRange) {
                Log.d(TAG, "findSmallMarkers: Contour " + contourIndex + " bị loại (ngoài vùng trung tâm)");
                contourIndex++;
                continue;
            }
            Log.d(TAG, "findSmallMarkers: Contour " + contourIndex + " => area=" + area +
                    ", aspectRatio=" + aspectRatio +
                    ", circularity=" + circularity);
            if (area >= minArea && area <= maxArea &&
                    aspectRatio >= 0.1 && aspectRatio <= 6 &&
                    circularity >= 0.65 && circularity <= 0.82) {
                markers.add(cnt);
                Log.d(TAG, "findSmallMarkers: Contour " + contourIndex + " được chọn");
            } else {
                Log.d(TAG, "findSmallMarkers: Contour " + contourIndex + " bị loại");
            }
            contourIndex++;
        }
        Log.d(TAG, "findSmallMarkers: Tìm được " + markers.size() + " small markers trong vùng trung tâm.");
        return markers;
    }

    // Debug small markers: Vẽ các small marker lên debug image và lưu ra file "smallMarkers_debug.jpg"
    public static void debugSmallMarkers(Mat image, double minArea, double maxArea, Context context) {
        Log.d(TAG, "Bắt đầu debugSmallMarkers...");
        List<MatOfPoint> smallMarkers = findSmallMarkersOnBChannel(image, minArea, maxArea);
        Log.d(TAG, "debugSmallMarkers: Số small marker tìm được: " + smallMarkers.size());
        Mat debugImg = new Mat();
        if (image.channels() == 1) {
            Imgproc.cvtColor(image, debugImg, Imgproc.COLOR_GRAY2BGR);
        } else {
            debugImg = image.clone();
        }
        for (int i = 0; i < smallMarkers.size(); i++) {
            Imgproc.drawContours(debugImg, Collections.singletonList(smallMarkers.get(i)), -1, new Scalar(0, 255, 0), 3);
            Point center = centerOf(smallMarkers.get(i));
            Imgproc.circle(debugImg, center, 8, new Scalar(0, 255, 0), 3);
            Imgproc.putText(debugImg, "SM" + i, new Point(center.x + 10, center.y),
                    Imgproc.FONT_HERSHEY_SIMPLEX, 1.0, new Scalar(0, 255, 0), 3);
        }
        ImageDebugUtils.saveDebugImage(debugImg, "smallMarkers_debug.jpg", context);
    }

    // ---------------------- Order Small Markers ----------------------
    /**
     * Sắp xếp danh sách 5 điểm marker theo thứ tự mong muốn:
     * marker0: marker có y cao nhất (bottom)
     * marker4: marker có y thấp nhất (top)
     * Trong số các điểm còn lại, sắp xếp theo x tăng dần:
     * marker2: marker có x nhỏ nhất (left)
     * marker3: marker có x lớn nhất (right)
     * marker1: marker còn lại (middle)
     * Nếu số marker ít hơn 5, trả về danh sách ban đầu.
     */
    public static List<Point> orderMarkersCustom(List<Point> markers) {
        if (markers.size() < 5) {
            Log.e(TAG, "orderMarkersCustom: Không đủ marker để sắp xếp (yêu cầu >=5)");
            return markers;
        }
        List<Point> copy = new ArrayList<>(markers);
        Collections.sort(copy, new Comparator<Point>() {
            @Override
            public int compare(Point p1, Point p2) {
                return Double.compare(p2.y, p1.y); // sắp xếp giảm dần theo y
            }
        });
        Point markerBottom = copy.get(0);
        Point markerTop = copy.get(copy.size() - 1);
        List<Point> middleMarkers = new ArrayList<>(copy.subList(1, copy.size() - 1));
        Collections.sort(middleMarkers, new Comparator<Point>() {
            @Override
            public int compare(Point p1, Point p2) {
                return Double.compare(p1.x, p2.x); // sắp xếp tăng dần theo x
            }
        });
        Point markerLeft = middleMarkers.get(0);
        Point markerRight = middleMarkers.get(middleMarkers.size() - 1);
        Point markerMiddle = middleMarkers.get(middleMarkers.size() / 2);
        List<Point> ordered = new ArrayList<>();
        ordered.add(markerBottom);  // marker0: bottom
        ordered.add(markerMiddle);  // marker1: middle
        ordered.add(markerLeft);    // marker2: left
        ordered.add(markerRight);   // marker3: right
        ordered.add(markerTop);     // marker4: top
        Log.d(TAG, "orderMarkersCustom: Kết quả sắp xếp:");
        for (int i = 0; i < ordered.size(); i++) {
            Log.d(TAG, "Marker " + i + ": (" + ordered.get(i).x + ", " + ordered.get(i).y + ")");
        }
        return ordered;
    }

    // ---------------------- ROI Extraction ----------------------
    public static class RegionResult {
        public Mat sbdRoi;
        public Mat maDeRoi;
        public Mat examLeftRoi;
        public Mat examRightRoi;
        // Thêm các offset của ROI trên ảnh processed (đã căn chỉnh)
        public double sbdOffsetX;
        public double sbdOffsetY;
        public double maDeOffsetX;
        public double maDeOffsetY;
        public double examLeftOffsetX;
        public double examLeftOffsetY;
        public double examRightOffsetX;
        public double examRightOffsetY;
    }


    /**
     * Trích xuất ROI từ ảnh dựa trên 5 marker đã sắp xếp theo thứ tự:
     * marker0: bottom, marker1: middle, marker2: left, marker3: right, marker4: top.
     * Vẽ overlay các ROI lên debugImg và lưu debug ảnh.
     *
     * @param src      Ảnh gốc đã xử lý.
     * @param marker0  Marker 0 (bottom).
     * @param marker1  Marker 1 (middle).
     * @param marker2  Marker 2 (left).
     * @param marker3  Marker 3 (right).
     * @param marker4  Marker 4 (top).
     * @param context  Context để lưu debug ảnh.
     * @param debugImg Ảnh clone của src dùng để vẽ overlay ROI.
     * @return RegionResult chứa các ROI.
     */
    public static RegionResult extractROI(Mat src, Point marker0, Point marker1, Point marker2, Point marker3, Point marker4, Context context, Mat debugImg) {
        // ROI SBD (Số báo danh)
        double offset_sbd_left = -9;
        double offset_sbd_right = 8;
        double offset_sbd_top = 10;
        double offset_sbd_bottom = 10;
        double sbd_x = marker2.x + offset_sbd_left;
        double sbd_y = marker4.y + offset_sbd_top;
        double sbd_width = (marker1.x - marker2.x) - (offset_sbd_left + offset_sbd_right);
        double sbd_height = (marker2.y - marker4.y) - (offset_sbd_top + offset_sbd_bottom);

        // ROI Mã đề
        double offset_ma_de_left = 10;
        double offset_ma_de_right = 54;
        double offset_ma_de_top = 12;
        double offset_ma_de_bottom = 10;
        double ma_de_x = marker1.x + offset_ma_de_left;
        double ma_de_y = marker4.y + offset_ma_de_top;
        double ma_de_width = (marker3.x - marker1.x) - (offset_ma_de_left + offset_ma_de_right);
        double ma_de_height = (marker3.y - marker4.y) - (offset_ma_de_top + offset_ma_de_bottom);

        // ROI Exam:
        double total_exam_width = marker3.x - marker2.x;
        double offset_exam_left_left = 15;
        double offset_exam_left_right = 29;
        double offset_exam_left_top = 12;
        double offset_exam_left_bottom = 8;
        double roi_exam_left_x = marker2.x + offset_exam_left_left;
        double roi_exam_left_y = marker2.y + offset_exam_left_top;
        double roi_exam_left_width = (total_exam_width / 2) - (offset_exam_left_left + offset_exam_left_right);
        double roi_exam_left_height = (marker0.y - marker2.y) - (offset_exam_left_top + offset_exam_left_bottom);

        double offset_exam_right_left = 15;
        double offset_exam_right_right = 29;
        double offset_exam_right_top = 12;
        double offset_exam_right_bottom = 8;
        double roi_exam_right_x = marker2.x + (total_exam_width / 2) + offset_exam_right_left;
        double roi_exam_right_y = marker2.y + offset_exam_right_top;
        double roi_exam_right_width = (total_exam_width - (total_exam_width / 2)) - (offset_exam_right_left + offset_exam_right_right);
        double roi_exam_right_height = (marker0.y - marker2.y) - (offset_exam_right_top + offset_exam_right_bottom);

        Log.d(TAG, "ROI SBD: sbd_x = " + sbd_x + ", sbd_y = " + sbd_y +
                ", sbd_width = " + sbd_width + ", sbd_height = " + sbd_height);
        Log.d(TAG, "ROI Mã đề: ma_de_x = " + ma_de_x + ", ma_de_y = " + ma_de_y +
                ", ma_de_width = " + ma_de_width + ", ma_de_height = " + ma_de_height);
        Log.d(TAG, "ROI Exam Left: x = " + roi_exam_left_x + ", y = " + roi_exam_left_y +
                ", width = " + roi_exam_left_width + ", height = " + roi_exam_left_height);
        Log.d(TAG, "ROI Exam Right: x = " + roi_exam_right_x + ", y = " + roi_exam_right_y +
                ", width = " + roi_exam_right_width + ", height = " + roi_exam_right_height);

        // Nếu debugImg là ảnh grayscale, chuyển sang BGR để vẽ overlay màu
        if (debugImg.channels() == 1) {
            Imgproc.cvtColor(debugImg, debugImg, Imgproc.COLOR_GRAY2BGR);
        }
        // Vẽ overlay các ROI lên debugImg
        Imgproc.rectangle(debugImg, new Point(sbd_x, sbd_y),
                new Point(sbd_x + sbd_width, sbd_y + sbd_height), new Scalar(255, 0, 0), 2);
        Imgproc.rectangle(debugImg, new Point(ma_de_x, ma_de_y),
                new Point(ma_de_x + ma_de_width, ma_de_y + ma_de_height), new Scalar(0, 255, 0), 2);
        Imgproc.rectangle(debugImg, new Point(roi_exam_left_x, roi_exam_left_y),
                new Point(roi_exam_left_x + roi_exam_left_width, roi_exam_left_y + roi_exam_left_height), new Scalar(0, 0, 255), 2);
        Imgproc.rectangle(debugImg, new Point(roi_exam_right_x, roi_exam_right_y),
                new Point(roi_exam_right_x + roi_exam_right_width, roi_exam_right_y + roi_exam_right_height), new Scalar(0, 0, 255), 2);
        ImageDebugUtils.saveDebugImage(debugImg, "debug_regions_custom.jpg", context);

        // Cắt ROI từ ảnh gốc src
        Rect sbdRect = new Rect((int) sbd_x, (int) sbd_y, (int) sbd_width, (int) sbd_height);
        Rect maDeRect = new Rect((int) ma_de_x, (int) ma_de_y, (int) ma_de_width, (int) ma_de_height);
        Rect examLeftRect = new Rect((int) roi_exam_left_x, (int) roi_exam_left_y, (int) roi_exam_left_width, (int) roi_exam_left_height);
        Rect examRightRect = new Rect((int) roi_exam_right_x, (int) roi_exam_right_y, (int) roi_exam_right_width, (int) roi_exam_right_height);

        RegionResult roiResult = new RegionResult();
        roiResult.sbdRoi = src.submat(sbdRect);
        roiResult.maDeRoi = src.submat(maDeRect);
        roiResult.examLeftRoi = src.submat(examLeftRect);
        roiResult.examRightRoi = src.submat(examRightRect);

        // --- Gán giá trị offset đã tính được vào RegionResult ---
        roiResult.sbdOffsetX = sbd_x;
        roiResult.sbdOffsetY = sbd_y;
        roiResult.maDeOffsetX = ma_de_x;
        roiResult.maDeOffsetY = ma_de_y;
        roiResult.examLeftOffsetX = roi_exam_left_x;
        roiResult.examLeftOffsetY = roi_exam_left_y;
        roiResult.examRightOffsetX = roi_exam_right_x;
        roiResult.examRightOffsetY = roi_exam_right_y;

        return roiResult;
    }

}
