package com.example.javaopencv.omr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.javaopencv.R;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestOmrActivity extends AppCompatActivity {

    private static final String TAG = "TestOmrActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Layout được thiết lập sao cho phần header (TextView: tvHeader) ở trên và ImageView (imageResult) chiếm phần còn lại
        setContentView(R.layout.activity_test_omr);

        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "OpenCV failed to load!");
            return;
        }
        Log.d(TAG, "OpenCV loaded successfully");

        // Giả sử layout activity_test_omr.xml có TextView với id "tvHeader" và ImageView với id "imageResult"
        TextView tvHeader = findViewById(R.id.tvHeader);
        ImageView imageResult = findViewById(R.id.imageResult);

        try {
            // 1. Load ảnh test từ assets.
            InputStream is = getAssets().open("phieu20cau3.jpg");
            Bitmap testBitmap = BitmapFactory.decodeStream(is);
            is.close();

            // 2. Xử lý OMR: Lấy ảnh căn chỉnh màu (alignedMat), số báo danh (sbd), mã đề (maDe), danh sách đáp án.
            OMRProcessor.OMRResult omrResult = OMRProcessor.process(testBitmap, getApplicationContext());
            if (omrResult == null || omrResult.sbd == null || omrResult.alignedMat == null) {
                tvHeader.setText("Xử lý OMR thất bại!");
                return;
            }
            Log.d(TAG, "Aligned image (color) size: "
                    + omrResult.alignedMat.cols() + " x " + omrResult.alignedMat.rows());
            Log.d(TAG, "alignedMat channels = " + omrResult.alignedMat.channels());

            // 3. Giả lập database đáp án.
            Map<String, List<String>> answerKeyMap = new HashMap<>();
            String[] key1Answers = {"A","D","B","C","D","B","A","B","C","D",
                    "A","D","D","A","B","C","A","C","A","D"};
            String[] key2Answers = {"D","C","B","A","D","C","B","A","D","C",
                    "B","A","D","C","B","A","D","C","B","A"};
            List<String> correctAnswersKey1 = new ArrayList<>(Arrays.asList(key1Answers));
            List<String> correctAnswersKey2 = new ArrayList<>(Arrays.asList(key2Answers));
            answerKeyMap.put("189", correctAnswersKey1);
            answerKeyMap.put("123", correctAnswersKey2);

            // 4. Lấy kết quả nhận dạng.
            String recognizedSBD = omrResult.sbd;
            String recognizedMaDe = omrResult.maDe;
            List<String> recognizedAnswers = omrResult.answers;
            if (recognizedAnswers == null || recognizedAnswers.size() != 20) {
                recognizedAnswers = new ArrayList<>();
                for (int i = 0; i < 20; i++) {
                    recognizedAnswers.add("A");
                }
            }
            List<String> correctAnswers = answerKeyMap.get(recognizedMaDe);
            if (correctAnswers == null || correctAnswers.size() != recognizedAnswers.size()) {
                correctAnswers = new ArrayList<>();
                for (int i = 0; i < 20; i++) {
                    correctAnswers.add("X");
                }
            }

            // Tính điểm.
            int correctCount = 0;
            for (int i = 0; i < recognizedAnswers.size(); i++) {
                if (recognizedAnswers.get(i).equals(correctAnswers.get(i))) {
                    correctCount++;
                }
            }
            double score = ((double) correctCount / 20.0) * 10.0;

            // 5. Tạo processedMat từ alignedMat (ảnh nhị phân dùng cho marker detection).
            Mat processedMat = OMRProcessor.postprocessAlignedImage(omrResult.alignedMat, getApplicationContext());
            Log.d(TAG, "ProcessedMat size: "
                    + processedMat.cols() + " x " + processedMat.rows());

            // 6. Tìm các marker nhỏ từ processedMat.
            List<MatOfPoint> smallMarkers = MarkerUtils.findSmallMarkersOnBChannel(processedMat, 165.0, 225.0);
            if (smallMarkers.size() < 5) {
                Log.e(TAG, "Không đủ marker nhỏ.");
                tvHeader.setText("Không đủ marker nhỏ.");
                return;
            }
            List<Point> centers = new ArrayList<>();
            for (MatOfPoint cnt : smallMarkers) {
                centers.add(MarkerUtils.centerOf(cnt));
            }
            List<Point> orderedSmallMarkers = MarkerUtils.orderMarkersCustom(centers);
            if (orderedSmallMarkers.size() < 5) {
                Log.e(TAG, "Không đủ marker sau khi sắp xếp.");
                tvHeader.setText("Không đủ marker sau khi sắp xếp.");
                return;
            }
            for (int i = 0; i < orderedSmallMarkers.size(); i++) {
                Log.d(TAG, "Marker " + i + ": " + orderedSmallMarkers.get(i).toString());
            }
            Point marker0 = orderedSmallMarkers.get(0); // bottom
            Point marker1 = orderedSmallMarkers.get(1); // middle
            Point marker2 = orderedSmallMarkers.get(2); // left
            Point marker3 = orderedSmallMarkers.get(3); // right
            Point marker4 = orderedSmallMarkers.get(4); // top

            // 7. Tính toán ROI offset từ processedMat bằng MarkerUtils.extractROI.
            MarkerUtils.RegionResult regions = MarkerUtils.extractROI(
                    processedMat, marker0, marker1, marker2, marker3, marker4,
                    getApplicationContext(), processedMat.clone()
            );
            if (regions == null) {
                Log.e(TAG, "Không xác định được ROI.");
                tvHeader.setText("Không xác định được ROI.");
                return;
            }
            Log.d(TAG, "ROI SBD offset: (" + regions.sbdOffsetX + ", " + regions.sbdOffsetY + "), size: "
                    + regions.sbdRoi.cols() + " x " + regions.sbdRoi.rows());
            Log.d(TAG, "ROI MaDe offset: (" + regions.maDeOffsetX + ", " + regions.maDeOffsetY + ")");
            Log.d(TAG, "ROI ExamLeft offset: (" + regions.examLeftOffsetX + ", " + regions.examLeftOffsetY + ")");
            Log.d(TAG, "ROI ExamRight offset: (" + regions.examRightOffsetX + ", " + regions.examRightOffsetY + ")");

            // 8. Cắt ROI từ ảnh màu căn chỉnh (alignedMat).
            Rect sbdRect = new Rect((int) regions.sbdOffsetX, (int) regions.sbdOffsetY,
                    regions.sbdRoi.cols(), regions.sbdRoi.rows());
            Mat sbdColor = new Mat(omrResult.alignedMat, sbdRect);
            Rect maDeRect = new Rect((int) regions.maDeOffsetX, (int) regions.maDeOffsetY,
                    regions.maDeRoi.cols(), regions.maDeRoi.rows());
            Mat maDeColor = new Mat(omrResult.alignedMat, maDeRect);
            Rect examLeftRect = new Rect((int) regions.examLeftOffsetX, (int) regions.examLeftOffsetY,
                    regions.examLeftRoi.cols(), regions.examLeftRoi.rows());
            Mat examLeftColor = new Mat(omrResult.alignedMat, examLeftRect);
            Rect examRightRect = new Rect((int) regions.examRightOffsetX, (int) regions.examRightOffsetY,
                    regions.examRightRoi.cols(), regions.examRightRoi.rows());
            Mat examRightColor = new Mat(omrResult.alignedMat, examRightRect);

            // 9. Vẽ grid trắng mảnh (thickness = 1) trên từng ROI Exam.
            Mat examLeftWithGrid = GridUtils.drawGridOnImage(examLeftColor, 4, 10, 0, new Scalar(255, 255, 255), 1);
            Mat examRightWithGrid = GridUtils.drawGridOnImage(examRightColor, 4, 10, 0, new Scalar(255, 255, 255), 1);
            ImageDebugUtils.saveDebugImage(examLeftWithGrid, "debug_examLeft_grid.jpg", getApplicationContext());
            ImageDebugUtils.saveDebugImage(examRightWithGrid, "debug_examRight_grid.jpg", getApplicationContext());
            Log.d(TAG, "Exam grid debug images saved.");

            // 10. Tách danh sách đáp án cho vùng Exam:
            // Exam Top (câu 1–10) và Exam Bottom (câu 11–20).
            List<String> examLeftAnswers = recognizedAnswers.subList(0, 10);
            List<String> examRightAnswers = recognizedAnswers.subList(10, 20);
            List<String> correctAnswersLeft = (correctAnswers != null) ? correctAnswers.subList(0, 10) : new ArrayList<>();
            List<String> correctAnswersRight = (correctAnswers != null) ? correctAnswers.subList(10, 20) : new ArrayList<>();

            // 11. Vẽ highlight cho vùng Exam Top (các vòng tròn highlight trên grid)
            // Lưu ý: hệ tọa độ của examLeftColor bắt đầu từ (0,0)
            Mat examLeftAnnotated = OMRVisualizer.drawExamResult(
                    examLeftWithGrid,
                    new OMRVisualizer.RegionCellInfo(0, 0, examLeftColor.cols(), examLeftColor.rows(), 10, 4),
                    examLeftAnswers, correctAnswersLeft);
            // 12. Vẽ highlight cho vùng Exam Bottom.
            Mat examRightAnnotated = OMRVisualizer.drawExamResult(
                    examRightWithGrid,
                    new OMRVisualizer.RegionCellInfo(0, 0, examRightColor.cols(), examRightColor.rows(), 10, 4),
                    examRightAnswers, correctAnswersRight);
            ImageDebugUtils.saveDebugImage(examLeftAnnotated, "debug_examLeft_highlighted.jpg", getApplicationContext());
            ImageDebugUtils.saveDebugImage(examRightAnnotated, "debug_examRight_highlighted.jpg", getApplicationContext());
            Log.d(TAG, "Exam highlight debug images saved.");

            // 13. Vẽ highlight cho SBD và Mã đề bằng dot xanh (sử dụng hàm drawSbdResult và drawMaDeResult).
            Mat sbdAnnotated = OMRVisualizer.drawSbdResult(
                    sbdColor,
                    new OMRVisualizer.RegionCellInfo(0, 0, sbdColor.cols(), sbdColor.rows(), 10, 6),
                    recognizedSBD);
            Mat maDeAnnotated = OMRVisualizer.drawMaDeResult(
                    maDeColor,
                    new OMRVisualizer.RegionCellInfo(0, 0, maDeColor.cols(), maDeColor.rows(), 10, 3),
                    recognizedMaDe);
            ImageDebugUtils.saveDebugImage(sbdAnnotated, "debug_sbd_highlighted.jpg", getApplicationContext());
            ImageDebugUtils.saveDebugImage(maDeAnnotated, "debug_maDe_highlighted.jpg", getApplicationContext());
            Log.d(TAG, "SBD & MaDe highlight debug images saved.");

            // 14. Overlay các ROI highlight (Exam, SBD, MaDe) lên ảnh màu căn chỉnh alignedMat (sử dụng copyTo trực tiếp).
            // Overlay ExamLeft:
            {
                int x = (int) regions.examLeftOffsetX;
                int y = (int) regions.examLeftOffsetY;
                if (x + examLeftAnnotated.cols() <= omrResult.alignedMat.cols() &&
                        y + examLeftAnnotated.rows() <= omrResult.alignedMat.rows()) {
                    examLeftAnnotated.copyTo(omrResult.alignedMat.submat(y, y + examLeftAnnotated.rows(), x, x + examLeftAnnotated.cols()));
                    Log.d(TAG, "Overlay ExamLeft thành công.");
                } else {
                    Log.e(TAG, "ExamLeft ROI vượt quá kích thước ảnh căn chỉnh.");
                }
            }
            // Overlay ExamRight:
            {
                int x = (int) regions.examRightOffsetX;
                int y = (int) regions.examRightOffsetY;
                if (x + examRightAnnotated.cols() <= omrResult.alignedMat.cols() &&
                        y + examRightAnnotated.rows() <= omrResult.alignedMat.rows()) {
                    examRightAnnotated.copyTo(omrResult.alignedMat.submat(y, y + examRightAnnotated.rows(), x, x + examRightAnnotated.cols()));
                    Log.d(TAG, "Overlay ExamRight thành công.");
                } else {
                    Log.e(TAG, "ExamRight ROI vượt quá kích thước ảnh căn chỉnh.");
                }
            }
            // Overlay SBD:
            {
                int x = (int) regions.sbdOffsetX;
                int y = (int) regions.sbdOffsetY;
                if (x + sbdAnnotated.cols() <= omrResult.alignedMat.cols() &&
                        y + sbdAnnotated.rows() <= omrResult.alignedMat.rows()) {
                    sbdAnnotated.copyTo(omrResult.alignedMat.submat(y, y + sbdAnnotated.rows(), x, x + sbdAnnotated.cols()));
                    Log.d(TAG, "Overlay SBD thành công.");
                } else {
                    Log.e(TAG, "SBD ROI vượt quá kích thước ảnh căn chỉnh.");
                }
            }
            // Overlay MaDe:
            {
                int x = (int) regions.maDeOffsetX;
                int y = (int) regions.maDeOffsetY;
                if (x + maDeAnnotated.cols() <= omrResult.alignedMat.cols() &&
                        y + maDeAnnotated.rows() <= omrResult.alignedMat.rows()) {
                    maDeAnnotated.copyTo(omrResult.alignedMat.submat(y, y + maDeAnnotated.rows(), x, x + maDeAnnotated.cols()));
                    Log.d(TAG, "Overlay MaDe thành công.");
                } else {
                    Log.e(TAG, "MaDe ROI vượt quá kích thước ảnh căn chỉnh.");
                }
            }

            // 15. Lưu file debug final và hiển thị ảnh final annotated lên ImageView.
            ImageDebugUtils.saveDebugImage(omrResult.alignedMat, "final_annotated_debug.jpg", getApplicationContext());
            Log.d(TAG, "Final annotated image saved: " + omrResult.alignedMat.cols() + " x " + omrResult.alignedMat.rows());
            Bitmap annotatedBmp = Bitmap.createBitmap(omrResult.alignedMat.cols(),
                    omrResult.alignedMat.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(omrResult.alignedMat, annotatedBmp);
            imageResult.setImageBitmap(annotatedBmp);

            // 16. Hiển thị header thông tin (Mã đề, Số câu đúng, Điểm) lên TextView.
            String headerInfo = "Mã đề: " + recognizedMaDe +
                    "\nSố câu đúng: " + correctCount + " / 20" +
                    "\nĐiểm: " + String.format("%.1f", score) + " / 10.0";
            tvHeader.setText(headerInfo);

        } catch (IOException e) {
            Log.e(TAG, "Lỗi khi load ảnh test", e);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi trong quá trình xử lý OMR", e);
        }
    }
}
