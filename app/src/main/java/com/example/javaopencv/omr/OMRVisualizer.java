package com.example.javaopencv.omr;

import android.util.Log;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.List;

public class OMRVisualizer {
    private static final String TAG = "OMRVisualizer";

    /**
     * Wrapper giữ nguyên tên cũ annotate(...) để không phải sửa chỗ khác.
     */
    public static Mat annotate(
            Mat alignedMat,
            RegionCellInfo sbdInfo, String recognizedSBD,
            RegionCellInfo maDeInfo, String recognizedMaDe,
            RegionCellInfo examLeftInfo, List<String> answersLeft, List<String> correctLeft,
            RegionCellInfo examRightInfo, List<String> answersRight, List<String> correctRight
    ) {
        return annotateAll(
                alignedMat,
                sbdInfo, recognizedSBD,
                maDeInfo, recognizedMaDe,
                examLeftInfo, answersLeft, correctLeft,
                examRightInfo, answersRight, correctRight
        );
    }

    /**
     * Thực hiện vẽ dot/circle lên 4 vùng: SBD, Mã đề, Exam trái (Q1–10), Exam phải (Q11–20).
     */
    // Trong OMRVisualizer.java, thêm vào cuối lớp:
    public static Mat annotateAll(
            Mat alignedMat,
            RegionCellInfo sbdInfo, String recognizedSBD,
            RegionCellInfo maDeInfo, String recognizedMaDe,
            RegionCellInfo examLeftInfo, List<String> answersLeft, List<String> correctLeft,
            RegionCellInfo examRightInfo, List<String> answersRight, List<String> correctRight
    ) {
        Mat output = alignedMat.clone();
        // vẽ SBD
        Mat tmp = drawSbdResult(output.submat(
                (int)sbdInfo.regionY,
                (int)(sbdInfo.regionY + sbdInfo.regionH),
                (int)sbdInfo.regionX,
                (int)(sbdInfo.regionX + sbdInfo.regionW)
        ), sbdInfo, recognizedSBD);
        tmp.copyTo(output.submat(
                (int)sbdInfo.regionY,
                (int)(sbdInfo.regionY + sbdInfo.regionH),
                (int)sbdInfo.regionX,
                (int)(sbdInfo.regionX + sbdInfo.regionW)
        ));
        // vẽ Mã đề
        tmp = drawMaDeResult(output.submat(
                (int)maDeInfo.regionY,
                (int)(maDeInfo.regionY + maDeInfo.regionH),
                (int)maDeInfo.regionX,
                (int)(maDeInfo.regionX + maDeInfo.regionW)
        ), maDeInfo, recognizedMaDe);
        tmp.copyTo(output.submat(
                (int)maDeInfo.regionY,
                (int)(maDeInfo.regionY + maDeInfo.regionH),
                (int)maDeInfo.regionX,
                (int)(maDeInfo.regionX + maDeInfo.regionW)
        ));
        // vẽ Exam trái
        tmp = drawExamResult(output.submat(
                (int)examLeftInfo.regionY,
                (int)(examLeftInfo.regionY + examLeftInfo.regionH),
                (int)examLeftInfo.regionX,
                (int)(examLeftInfo.regionX + examLeftInfo.regionW)
        ), examLeftInfo, answersLeft, correctLeft);
        tmp.copyTo(output.submat(
                (int)examLeftInfo.regionY,
                (int)(examLeftInfo.regionY + examLeftInfo.regionH),
                (int)examLeftInfo.regionX,
                (int)(examLeftInfo.regionX + examLeftInfo.regionW)
        ));
        // vẽ Exam phải
        tmp = drawExamResult(output.submat(
                (int)examRightInfo.regionY,
                (int)(examRightInfo.regionY + examRightInfo.regionH),
                (int)examRightInfo.regionX,
                (int)(examRightInfo.regionX + examRightInfo.regionW)
        ), examRightInfo, answersRight, correctRight);
        tmp.copyTo(output.submat(
                (int)examRightInfo.regionY,
                (int)(examRightInfo.regionY + examRightInfo.regionH),
                (int)examRightInfo.regionX,
                (int)(examRightInfo.regionX + examRightInfo.regionW)
        ));

        return output;
    }


    // ------------------------------------------------------------------
    // Phần vẽ dot / circle lên từng ô
    // ------------------------------------------------------------------

    public static Mat drawSbdResult(Mat sbdRegion, RegionCellInfo sbdInfo, String recognizedSBD) {
        Mat output = sbdRegion.clone();
        if (recognizedSBD != null) {
            for (int col = 0; col < recognizedSBD.length(); col++) {
                int rowIndex;
                try {
                    rowIndex = Character.getNumericValue(recognizedSBD.charAt(col));
                } catch (Exception e) {
                    Log.w(TAG, "SBD parse error: " + e.getMessage());
                    continue;
                }
                if (rowIndex >= 0 && rowIndex < sbdInfo.rows && col < sbdInfo.cols) {
                    drawDotInCell(output, sbdInfo, rowIndex, col, new Scalar(0, 255, 0), 5);
                }
            }
        }
        return output;
    }

    public static Mat drawMaDeResult(Mat maDeRegion, RegionCellInfo maDeInfo, String recognizedMaDe) {
        Mat output = maDeRegion.clone();
        if (recognizedMaDe != null) {
            for (int col = 0; col < recognizedMaDe.length(); col++) {
                int rowIndex;
                try {
                    rowIndex = Character.getNumericValue(recognizedMaDe.charAt(col));
                } catch (Exception e) {
                    Log.w(TAG, "MaDe parse error: " + e.getMessage());
                    continue;
                }
                if (rowIndex >= 0 && rowIndex < maDeInfo.rows && col < maDeInfo.cols) {
                    drawDotInCell(output, maDeInfo, rowIndex, col, new Scalar(0, 255, 0), 5);
                }
            }
        }
        return output;
    }

    public static Mat drawExamResult(
            Mat examRegion,
            RegionCellInfo examInfo,
            List<String> recognizedAnswers,
            List<String> correctAnswers
    ) {
        Mat output = examRegion.clone();
        int questionCount = (recognizedAnswers != null) ? recognizedAnswers.size() : 0;
        for (int q = 0; q < questionCount; q++) {
            int userCol = answerToColIndex(recognizedAnswers.get(q));
            int correctCol = answerToColIndex(
                    (correctAnswers != null && q < correctAnswers.size())
                            ? correctAnswers.get(q)
                            : null
            );
            if (userCol < 0) {
                if (correctCol >= 0) {
                    drawCircleInCell(output, examInfo, q, correctCol, new Scalar(0, 255, 255), 8, 1);
                }
            } else if (userCol == correctCol) {
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

    private static void drawDotInCell(Mat canvas, RegionCellInfo info, int row, int col, Scalar color, int dotRadius) {
        double cellW = info.regionW / info.cols;
        double cellH = info.regionH / info.rows;
        double x = info.regionX + col * cellW + cellW/2;
        double y = info.regionY + row * cellH + cellH/2;
        Imgproc.circle(canvas, new Point(x, y), dotRadius, color, Core.FILLED);
    }

    private static void drawCircleInCell(Mat canvas, RegionCellInfo info,
                                         int row, int col, Scalar color,
                                         int desiredRadius, int thickness) {
        if (row<0||row>=info.rows||col<0||col>=info.cols) return;
        double cellW = info.regionW / info.cols;
        double cellH = info.regionH / info.rows;
        double x = info.regionX + col * cellW + cellW/2;
        double y = info.regionY + row * cellH + cellH/2;
        int radius = Math.max((int)(Math.min(cellW,cellH)/6), desiredRadius);
        Imgproc.circle(canvas, new Point(x, y), radius, color, thickness);
    }

    private static int answerToColIndex(String ans) {
        if (ans == null) return -1;
        switch (ans.trim().toUpperCase()) {
            case "A": return 0;
            case "B": return 1;
            case "C": return 2;
            case "D": return 3;
            default:  return -1;
        }
    }

    /** Thông tin vùng ROI và cấu trúc lưới (rows, cols) */
    public static class RegionCellInfo {
        public final double regionX, regionY, regionW, regionH;
        public final int rows, cols;
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
