package com.example.javaopencv.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class GradeResult {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo public int examId;
    @ColumnInfo public String maDe;
    @ColumnInfo public String sbd;
    @ColumnInfo public String answersCsv;      // CSV đáp án đã chọn (1..4)
    @ColumnInfo public int correctCount;
    @ColumnInfo public int totalQuestions;
    @ColumnInfo public double score;
    @ColumnInfo public String imagePath;
    @ColumnInfo public float focusX;
    @ColumnInfo public float focusY;
    @ColumnInfo public long timestamp;

    /**
     * Constructor để tạo mới trước khi insert vào DB.
     * Room sẽ bỏ qua constructor này nhờ @Ignore.
     */
    @Ignore
    public GradeResult(int examId,
                       String maDe,
                       String sbd,
                       String answersCsv,
                       int correctCount,
                       int totalQuestions,
                       double score,
                       String imagePath,
                       float focusX,
                       float focusY) {
        this.examId         = examId;
        this.maDe           = maDe;
        this.sbd            = sbd;
        this.answersCsv     = answersCsv;
        this.correctCount   = correctCount;
        this.totalQuestions = totalQuestions;
        this.score          = score;
        this.imagePath      = imagePath;
        this.focusX         = focusX;
        this.focusY         = focusY;
        this.timestamp      = System.currentTimeMillis();
    }

    /**
     * Constructor đầy đủ (12 tham số) Room sẽ sử dụng để load và update.
     */
    public GradeResult(long id,
                       int examId,
                       String maDe,
                       String sbd,
                       String answersCsv,
                       int correctCount,
                       int totalQuestions,
                       double score,
                       String imagePath,
                       float focusX,
                       float focusY,
                       long timestamp) {
        this.id             = id;
        this.examId         = examId;
        this.maDe           = maDe;
        this.sbd            = sbd;
        this.answersCsv     = answersCsv;
        this.correctCount   = correctCount;
        this.totalQuestions = totalQuestions;
        this.score          = score;
        this.imagePath      = imagePath;
        this.focusX         = focusX;
        this.focusY         = focusY;
        this.timestamp      = timestamp;
    }

    public int getTotalQuestions() {
        return totalQuestions;
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
                && (answersCsv != null ? answersCsv.equals(g.answersCsv) : g.answersCsv == null)
                && imagePath.equals(g.imagePath);
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + examId;
        result = 31 * result + maDe.hashCode();
        result = 31 * result + sbd.hashCode();
        result = 31 * result + (answersCsv != null ? answersCsv.hashCode() : 0);
        result = 31 * result + correctCount;
        result = 31 * result + totalQuestions;
        long temp = Double.doubleToLongBits(score);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + imagePath.hashCode();
        result = 31 * result + Float.floatToIntBits(focusX);
        result = 31 * result + Float.floatToIntBits(focusY);
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }
}
