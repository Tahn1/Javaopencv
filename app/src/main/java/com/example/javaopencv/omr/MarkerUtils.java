package com.example.javaopencv.omr;

import android.content.Context;
import android.util.Log;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MarkerUtils {
    private static final String TAG = "MarkerUtils";

    // ---------------------- Marker Large Detection ----------------------
    public static List<MatOfPoint> findMarkers(Mat image, double minArea, double maxArea, double circularityLow, double circularityHigh) {
        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

        Mat thresh = new Mat();
        // Sử dụng Otsu để tự động xác định ngưỡng, tạo ảnh nhị phân đảo
        Imgproc.threshold(gray, thresh, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
        // Lưu debug ảnh threshold vào file
        Imgcodecs.imwrite("debug_threshold_large.jpg", thresh);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(thresh, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Log.d(TAG, "findMarkers: Contours found: " + contours.size());

        // Lưu debug ảnh với các contour (vẽ bằng màu đỏ)
        Mat debugContoursImg = new Mat();
        Imgproc.cvtColor(gray, debugContoursImg, Imgproc.COLOR_GRAY2BGR);
        Imgproc.drawContours(debugContoursImg, contours, -1, new Scalar(0, 0, 255), 2);
        Imgcodecs.imwrite("debug_initial_contours_large.jpg", debugContoursImg);

        List<MatOfPoint> markers = new ArrayList<>();
        for (MatOfPoint cnt : contours) {
            double area = Imgproc.contourArea(cnt);
            Log.d(TAG, "Processing contour, area = " + area);
            if (area < minArea || area > maxArea) {
                Log.d(TAG, "Contour bị loại do area không thuộc khoảng [" + minArea + ", " + maxArea + "]");
                continue;
            }
            double perimeter = Imgproc.arcLength(new MatOfPoint2f(cnt.toArray()), true);
            if (perimeter == 0)
                continue;
            double circularity = 4 * Math.PI * area / (perimeter * perimeter);
            Log.d(TAG, "Contour: area = " + area + ", perimeter = " + perimeter + ", circularity = " + circularity);
            if (circularity < circularityLow || circularity > circularityHigh) {
                Log.d(TAG, "Contour bị loại do circularity không thuộc khoảng [" + circularityLow + ", " + circularityHigh + "]");
                continue;
            }
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(new MatOfPoint2f(cnt.toArray()), approx, 0.02 * perimeter, true);
            if (approx.total() == 4) {
                // Dùng minAreaRect để lấy hộp chứa contour với 4 điểm chính xác
                RotatedRect rect = Imgproc.minAreaRect(new MatOfPoint2f(cnt.toArray()));
                Point[] box = new Point[4];
                rect.points(box);
                for (int i = 0; i < box.length; i++) {
                    box[i].x = Math.round(box[i].x);
                    box[i].y = Math.round(box[i].y);
                }
                MatOfPoint marker = new MatOfPoint(box);
                markers.add(marker);
                Log.d(TAG, "findMarkers: Found marker: " + Arrays.toString(box) +
                        ", circularity = " + String.format("%.2f", circularity));
            }
        }
        Log.d(TAG, "findMarkers: Total markers: " + markers.size());
        return markers;
    }

    // Tính trung tâm của marker bằng cách lấy trung bình các tọa độ của các đỉnh
    public static Point centerOf(MatOfPoint marker) {
        Point[] pts = marker.toArray();
        double sumX = 0, sumY = 0;
        for (Point p : pts) {
            sumX += p.x;
            sumY += p.y;
        }
        return new Point(sumX / pts.length, sumY / pts.length);
    }

    // Debug marker lớn: Vẽ các marker lên ảnh debug và lưu debug ảnh
    public static void debugLargeMarkers(Mat image, double minArea, double maxArea, Context context) {
        List<MatOfPoint> markers = findMarkers(image, minArea, maxArea, 0.65, 0.9);
        Log.d(TAG, "debugLargeMarkers: Found " + markers.size() + " large markers.");
        for (int i = 0; i < markers.size(); i++) {
            Point center = centerOf(markers.get(i));
            Log.d(TAG, "debugLargeMarkers: Marker " + i + " center: (" + center.x + ", " + center.y + ")");
        }
        Mat debugImg = image.clone();
        for (int i = 0; i < markers.size(); i++) {
            Imgproc.drawContours(debugImg, Collections.singletonList(markers.get(i)), -1, new Scalar(0, 255, 0), 2);
            Point center = centerOf(markers.get(i));
            Imgproc.circle(debugImg, center, 8, new Scalar(0, 255, 0), 2);
            Imgproc.putText(debugImg, "M" + i, new Point(center.x + 5, center.y),
                    Imgproc.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(0, 255, 0), 2);
        }
        ImageDebugUtils.saveDebugImage(debugImg, "debug_large_markers.jpg", context);
    }

    /**
     * Hàm căn chỉnh ảnh sử dụng 4 marker lớn.
     * Tìm 4 marker lớn, xác định các góc theo trung tâm của chúng dựa trên Moments,
     * sau đó tính toán ma trận biến đổi phối cảnh và warp ảnh.
     * Sẽ in log các giá trị góc và lưu debug ảnh căn chỉnh với tên "aligned_debug.jpg".
     *
     * @param image    Ảnh đầu vào.
     * @param minArea  Diện tích tối thiểu.
     * @param maxArea  Diện tích tối đa.
     * @param context  Context để lưu debug ảnh.
     * @return Ảnh đã căn chỉnh.
     * @throws Exception Nếu không đủ marker.
     */
    public static Mat alignImageUsingMarkers(Mat image, double minArea, double maxArea, Context context) throws Exception {
        List<MatOfPoint> markers = findMarkers(image, minArea, maxArea, 0.6, 0.99);
        Log.d(TAG, "alignImageUsingMarkers: Found " + markers.size() + " markers.");
        if (markers.size() < 4) {
            throw new Exception("Không đủ marker lớn để căn chỉnh ảnh");
        }
        // Lấy 4 góc marker từ danh sách các marker lớn
        Point[] corners = getCornerMarkers(markers);
        for (int i = 0; i < corners.length; i++) {
            Log.d(TAG, String.format("alignImageUsingMarkers: Corner %d: (%.1f, %.1f)", i, corners[i].x, corners[i].y));
        }
        int maxWidth = (int) Math.max(distance(corners[0], corners[1]), distance(corners[2], corners[3]));
        int maxHeight = (int) Math.max(distance(corners[1], corners[2]), distance(corners[3], corners[0]));
        Log.d(TAG, "alignImageUsingMarkers: Calculated width = " + maxWidth + ", height = " + maxHeight);

        MatOfPoint2f srcPts = new MatOfPoint2f(corners);
        MatOfPoint2f dstPts = new MatOfPoint2f(
                new Point(0, 0),
                new Point(maxWidth - 1, 0),
                new Point(maxWidth - 1, maxHeight - 1),
                new Point(0, maxHeight - 1)
        );
        Mat transform = Imgproc.getPerspectiveTransform(srcPts, dstPts);
        Mat aligned = new Mat();
        Imgproc.warpPerspective(image, aligned, transform, new Size(maxWidth, maxHeight));

        // Lưu debug ảnh đã căn chỉnh
        ImageDebugUtils.saveDebugImage(aligned, "aligned_debug.jpg", context);
        Log.d(TAG, "alignImageUsingMarkers: Aligned debug image saved as 'aligned_debug.jpg'");
        return aligned;
    }

    /**
     * Từ danh sách marker lớn, tính trung tâm của mỗi marker dựa trên Moments và xác định 4 góc theo:
     * - Top-left: có tổng (x+y) nhỏ nhất.
     * - Top-right: có hiệu (y-x) nhỏ nhất.
     * - Bottom-right: có tổng (x+y) lớn nhất.
     * - Bottom-left: có hiệu (y-x) lớn nhất.
     *
     * @param markers Danh sách marker lớn.
     * @return Mảng 4 điểm theo thứ tự: [top-left, top-right, bottom-right, bottom-left].
     */
    public static Point[] getCornerMarkers(List<MatOfPoint> markers) {
        List<Point> centers = new ArrayList<>();
        for (MatOfPoint marker : markers) {
            centers.add(centerOf(marker));
        }
        double[] sums = new double[centers.size()];
        double[] diffs = new double[centers.size()];
        for (int i = 0; i < centers.size(); i++) {
            Point p = centers.get(i);
            sums[i] = p.x + p.y;
            diffs[i] = p.y - p.x;
        }
        int idxTL = 0, idxBR = 0, idxTR = 0, idxBL = 0;
        double minSum = sums[0], maxSum = sums[0];
        double minDiff = diffs[0], maxDiff = diffs[0];
        for (int i = 1; i < centers.size(); i++) {
            if (sums[i] < minSum) { minSum = sums[i]; idxTL = i; }
            if (sums[i] > maxSum) { maxSum = sums[i]; idxBR = i; }
            if (diffs[i] < minDiff) { minDiff = diffs[i]; idxTR = i; }
            if (diffs[i] > maxDiff) { maxDiff = diffs[i]; idxBL = i; }
        }
        Point topLeft = centers.get(idxTL);
        Point topRight = centers.get(idxTR);
        Point bottomRight = centers.get(idxBR);
        Point bottomLeft = centers.get(idxBL);
        return new Point[]{ topLeft, topRight, bottomRight, bottomLeft };
    }

    private static double distance(Point p1, Point p2) {
        return Math.hypot(p1.x - p2.x, p1.y - p2.y);
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
        double offset_exam_left_right = 33;
        double offset_exam_left_top = 12;
        double offset_exam_left_bottom = 10;
        double roi_exam_left_x = marker2.x + offset_exam_left_left;
        double roi_exam_left_y = marker2.y + offset_exam_left_top;
        double roi_exam_left_width = (total_exam_width / 2) - (offset_exam_left_left + offset_exam_left_right);
        double roi_exam_left_height = (marker0.y - marker2.y) - (offset_exam_left_top + offset_exam_left_bottom);

        double offset_exam_right_left = 15;
        double offset_exam_right_right = 33;
        double offset_exam_right_top = 12;
        double offset_exam_right_bottom = 10;
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
        ImageDebugUtils.saveDebugImage(debugImg, "debug_roi_overlay.jpg", context);

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
