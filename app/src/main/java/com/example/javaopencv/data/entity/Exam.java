package com.example.javaopencv.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "exams")
public class Exam {
    @PrimaryKey
    public int id;
    public String title;
    public String phieu;
    public int soCau;
    public String date;

    public Exam(int id, String title, String phieu, int soCau, String date) {
        this.id = id;
        this.title = title;
        this.phieu = phieu;
        this.soCau = soCau;
        this.date = date;
    }
}
