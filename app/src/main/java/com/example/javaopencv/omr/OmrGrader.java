package com.example.javaopencv.omr;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.example.javaopencv.data.AppDatabase;
import com.example.javaopencv.data.entity.Answer;
import com.example.javaopencv.data.dao.AnswerDao;
import com.example.javaopencv.omr.GridUtils;
import com.example.javaopencv.omr.ImageDebugUtils;
import com.example.javaopencv.omr.MarkerUtils;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OmrGrader {
    private static final String TAG = "OmrGrader";

    public static class Result {
        public Bitmap annotatedBitmap;
        public String sbd;
        public String maDe;
        public int correctCount;
        public double score;
    }

    public static Result grade(Bitmap capturedBitmap, int examId, Context context) {
        Result finalResult = new Result();

        OMRProcessor.OMRResult omrResult = OMRProcessor.process(capturedBitmap, context);
        if (omrResult == null || omrResult.sbd == null || omrResult.alignedMat == null) {
            Log.e(TAG, "OMR processing failed");
            return null;
        }

        String recognizedSBD = omrResult.sbd;
        String recognizedMaDe = omrResult.maDe;
        List<String> recognizedAnswers = omrResult.answers;

        AppDatabase db = AppDatabase.getInstance(context);
        AnswerDao answerDao = db.answerDao();
        List<Answer> answersInDb = answerDao.getAnswersByExamAndCodeSync(examId, recognizedMaDe);

        Map<Integer, String> correctAnswerMap = new HashMap<>();
        for (Answer a : answersInDb) {
            correctAnswerMap.put(a.cauSo, a.dapAn);
        }

        List<String> correctAnswers = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            correctAnswers.add(correctAnswerMap.getOrDefault(i, "X"));
        }

        int correctCount = 0;
        for (int i = 0; i < recognizedAnswers.size(); i++) {
            if (recognizedAnswers.get(i).equals(correctAnswers.get(i))) {
                correctCount++;
            }
        }
        double score = ((double) correctCount / 20.0) * 10.0;

        Mat processedMat = OMRProcessor.postprocessAlignedImage(omrResult.alignedMat, context);
        List<MatOfPoint> smallMarkers = MarkerUtils.findSmallMarkersOnBChannel(processedMat, 165.0, 225.0);
        List<Point> centers = new ArrayList<>();
        for (MatOfPoint cnt : smallMarkers) centers.add(MarkerUtils.centerOf(cnt));
        List<Point> ordered = MarkerUtils.orderMarkersCustom(centers);
        if (ordered.size() < 5) {
            Log.e(TAG, "Not enough ordered small markers");
            return null;
        }

        MarkerUtils.RegionResult regions = MarkerUtils.extractROI(
                processedMat, ordered.get(0), ordered.get(1), ordered.get(2), ordered.get(3), ordered.get(4),
                context, processedMat.clone()
        );

        Mat aligned = omrResult.alignedMat;

        // SBD
        Mat sbdColor = new Mat(aligned, new Rect((int) regions.sbdOffsetX, (int) regions.sbdOffsetY,
                regions.sbdRoi.cols(), regions.sbdRoi.rows()));
        Mat sbdAnnotated = OMRVisualizer.drawSbdResult(sbdColor,
                new OMRVisualizer.RegionCellInfo(0, 0, sbdColor.cols(), sbdColor.rows(), 10, 6), recognizedSBD);
        sbdAnnotated.copyTo(aligned.submat((int) regions.sbdOffsetY, (int) regions.sbdOffsetY + sbdAnnotated.rows(),
                (int) regions.sbdOffsetX, (int) regions.sbdOffsetX + sbdAnnotated.cols()));

        // MaDe
        Mat maDeColor = new Mat(aligned, new Rect((int) regions.maDeOffsetX, (int) regions.maDeOffsetY,
                regions.maDeRoi.cols(), regions.maDeRoi.rows()));
        Mat maDeAnnotated = OMRVisualizer.drawMaDeResult(maDeColor,
                new OMRVisualizer.RegionCellInfo(0, 0, maDeColor.cols(), maDeColor.rows(), 10, 3), recognizedMaDe);
        maDeAnnotated.copyTo(aligned.submat((int) regions.maDeOffsetY, (int) regions.maDeOffsetY + maDeAnnotated.rows(),
                (int) regions.maDeOffsetX, (int) regions.maDeOffsetX + maDeAnnotated.cols()));

        // ExamLeft
        Mat examLeft = new Mat(aligned, new Rect((int) regions.examLeftOffsetX, (int) regions.examLeftOffsetY,
                regions.examLeftRoi.cols(), regions.examLeftRoi.rows()));
        Mat examLeftGrid = GridUtils.drawGridOnImage(examLeft, 4, 10, 0, new Scalar(255, 255, 255), 1);
        Mat examLeftAnnotated = OMRVisualizer.drawExamResult(examLeftGrid,
                new OMRVisualizer.RegionCellInfo(0, 0, examLeft.cols(), examLeft.rows(), 10, 4),
                recognizedAnswers.subList(0, 10), correctAnswers.subList(0, 10));
        examLeftAnnotated.copyTo(aligned.submat((int) regions.examLeftOffsetY,
                (int) regions.examLeftOffsetY + examLeftAnnotated.rows(),
                (int) regions.examLeftOffsetX, (int) regions.examLeftOffsetX + examLeftAnnotated.cols()));

        // ExamRight
        Mat examRight = new Mat(aligned, new Rect((int) regions.examRightOffsetX, (int) regions.examRightOffsetY,
                regions.examRightRoi.cols(), regions.examRightRoi.rows()));
        Mat examRightGrid = GridUtils.drawGridOnImage(examRight, 4, 10, 0, new Scalar(255, 255, 255), 1);
        Mat examRightAnnotated = OMRVisualizer.drawExamResult(examRightGrid,
                new OMRVisualizer.RegionCellInfo(0, 0, examRight.cols(), examRight.rows(), 10, 4),
                recognizedAnswers.subList(10, 20), correctAnswers.subList(10, 20));
        examRightAnnotated.copyTo(aligned.submat((int) regions.examRightOffsetY,
                (int) regions.examRightOffsetY + examRightAnnotated.rows(),
                (int) regions.examRightOffsetX, (int) regions.examRightOffsetX + examRightAnnotated.cols()));

        ImageDebugUtils.saveDebugImage(aligned, "final_annotated_debug.jpg", context);

        Bitmap bmp = Bitmap.createBitmap(aligned.cols(), aligned.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(aligned, bmp);

        finalResult.annotatedBitmap = bmp;
        finalResult.sbd = recognizedSBD;
        finalResult.maDe = recognizedMaDe;
        finalResult.correctCount = correctCount;
        finalResult.score = score;

        return finalResult;
    }
}
