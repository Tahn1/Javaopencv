package com.example.javaopencv.omr;

import java.util.List;

public class OMRResult {
    private String studentId;
    private String examCode;
    private List<String> answers;

    public OMRResult(String studentId, String examCode, List<String> answers) {
        this.studentId = studentId;
        this.examCode = examCode;
        this.answers = answers;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getExamCode() {
        return examCode;
    }

    public List<String> getAnswers() {
        return answers;
    }
}
