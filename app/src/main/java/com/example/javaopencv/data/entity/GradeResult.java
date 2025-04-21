package com.example.javaopencv.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class GradeResult {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo public int examId;
    @ColumnInfo public String maDe;
    @ColumnInfo public String sbd;
    @ColumnInfo public int correctCount;
    @ColumnInfo public int totalQuestions;
    @ColumnInfo public double score;
    @ColumnInfo public String imagePath;
    @ColumnInfo public float focusX;   // thêm
    @ColumnInfo public float focusY;   // thêm
    @ColumnInfo public long timestamp;

    /**
     * @param examId         ID của bài kiểm tra (khóa ngoại)
     * @param maDe           mã đề
     * @param sbd            số báo danh
     * @param correctCount   số câu đúng
     * @param totalQuestions tổng số câu
     * @param score          điểm (0–10)
     * @param imagePath      đường dẫn đến ảnh debug đã chấm
     * @param focusX         tỷ lệ 0..1 điểm crop theo trục X
     * @param focusY         tỷ lệ 0..1 điểm crop theo trục Y
     */
    public GradeResult(int examId,
                       String maDe,
                       String sbd,
                       int correctCount,
                       int totalQuestions,
                       double score,
                       String imagePath,
                       float focusX,
                       float focusY) {
        this.examId         = examId;
        this.maDe           = maDe;
        this.sbd            = sbd;
        this.correctCount   = correctCount;
        this.totalQuestions = totalQuestions;
        this.score          = score;
        this.imagePath      = imagePath;
        this.focusX         = focusX;
        this.focusY         = focusY;
        this.timestamp      = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GradeResult)) return false;
        GradeResult g = (GradeResult) o;
        return id == g.id
                && examId == g.examId
                && correctCount == g.correctCount
                && totalQuestions == g.totalQuestions
                && Double.compare(g.score, score) == 0
                && Float.compare(g.focusX, focusX) == 0
                && Float.compare(g.focusY, focusY) == 0
                && timestamp == g.timestamp
                && maDe.equals(g.maDe)
                && sbd.equals(g.sbd)
                && imagePath.equals(g.imagePath);
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + examId;
        long temp = Double.doubleToLongBits(score);
        result = 31 * result + maDe.hashCode();
        result = 31 * result + sbd.hashCode();
        result = 31 * result + correctCount;
        result = 31 * result + totalQuestions;
        result = 31 * result + (int)(temp ^ (temp >>> 32));
        result = 31 * result + imagePath.hashCode();
        result = 31 * result + Float.floatToIntBits(focusX);
        result = 31 * result + Float.floatToIntBits(focusY);
        result = 31 * result + (int)(timestamp ^ (timestamp >>> 32));
        return result;
    }
}
