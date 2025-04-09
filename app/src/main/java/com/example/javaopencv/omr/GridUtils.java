package com.example.javaopencv.omr;

import android.content.Context;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class GridUtils {

    /**
     * Chia một vùng ảnh (regionImg) thành các ô dựa theo số cột và hàng cho trước.
     * Nếu headerRows > 0 thì phần header không được chia vào grid.
     *
     * @param regionImg Ảnh nguồn (ROI)
     * @param cols Số cột.
     * @param rows Số hàng.
     * @param headerRows Số hàng header (không bao gồm trong grid), nếu không có thì truyền 0.
     * @return Danh sách 2 chiều, mỗi phần tử là danh sách các ô (Mat) của một hàng.
     */
    public static List<List<Mat>> splitRegionIntoCells(Mat regionImg, int cols, int rows, int headerRows) {
        List<List<Mat>> cells = new ArrayList<>();
        int regionWidth = regionImg.cols();
        int regionHeight = regionImg.rows();
        int yOffset = 0;
        if (headerRows > 0) {
            int headerHeight = regionHeight / (rows + headerRows);
            yOffset = headerHeight * headerRows;
            regionHeight = regionHeight - yOffset;
        }
        int cellWidth = regionWidth / cols;
        int cellHeight = regionHeight / rows;
        for (int r = 0; r < rows; r++) {
            List<Mat> rowCells = new ArrayList<>();
            for (int c = 0; c < cols; c++) {
                int xStart = c * cellWidth;
                int yStart = yOffset + r * cellHeight;
                Rect cellRect = new Rect(xStart, yStart, cellWidth, cellHeight);
                rowCells.add(new Mat(regionImg, cellRect));
            }
            cells.add(rowCells);
        }
        return cells;
    }

    /**
     * Overload không có headerRows.
     */
    public static List<List<Mat>> splitRegionIntoCells(Mat regionImg, int cols, int rows) {
        return splitRegionIntoCells(regionImg, cols, rows, 0);
    }

    /**
     * Kiểm tra xem một ô có được tô hay không dựa trên tỉ lệ số pixel trắng.
     * Ngưỡng tô được dùng là 0.14.
     *
     * @param cell Ảnh của ô (Mat).
     * @param fillThreshold Ngưỡng tô (ví dụ 0.14).
     * @return true nếu ô được tô vượt qua ngưỡng, false nếu không.
     */
    public static boolean isCellMarked(Mat cell, double fillThreshold) {
        int totalPixels = cell.rows() * cell.cols();
        int whitePixels = Core.countNonZero(cell);
        double ratio = (double) whitePixels / totalPixels;
        // Debug: in ra tỉ lệ tô của từng cell
        System.out.println("Cell fill ratio: " + ratio);
        return ratio > fillThreshold;
    }

    /**
     * Trích xuất đáp án từ grid cho phần trắc nghiệm theo hàng.
     * Với mỗi hàng, hàm duyệt qua các ô và chọn ô có tỉ lệ tô cao nhất (nếu vượt qua ngưỡng 0.14),
     * ánh xạ theo thứ tự "A", "B", "C", "D". Nếu không có ô nào vượt qua ngưỡng thì trả về "X".
     *
     * @param cells Grid các ô (List<List<Mat>>).
     * @param fillThreshold Ngưỡng tô (0.14).
     * @return Danh sách đáp án.
     */
    public static List<String> extractExamAnswers(List<List<Mat>> cells, double fillThreshold) {
        List<String> answers = new ArrayList<>();
        String[] choices = {"A", "B", "C", "D"};
        for (int i = 0; i < cells.size(); i++) {
            double bestRatio = 0;
            int bestChoice = -1;
            List<Mat> row = cells.get(i);
            for (int j = 0; j < row.size(); j++) {
                Mat cell = row.get(j);
                int total = cell.rows() * cell.cols();
                int white = Core.countNonZero(cell);
                double ratio = (double) white / total;
                if (ratio > fillThreshold && ratio > bestRatio) {
                    bestRatio = ratio;
                    bestChoice = j;
                }
            }
            if (bestChoice == -1)
                answers.add("X");
            else
                answers.add(choices[bestChoice]);
        }
        return answers;
    }

    /**
     * Trích xuất chữ số (hoặc ký tự) từ grid cho SBD hoặc Mã đề theo cột.
     * Với mỗi cột, chọn ô có tỉ lệ tô cao nhất nếu vượt qua ngưỡng 0.14, nếu không có thì "X".
     *
     * @param cells Grid các ô.
     * @param fillThreshold Ngưỡng tô (0.14).
     * @return Chuỗi ký tự được trích xuất.
     */
    public static String extractDigits(List<List<Mat>> cells, double fillThreshold) {
        StringBuilder digits = new StringBuilder();
        int cols = cells.get(0).size();
        int rows = cells.size();
        for (int j = 0; j < cols; j++) {
            double bestRatio = 0;
            int bestRow = -1;
            for (int i = 0; i < rows; i++) {
                Mat cell = cells.get(i).get(j);
                int total = cell.rows() * cell.cols();
                int white = Core.countNonZero(cell);
                double ratio = (double) white / total;
                if (ratio > fillThreshold && ratio > bestRatio) {
                    bestRatio = ratio;
                    bestRow = i;
                }
            }
            if (bestRow == -1)
                digits.append("X");
            else
                digits.append(bestRow);
        }
        return digits.toString();
    }

    /**
     * So sánh theo cột cho grid SBD và Mã đề, trả về kết quả là mảng gồm SBD và Mã đề.
     *
     * @param sbdCells  Grid SBD.
     * @param maDeCells Grid Mã đề.
     * @param fillThreshold Ngưỡng tô (0.14).
     * @return Mảng hai phần tử chứa SBD và Mã đề.
     */
    public static String[] extractKeysByColumns(List<List<Mat>> sbdCells, List<List<Mat>> maDeCells, double fillThreshold) {
        String sbdKey = extractDigits(sbdCells, fillThreshold);
        String maDeKey = extractDigits(maDeCells, fillThreshold);
        return new String[]{sbdKey, maDeKey};
    }

    /**
     * Vẽ grid overlay lên vùng ảnh (ROI) và trả về ảnh debug với overlay grid.
     *
     * @param regionImg Ảnh ROI nguồn.
     * @param cols Số cột.
     * @param rows Số hàng.
     * @param headerRows Số hàng header (không được chia vào grid).
     * @param color Màu của grid (ví dụ new Scalar(255, 255, 255) cho màu trắng).
     * @param thickness Độ dày của đường grid.
     * @return Ảnh debug có overlay grid.
     */
    public static Mat drawGridOnImage(Mat regionImg, int cols, int rows, int headerRows, Scalar color, int thickness) {
        Mat debugImg = regionImg.clone();
        int regionWidth = regionImg.cols();
        int regionHeight = regionImg.rows();
        int yOffset = 0;
        if (headerRows > 0) {
            int headerHeight = regionHeight / (rows + headerRows);
            yOffset = headerHeight * headerRows;
        }
        int cellWidth = regionWidth / cols;
        int cellHeight = (regionHeight - yOffset) / rows;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int xStart = c * cellWidth;
                int yStart = yOffset + r * cellHeight;
                Rect cellRect = new Rect(xStart, yStart, cellWidth, cellHeight);
                Imgproc.rectangle(debugImg, cellRect.tl(), cellRect.br(), color, thickness);
            }
        }
        return debugImg;
    }

    /**
     * Vẽ grid overlay lên một vùng xác định của ảnh căn chỉnh.
     *
     * @param alignedImg Ảnh căn chỉnh.
     * @param regionX Tọa độ X của vùng.
     * @param regionY Tọa độ Y của vùng.
     * @param regionW Chiều rộng vùng.
     * @param regionH Chiều cao vùng.
     * @param cols Số cột.
     * @param rows Số hàng.
     * @param color Màu của grid (ví dụ new Scalar(255, 255, 255)).
     * @param thickness Độ dày của đường grid.
     * @return Ảnh có overlay grid.
     */
    public static Mat drawRegionGrid(Mat alignedImg, double regionX, double regionY, double regionW, double regionH,
                                     int cols, int rows, Scalar color, int thickness) {
        Mat overlay = alignedImg.clone();
        int cellWidth = (int) (regionW / cols);
        int cellHeight = (int) (regionH / rows);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int xStart = (int) (regionX + c * cellWidth);
                int yStart = (int) (regionY + r * cellHeight);
                int xEnd = xStart + cellWidth;
                int yEnd = yStart + cellHeight;
                Imgproc.rectangle(overlay, new Point(xStart, yStart), new Point(xEnd, yEnd), color, thickness);
            }
        }
        return overlay;
    }

    /**
     * Debug grid trên ROI: vẽ grid lên ảnh và lưu debug ảnh.
     *
     * @param regionImg Ảnh ROI cần debug.
     * @param cols Số cột của grid.
     * @param rows Số hàng của grid.
     * @param headerRows Số hàng header (nếu có).
     * @param context Context để lưu debug ảnh.
     * @param filename Tên file debug (ví dụ "debug_grid_roi.jpg").
     */
    public static void debugGridOnImage(Mat regionImg, int cols, int rows, int headerRows, Scalar color, int thickness, Context context, String filename) {
        Mat debugImg = drawGridOnImage(regionImg, cols, rows, headerRows, color, thickness);
        ImageDebugUtils.saveDebugImage(debugImg, filename, context);
    }
}
