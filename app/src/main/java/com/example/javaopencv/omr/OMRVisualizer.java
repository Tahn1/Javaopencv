package com.example.javaopencv.omr;

import android.util.Log;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import java.util.List;

/**
 * Lớp tiện ích để vẽ overlay highlight cho phiếu OMR đã căn chỉnh.
 * Phiên bản này thực hiện:
 *   - Vẽ dot xanh cho SBD và Mã đề.
 *   - Vẽ highlight cho vùng Exam.
 *     Vùng Exam được chia thành 2 ROI riêng: Exam Top (câu 1–10) và Exam Bottom (câu 11–20).
 *     Nếu người dùng chọn đáp án đúng: vẽ vòng tròn xanh lá;
 *     Nếu người dùng chọn sai: vẽ vòng tròn đỏ, và nếu không chọn thì vẽ vòng tròn màu vàng tại ô đáp án đúng.
 */
public class OMRVisualizer {

    private static final String TAG = "OMRVisualizer";

    // Vẽ dot cho SBD
    public static Mat drawSbdResult(Mat sbdRegion, RegionCellInfo sbdInfo, String recognizedSBD) {
        Mat output = sbdRegion.clone();
        if (recognizedSBD != null) {
            for (int col = 0; col < recognizedSBD.length(); col++) {
                char digitChar = recognizedSBD.charAt(col);
                int rowIndex = -1;
                try {
                    rowIndex = Character.getNumericValue(digitChar);
                } catch (Exception e) {
                    Log.w(TAG, "SBD parse error: " + e.getMessage());
                }
                if (rowIndex >= 0 && rowIndex < sbdInfo.rows && col < sbdInfo.cols) {
                    drawDotInCell(output, sbdInfo, rowIndex, col, new Scalar(0, 255, 0), 5);
                }
            }
        }
        return output;
    }

    // Vẽ dot cho Mã đề
    public static Mat drawMaDeResult(Mat maDeRegion, RegionCellInfo maDeInfo, String recognizedMaDe) {
        Mat output = maDeRegion.clone();
        if (recognizedMaDe != null) {
            for (int col = 0; col < recognizedMaDe.length(); col++) {
                char digitChar = recognizedMaDe.charAt(col);
                int rowIndex = -1;
                try {
                    rowIndex = Character.getNumericValue(digitChar);
                } catch (Exception e) {
                    Log.w(TAG, "MaDe parse error: " + e.getMessage());
                }
                if (rowIndex >= 0 && rowIndex < maDeInfo.rows && col < maDeInfo.cols) {
                    drawDotInCell(output, maDeInfo, rowIndex, col, new Scalar(0, 255, 0), 5);
                }
            }
        }
        return output;
    }

    // Vẽ highlight cho một ROI Exam với grid (ví dụ Exam Top hoặc Exam Bottom)
    public static Mat drawExamResult(
            Mat examRegion,
            RegionCellInfo examInfo,
            List<String> recognizedAnswers,
            List<String> correctAnswers
    ) {
        Mat output = examRegion.clone();
        int questionCount = recognizedAnswers != null ? recognizedAnswers.size() : 0;
        for (int q = 0; q < questionCount; q++) {
            int userCol = answerToColIndex(recognizedAnswers.get(q));
            int correctCol = answerToColIndex((correctAnswers != null && q < correctAnswers.size())
                    ? correctAnswers.get(q) : null);
            Log.d(TAG, "Single ROI Exam - Q" + q + ": userCol=" + userCol + ", correctCol=" + correctCol);
            if (userCol == -1) {
                if (correctCol >= 0) {
                    drawCircleInCell(output, examInfo, q, correctCol, new Scalar(0, 255, 255), 8, 1);
                }
                continue;
            }
            boolean isCorrect = (userCol == correctCol && userCol >= 0);
            if (isCorrect) {
                drawCircleInCell(output, examInfo, q, userCol, new Scalar(0, 255, 0), 8, 1);
            } else {
                drawCircleInCell(output, examInfo, q, userCol, new Scalar(255, 0, 0), 8, 1);
                if (correctCol >= 0) {
                    drawCircleInCell(output, examInfo, q, correctCol, new Scalar(255, 255, 0), 8, 1);
                }
            }
        }
        return output;
    }

    /* --- Các hàm hỗ trợ vẽ --- */
    // Vẽ dot tại trung tâm cell.
    private static void drawDotInCell(Mat canvas, RegionCellInfo info, int row, int col, Scalar color, int dotRadius) {
        double cellWidth = info.regionW / info.cols;
        double cellHeight = info.regionH / info.rows;
        double xStart = info.regionX + col * cellWidth;
        double yStart = info.regionY + row * cellHeight;
        double cx = xStart + cellWidth / 2.0;
        double cy = yStart + cellHeight / 2.0;
        Log.d(TAG, "drawDotInCell: row=" + row + ", col=" + col + ", center=(" + cx + "," + cy + "), dotRadius=" + dotRadius);
        Imgproc.circle(canvas, new Point(cx, cy), dotRadius, color, Core.FILLED);
    }

    // Vẽ vòng tròn highlight tại trung tâm cell.
    private static void drawCircleInCell(Mat canvas, RegionCellInfo info, int row, int col, Scalar color, int desiredRadius, int thickness) {
        if (row < 0 || row >= info.rows || col < 0 || col >= info.cols) {
            Log.d(TAG, "drawCircleInCell: invalid row=" + row + ", col=" + col);
            return;
        }
        double cellWidth = info.regionW / info.cols;
        double cellHeight = info.regionH / info.rows;
        double xStart = info.regionX + col * cellWidth;
        double yStart = info.regionY + row * cellHeight;
        double cx = xStart + cellWidth / 2.0;
        double cy = yStart + cellHeight / 2.0;
        int calcRadius = (int)(Math.min(cellWidth, cellHeight) / 6);
        calcRadius = Math.max(calcRadius, 5);
        int radius = (desiredRadius > calcRadius) ? desiredRadius : calcRadius;
        Log.d(TAG, "drawCircleInCell: row=" + row + ", col=" + col + ", center=(" + cx + "," + cy + "), radius=" + radius + ", color=" + color.toString());
        Imgproc.circle(canvas, new Point(cx, cy), radius, color, thickness);
    }

    private static int answerToColIndex(String ans) {
        if (ans == null) return -1;
        ans = ans.trim().toUpperCase();
        switch (ans) {
            case "A": return 0;
            case "B": return 1;
            case "C": return 2;
            case "D": return 3;
            default: return -1;
        }
    }

    /**
     * Lớp chứa thông tin vùng ROI và cấu trúc lưới.
     */
    public static class RegionCellInfo {
        public double regionX, regionY, regionW, regionH;
        public int rows, cols;
        public RegionCellInfo(double x, double y, double w, double h, int r, int c) {
            this.regionX = x;
            this.regionY = y;
            this.regionW = w;
            this.regionH = h;
            this.rows = r;
            this.cols = c;
        }
    }
}
