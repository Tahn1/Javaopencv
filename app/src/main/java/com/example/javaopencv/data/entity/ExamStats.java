package com.example.javaopencv.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "exam_stats")
public class ExamStats {

    @PrimaryKey
    public int examId;           // examId của bài thi (khóa chính)
    public int soBaiCham;        // Số bài chấm
    public int soDapAn;          // Số đáp án (số mã đề) được tạo ra
    public double diemTrungBinh; // Điểm trung bình
    public double diemThapNhat;  // Điểm thấp nhất
    public double diemCaoNhat;   // Điểm cao nhất

    public ExamStats(int examId, int soBaiCham, int soDapAn, double diemTrungBinh, double diemThapNhat, double diemCaoNhat) {
        this.examId = examId;
        this.soBaiCham = soBaiCham;
        this.soDapAn = soDapAn;
        this.diemTrungBinh = diemTrungBinh;
        this.diemThapNhat = diemThapNhat;
        this.diemCaoNhat = diemCaoNhat;
    }
}
