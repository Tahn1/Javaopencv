package com.example.javaopencv.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ExamCodeEntry")
public class ExamCodeEntry {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int examId;
    public String code;
    public String answers;
    public int questionCount;

    public ExamCodeEntry(int examId, String code, String answers, int questionCount) {
        this.examId = examId;
        this.code = code;
        this.answers = answers;
        this.questionCount = questionCount;
    }
}

