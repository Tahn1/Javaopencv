package com.example.javaopencv.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "answers")
public class Answer {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int examId;
    public String code;
    public int cauSo;
    public String dapAn;

    public Answer(int examId, String code, int cauSo, String dapAn) {
        this.examId = examId;
        this.code = code;
        this.cauSo = cauSo;
        this.dapAn = dapAn;
    }
}
