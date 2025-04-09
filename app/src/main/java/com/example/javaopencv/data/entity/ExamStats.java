package com.example.javaopencv.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "exam_stats")
public class ExamStats {
    @PrimaryKey
    public int examId;
    public int soBaiCham;
    public int soDapAn;
    public double diemTrungBinh;
    public double diemThapNhat;
    public double diemCaoNhat;

    public ExamStats(int examId, int soBaiCham, int soDapAn, double diemTrungBinh, double diemThapNhat, double diemCaoNhat) {
        this.examId = examId;
        this.soBaiCham = soBaiCham;
        this.soDapAn = soDapAn;
        this.diemTrungBinh = diemTrungBinh;
        this.diemThapNhat = diemThapNhat;
        this.diemCaoNhat = diemCaoNhat;
    }
}
