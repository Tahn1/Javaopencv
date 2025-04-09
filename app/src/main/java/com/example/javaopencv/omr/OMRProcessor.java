package com.example.javaopencv.omr;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class OMRProcessor {

    private static final String TAG = "OMRProcessor";
    // Ngưỡng tô mặc định 0.14
    private static final double FILL_THRESHOLD = 0.18;

    public static class OMRResult {
        public String sbd;         // Số báo danh
        public String maDe;        // Mã đề
        public List<String> answers; // Danh sách đáp án
    }

    /**
     * Xử lý OMR từ ảnh Bitmap đầu vào, lưu debug ảnh qua mỗi bước.
     *
     * @param inputBitmap Ảnh Bitmap đầu vào.
     * @param context     Context để lưu debug ảnh.
     * @return OMRResult chứa kết quả đọc được.
     */
    public static OMRResult process(Bitmap inputBitmap, Context context) {
        Log.d(TAG, "Bắt đầu xử lý OMR");
        OMRResult result = new OMRResult();

        try {
            // 1. Chuyển Bitmap sang Mat
            Mat srcMat = new Mat();
            Utils.bitmapToMat(inputBitmap, srcMat);
            Log.d(TAG, "Chuyển Bitmap sang Mat, kích thước: " + srcMat.width() + "x" + srcMat.height());
            ImageDebugUtils.saveDebugImage(srcMat, "srcMat.jpg", context);

            // 2. Đổi màu từ BGR sang RGB nếu cần
            Imgproc.cvtColor(srcMat, srcMat, Imgproc.COLOR_BGR2RGB, 3);
            Log.d(TAG, "Đã chuyển sang RGB");
            ImageDebugUtils.saveDebugImage(srcMat, "srcMat_rgb.jpg", context);

            // 3. Resize ảnh (scaleFactor = 0.4)
            double scaleFactor = 0.4;
            Size newSize = new Size(srcMat.width() * scaleFactor, srcMat.height() * scaleFactor);
            Imgproc.resize(srcMat, srcMat, newSize);
            Log.d(TAG, "Resize ảnh, kích thước mới: " + srcMat.width() + "x" + srcMat.height());
            ImageDebugUtils.saveDebugImage(srcMat, "srcMat_resized.jpg", context);

            // 4. Chuyển sang grayscale
            Mat grayMat = new Mat();
            Imgproc.cvtColor(srcMat, grayMat, Imgproc.COLOR_BGR2GRAY, 1);
            Log.d(TAG, "Chuyển sang grayscale");
            ImageDebugUtils.saveDebugImage(grayMat, "grayMat.jpg", context);

            // 5. Áp dụng threshold với Otsu (binarize)
            Mat threshMat = new Mat();
            Imgproc.threshold(grayMat, threshMat, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
            Log.d(TAG, "Áp dụng threshold Otsu");
            ImageDebugUtils.saveDebugImage(threshMat, "threshMat.jpg", context);

            // 6. Debug marker lớn: vẽ contour marker lớn (sử dụng MarkerUtils)
            MarkerUtils.debugLargeMarkers(srcMat, 100.0, 5000.0, context);
            Log.d(TAG, "Đã debug marker lớn");

            // 7. Căn chỉnh ảnh bằng marker lớn
            Mat alignedMat;
            try {
                // Các giá trị minArea và maxArea có thể điều chỉnh để phù hợp với ảnh của bạn.
                alignedMat = MarkerUtils.alignImageUsingMarkers(srcMat, 100.0, 1500.0, context);
                Log.d(TAG, "Ảnh căn chỉnh, kích thước: " + alignedMat.width() + "x" + alignedMat.height());
                ImageDebugUtils.saveDebugImage(alignedMat, "alignedMat.jpg", context);
            } catch (Exception e) {
                Log.e(TAG, "Không căn chỉnh được ảnh bằng marker lớn", e);
                result.sbd = null;
                return result;
            }

            // 8. Postprocess ảnh căn chỉnh: chuyển sang grayscale, CLAHE, threshold lại.
            Mat processedMat = postprocessAlignedImage(alignedMat, context);
            Log.d(TAG, "Sau postprocess, ảnh xử lý: " + processedMat.width() + "x" + processedMat.height());
            ImageDebugUtils.saveDebugImage(processedMat, "processedMat.jpg", context);

            // 9. Debug small markers: Vẽ và lưu debug ảnh small markers trên processedMat
            MarkerUtils.debugSmallMarkers(processedMat, 165.0, 225.0, context);

            // 10. Tìm các small marker (contours) từ processedMat
            List<MatOfPoint> smallMarkers = MarkerUtils.findSmallMarkersOnBChannel(processedMat, 165.0, 225.0);
            Log.d(TAG, "Số marker nhỏ tìm được: " + smallMarkers.size());
            if (smallMarkers.size() < 5) {
                Log.e(TAG, "Không tìm đủ marker nhỏ.");
                result.sbd = null;
                return result;
            }

            // 11. Từ các small marker, lấy tọa độ trung tâm và sắp xếp thành 5 marker theo thứ tự (bottom, middle, left, right, top)
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

            // 12. Trích xuất ROI từ processedMat dựa trên 5 marker này
            MarkerUtils.RegionResult regions = MarkerUtils.extractROI(processedMat, marker0, marker1, marker2, marker3, marker4, context, processedMat.clone());
            if (regions == null) {
                Log.e(TAG, "Không xác định được ROI từ các marker nhỏ.");
                result.sbd = null;
                return result;
            }
            Log.d(TAG, "ROI được xác định thành công");
            ImageDebugUtils.saveDebugImage(regions.sbdRoi, "roi_sbd.jpg", context);
            ImageDebugUtils.saveDebugImage(regions.maDeRoi, "roi_maDe.jpg", context);
            ImageDebugUtils.saveDebugImage(regions.examLeftRoi, "roi_examLeft.jpg", context);
            ImageDebugUtils.saveDebugImage(regions.examRightRoi, "roi_examRight.jpg", context);

            // 13. Chia lưới các vùng ROI
            List<List<Mat>> sbdCells = GridUtils.splitRegionIntoCells(regions.sbdRoi, 6, 10);
            List<List<Mat>> maDeCells = GridUtils.splitRegionIntoCells(regions.maDeRoi, 3, 10);
            List<List<Mat>> examLeftCells = GridUtils.splitRegionIntoCells(regions.examLeftRoi, 4, 10);
            List<List<Mat>> examRightCells = GridUtils.splitRegionIntoCells(regions.examRightRoi, 4, 10);
            Log.d(TAG, "Chia lưới thành công");

            // 13a. Vẽ debug grid cho từng ROI và lưu debug ảnh GRID (màu trắng, độ dày 2)
            Mat gridSBD = GridUtils.drawGridOnImage(regions.sbdRoi, 6, 10, 0, new Scalar(255, 255, 255), 2);
            ImageDebugUtils.saveDebugImage(gridSBD, "grid_sbd.jpg", context);

            Mat gridMaDe = GridUtils.drawGridOnImage(regions.maDeRoi, 3, 10, 0, new Scalar(255, 255, 255), 2);
            ImageDebugUtils.saveDebugImage(gridMaDe, "grid_maDe.jpg", context);

            Mat gridExamLeft = GridUtils.drawGridOnImage(regions.examLeftRoi, 4, 10, 0, new Scalar(255, 255, 255), 2);
            ImageDebugUtils.saveDebugImage(gridExamLeft, "grid_examLeft.jpg", context);

            Mat gridExamRight = GridUtils.drawGridOnImage(regions.examRightRoi, 4, 10, 0, new Scalar(255, 255, 255), 2);
            ImageDebugUtils.saveDebugImage(gridExamRight, "grid_examRight.jpg", context);

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
     * Postprocess ảnh căn chỉnh:
     * - Chuyển sang grayscale.
     * - Áp dụng CLAHE (clipLimit 2.0, tileGridSize 8x8).
     * - Áp dụng threshold Otsu để chuyển ảnh thành nhị phân.
     *
     * @param alignedImg Ảnh căn chỉnh đầu vào.
     * @param context    Context để lưu debug ảnh.
     * @return Ảnh đã được xử lý (binary).
     */
    private static Mat postprocessAlignedImage(Mat alignedImg, Context context) {
        Mat gray = new Mat();
        Imgproc.cvtColor(alignedImg, gray, Imgproc.COLOR_BGR2GRAY, 1);
        Log.d(TAG, "Chuyển alignedImg sang grayscale");

        Mat claheImg = new Mat();
        CLAHE clahe = Imgproc.createCLAHE(2.0, new Size(8, 8));
        clahe.apply(gray, claheImg);
        Log.d(TAG, "CLAHE đã được áp dụng");
        ImageDebugUtils.saveDebugImage(claheImg, "claheImg.jpg", context);

        Mat binary = new Mat();
        Imgproc.threshold(claheImg, binary, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
        Log.d(TAG, "Threshold nhị phân được áp dụng");
        ImageDebugUtils.saveDebugImage(binary, "binaryImg.jpg", context);

        return binary;
    }
}
