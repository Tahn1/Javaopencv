package com.example.javaopencv.omr;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import java.util.List;

public class HighlightUtils {
    /**
     * Vẽ highlight overlay lên ảnh grid.
     * @param region ảnh gốc (ROI) của grid
     * @param cols số cột của grid
     * @param rows số hàng của grid
     * @param recognizedAnswers danh sách đáp án nhận dạng (theo thứ tự row-major, kích thước = rows*? – ở đây bạn sẽ dùng số hàng ứng với mỗi vùng)
     * @param correctAnswers danh sách đáp án chuẩn (cùng kích thước)
     * @return ảnh đã được vẽ highlight overlay
     */
    public static Mat applyHighlightOverlay(Mat region, int cols, int rows, List<String> recognizedAnswers, List<String> correctAnswers) {
        Mat annotated = region.clone();
        int cellWidth = region.cols() / cols;
        int cellHeight = region.rows() / rows;
        int radius = Math.min(cellWidth, cellHeight) / 4;

        int index = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (index >= recognizedAnswers.size() || index >= correctAnswers.size()) break;
                String recog = recognizedAnswers.get(index);
                String corr = correctAnswers.get(index);
                int centerX = c * cellWidth + cellWidth / 2;
                int centerY = r * cellHeight + cellHeight / 2;
                Point center = new Point(centerX, centerY);
                if (recog != null && recog.equals(corr)) {
                    // Đúng: vòng tròn xanh lá cây
                    Imgproc.circle(annotated, center, radius, new Scalar(0, 255, 0), 2);
                } else {
                    // Sai: vòng tròn đỏ ngoài, vòng tròn vàng bên trong
                    Imgproc.circle(annotated, center, radius, new Scalar(0, 0, 255), 2);
                    int innerRadius = radius / 2;
                    Imgproc.circle(annotated, center, innerRadius, new Scalar(0, 255, 255), 2);
                }
                index++;
            }
        }
        return annotated;
    }
}
