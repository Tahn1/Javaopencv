package com.example.javaopencv.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "exams")
public class Exam {
    @PrimaryKey
    public int id;       // examId (phải là duy nhất)
    public String title;
    public String phieu;
    public int soCau;    // Số câu
    public String date;

    public Exam(int id, String title, String phieu, int soCau, String date) {
        this.id = id;
        this.title = title;
        this.phieu = phieu;
        this.soCau = soCau;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public int getSoCau() {
        return soCau;
    }
}
