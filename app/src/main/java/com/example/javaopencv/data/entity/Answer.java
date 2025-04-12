package com.example.javaopencv.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "answers")
public class Answer {
    @PrimaryKey(autoGenerate = true)
    public int id;       // Khóa chính tự tăng
    public int examId;   // ID bài thi
    public String code;  // Mã đề
    public int cauSo;    // Số thứ tự câu
    public String dapAn; // Đáp án (A/B/C/D hoặc null)

    public Answer(int examId, String code, int cauSo, String dapAn) {
        this.examId = examId;
        this.code = code;
        this.cauSo = cauSo;
        this.dapAn = dapAn;
    }
}