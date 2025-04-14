package com.example.javaopencv.omr;

import java.util.List;
import java.util.Map;

public class OMRGrader {
    // Hàm gradeExam so sánh danh sách đáp án nhận dạng với answer key dựa vào mã đề
    public static int gradeExam(String examCode, List<String> recognizedAnswers, Map<String, List<String>> answerKeyMap) {
        List<String> correctKey = answerKeyMap.get(examCode);
        if (correctKey == null || correctKey.size() != recognizedAnswers.size()) {
            // Nếu không tìm thấy hoặc không đủ đáp án chuẩn, trả về 0
            return 0;
        }
        int count = 0;
        for (int i = 0; i < recognizedAnswers.size(); i++) {
            if (recognizedAnswers.get(i) != null && recognizedAnswers.get(i).equals(correctKey.get(i))) {
                count++;
            }
        }
        return count;
    }
}
