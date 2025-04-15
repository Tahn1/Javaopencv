package com.example.javaopencv.omr;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class OMRProcessor {

    private static final String TAG = "OMRProcessor";
    private static final double FILL_THRESHOLD = 0.18;

    // Thêm trường alignedMat vào OMRResult để lưu ảnh căn chỉnh.
    public static class OMRResult {
        public String sbd;          // Số báo danh
        public String maDe;         // Mã đề
        public List<String> answers; // Danh sách đáp án
        public Mat alignedMat;      // Ảnh căn chỉnh (được lưu sau bước căn chỉnh marker lớn)
    }

    /**
     * Xử lý OMR từ ảnh Bitmap đầu vào, lưu debug ảnh qua mỗi bước.
     */
    public static OMRResult process(Bitmap inputBitmap, Context context) {
        Log.d(TAG, "Bắt đầu xử lý OMR");
        OMRResult result = new OMRResult();

        try {
            // 1. Chuyển Bitmap sang Mat
            Mat srcMat = new Mat();
            Utils.bitmapToMat(inputBitmap, srcMat);
            Log.d(TAG, "Chuyển Bitmap sang Mat, kích thước: " + srcMat.width() + "x" + srcMat.height());
            // 2. Đổi màu từ BGR sang RGB nếu cần
            Imgproc.cvtColor(srcMat, srcMat, Imgproc.COLOR_BGR2RGB, 3);
            Log.d(TAG, "Đã chuyển sang RGB");

            // 3. Resize ảnh (scaleFactor = 0.4)
            double scaleFactor = 0.4;
            Size newSize = new Size(srcMat.width() * scaleFactor, srcMat.height() * scaleFactor);
            Imgproc.resize(srcMat, srcMat, newSize);
            Log.d(TAG, "Resize ảnh, kích thước mới: " + srcMat.width() + "x" + srcMat.height());
            // 4. Chuyển sang grayscale
            Mat grayMat = new Mat();
            Imgproc.cvtColor(srcMat, grayMat, Imgproc.COLOR_BGR2GRAY, 1);
            Log.d(TAG, "Chuyển sang grayscale");

            // 5. Áp dụng threshold với Otsu (binarize)
            Mat threshMat = new Mat();
            Imgproc.threshold(grayMat, threshMat, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
            Log.d(TAG, "Áp dụng threshold Otsu");

            // 6. Debug marker lớn: vẽ contour marker lớn (sử dụng MarkerUtils)
            MarkerUtils.debugLargeMarkers(srcMat, 100.0, 5000.0, context);
            Log.d(TAG, "Đã debug marker lớn");

            // 7. Căn chỉnh ảnh bằng marker lớn
            Mat alignedMat;
            try {
                alignedMat = MarkerUtils.alignImageUsingMarkers(srcMat, 100.0, 1500.0, context);
                Log.d(TAG, "Ảnh căn chỉnh, kích thước: " + alignedMat.width() + "x" + alignedMat.height());
                // Lưu ảnh căn chỉnh vào kết quả
                result.alignedMat = alignedMat;
            } catch (Exception e) {
                Log.e(TAG, "Không căn chỉnh được ảnh bằng marker lớn", e);
                result.sbd = null;
                return result;
            }

            // 8. Postprocess ảnh căn chỉnh: chuyển sang grayscale, CLAHE, threshold lại.
            Mat processedMat = postprocessAlignedImage(alignedMat, context);
            Log.d(TAG, "Sau postprocess, ảnh xử lý: " + processedMat.width() + "x" + processedMat.height());
            ImageDebugUtils.saveDebugImage(processedMat, "processedMat.jpg", context);

            // 9. Debug small markers: vẽ và lưu debug ảnh small markers trên processedMat
            MarkerUtils.debugSmallMarkers(processedMat, 165.0, 225.0, context);

            // 10. Tìm các small marker (contours) từ processedMat
            List<MatOfPoint> smallMarkers = MarkerUtils.findSmallMarkersOnBChannel(processedMat, 165.0, 225.0);
            Log.d(TAG, "Số marker nhỏ tìm được: " + smallMarkers.size());
            if (smallMarkers.size() < 5) {
                Log.e(TAG, "Không tìm đủ marker nhỏ.");
                result.sbd = null;
                return result;
            }

            // 11. Lấy tọa độ trung tâm và sắp xếp thành 5 marker (bottom, middle, left, right, top)
            List<Point> centers = new ArrayList<>();
            for (MatOfPoint cnt : smallMarkers) {
                centers.add(MarkerUtils.centerOf(cnt));
            }
            List<Point> orderedSmallMarkers = MarkerUtils.orderMarkersCustom(centers);
            if (orderedSmallMarkers.size() < 5) {
                Log.e(TAG, "Không đủ small marker sau khi sắp xếp.");
                result.sbd = null;
                return result;
            }
            Point marker0 = orderedSmallMarkers.get(0);  // bottom
            Point marker1 = orderedSmallMarkers.get(1);  // middle
            Point marker2 = orderedSmallMarkers.get(2);  // left
            Point marker3 = orderedSmallMarkers.get(3);  // right
            Point marker4 = orderedSmallMarkers.get(4);  // top

            // 12. Trích xuất ROI từ processedMat dựa trên 5 marker
            MarkerUtils.RegionResult regions = MarkerUtils.extractROI(processedMat, marker0, marker1, marker2, marker3, marker4, context, processedMat.clone());
            if (regions == null) {
                Log.e(TAG, "Không xác định được ROI từ các marker nhỏ.");
                result.sbd = null;
                return result;
            }
            Log.d(TAG, "ROI được xác định thành công");


            // 13. Chia lưới các vùng ROI
            List<List<Mat>> sbdCells = GridUtils.splitRegionIntoCells(regions.sbdRoi, 6, 10);
            List<List<Mat>> maDeCells = GridUtils.splitRegionIntoCells(regions.maDeRoi, 3, 10);
            List<List<Mat>> examLeftCells = GridUtils.splitRegionIntoCells(regions.examLeftRoi, 4, 10);
            List<List<Mat>> examRightCells = GridUtils.splitRegionIntoCells(regions.examRightRoi, 4, 10);
            Log.d(TAG, "Chia lưới thành công");

            // 14. Đọc SBD và Mã đề
            String sbdDigits = GridUtils.extractDigits(sbdCells, FILL_THRESHOLD);
            String maDeDigits = GridUtils.extractDigits(maDeCells, FILL_THRESHOLD);
            Log.d(TAG, "Đã đọc SBD: " + sbdDigits + ", Mã đề: " + maDeDigits);

            // 15. Đọc đáp án từ grid trắc nghiệm: ghép đáp án bên trái và bên phải
            List<String> examLeftAnswers = GridUtils.extractExamAnswers(examLeftCells, FILL_THRESHOLD);
            List<String> examRightAnswers = GridUtils.extractExamAnswers(examRightCells, FILL_THRESHOLD);
            List<String> allAnswers = new ArrayList<>();
            allAnswers.addAll(examLeftAnswers);
            allAnswers.addAll(examRightAnswers);
            Log.d(TAG, "Đã đọc đáp án: " + allAnswers);

            result.sbd = sbdDigits;
            result.maDe = maDeDigits;
            result.answers = allAnswers;
            Log.d(TAG, "Xử lý OMR thành công");

            return result;

        } catch (Exception e) {
            Log.e(TAG, "Lỗi trong quá trình xử lý OMR", e);
            result.sbd = null;
            return result;
        }
    }

    /**
     * Sau khi căn chỉnh, postprocess ảnh:
     * - Chuyển sang grayscale,
     * - Áp dụng CLAHE,
     * - Áp dụng threshold Otsu.
     */
    public static Mat postprocessAlignedImage(Mat alignedImg, Context context) {
        Mat gray = new Mat();
        Imgproc.cvtColor(alignedImg, gray, Imgproc.COLOR_BGR2GRAY, 1);
        Log.d(TAG, "Chuyển alignedImg sang grayscale");

        Mat claheImg = new Mat();
        CLAHE clahe = Imgproc.createCLAHE(2.0, new Size(8, 8));
        clahe.apply(gray, claheImg);
        Log.d(TAG, "CLAHE đã được áp dụng");

        Mat binary = new Mat();
        Imgproc.threshold(claheImg, binary, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
        Log.d(TAG, "Threshold nhị phân được áp dụng");

        return binary;
    }
}
