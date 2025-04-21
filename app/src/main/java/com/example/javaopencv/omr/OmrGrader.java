package com.example.javaopencv.omr;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.example.javaopencv.data.AppDatabase;
import com.example.javaopencv.data.entity.Answer;
import com.example.javaopencv.data.dao.AnswerDao;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OmrGrader {
    private static final String TAG = "OmrGrader";

    /** Kết quả cuối cùng trả về cho UI */
    public static class Result {
        public Bitmap annotatedBitmap;
        public String sbd;
        public String maDe;
        public int correctCount;
        public double score;
    }

    /**
     * Chấm bài từ bitmap đã chụp, dựa trên dữ liệu trong DB (examId, mã đề, đáp án),
     * rồi trả về ảnh đã annotate và thống kê điểm số.
     */
    public static Result grade(Bitmap capturedBitmap, int examId, Context context) {
        Result finalResult = new Result();

        // 1) Xử lý OMR: trích xuất SBD, mã đề và đáp án
        OMRProcessor.OMRResult omrResult = OMRProcessor.process(capturedBitmap, context);
        if (omrResult == null || omrResult.sbd == null || omrResult.alignedMat == null) {
            Log.e(TAG, "OMR processing failed");
            return null;
        }

        String recognizedSBD     = omrResult.sbd;
        String recognizedMaDe    = omrResult.maDe;
        List<String> answers     = omrResult.answers;

        // 2) Lấy đáp án đúng từ DB dựa trên examId và mã đề
        AnswerDao answerDao = AppDatabase
                .getInstance(context)
                .answerDao();
        List<Answer> answersInDb = answerDao.getAnswersByExamAndCodeSync(examId, recognizedMaDe);

        Map<Integer, String> correctAnswerMap = new HashMap<>();
        for (Answer a : answersInDb) {
            correctAnswerMap.put(a.cauSo, a.dapAn);
        }

        // 3) So sánh để đếm số câu đúng
        int correctCount = 0;
        for (int i = 0; i < answers.size(); i++) {
            String userAns = answers.get(i);
            String trueAns = correctAnswerMap.getOrDefault(i+1, "X");
            if (userAns.equals(trueAns)) {
                correctCount++;
            }
        }
        double score = ((double) correctCount / answers.size()) * 10.0;

        // 4) Vẽ annotate lên ảnh đã căn chỉnh
        Mat aligned = omrResult.alignedMat;
        Bitmap bmp = Bitmap.createBitmap(aligned.cols(), aligned.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(aligned, bmp);

        // Vẽ overlay SBD, mã đề và đáp án (xanh/đỏ) lên bmp
        Mat annotatedMat = OMRVisualizer.annotate(aligned, omrResult.sbd, omrResult.maDe, answers, correctAnswerMap);

        // 5) Chuyển Mat annotate về Bitmap
        Bitmap annotatedBmp = Bitmap.createBitmap(annotatedMat.cols(), annotatedMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(annotatedMat, annotatedBmp);

        // Chuẩn bị kết quả trả về
        finalResult.annotatedBitmap = annotatedBmp;
        finalResult.sbd             = recognizedSBD;
        finalResult.maDe            = recognizedMaDe;
        finalResult.correctCount    = correctCount;
        finalResult.score           = score;

        return finalResult;
    }
}
