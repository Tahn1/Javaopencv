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
    // Thêm trường note
    @ColumnInfo public String note;

    /** Room sẽ bỏ qua constructor này. */
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
        this.note           = "";  // mặc định rỗng
    }

    /** Room sẽ sử dụng constructor đầy đủ này. */
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
                       long timestamp,
                       String note) {
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
        this.note           = note;
    }

    // --- Getters ---
    public long getId()               { return id; }
    public int getExamId()            { return examId; }
    public String getMaDe()           { return maDe; }
    public String getSbd()            { return sbd; }
    public String getAnswersCsv()     { return answersCsv; }
    public int getCorrectCount()      { return correctCount; }
    public int getTotalQuestions()    { return totalQuestions; }
    public double getScore()          { return score; }
    public String getImagePath()      { return imagePath; }
    public float getFocusX()          { return focusX; }
    public float getFocusY()          { return focusY; }
    public long getTimestamp()        { return timestamp; }
    public String getNote()           { return note; }

    // --- Setters ---
    public void setId(long id)         { this.id = id; }
    public void setExamId(int examId)  { this.examId = examId; }
    public void setMaDe(String maDe)   { this.maDe = maDe; }
    public void setSbd(String sbd)     { this.sbd = sbd; }
    public void setAnswersCsv(String answersCsv) { this.answersCsv = answersCsv; }
    public void setCorrectCount(int correctCount) { this.correctCount = correctCount; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
    public void setScore(double score) { this.score = score; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setFocusX(float focusX) { this.focusX = focusX; }
    public void setFocusY(float focusY) { this.focusY = focusY; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setNote(String note)  { this.note = note; }

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
                && imagePath.equals(g.imagePath)
                && (note != null ? note.equals(g.note) : g.note == null);
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
        result = 31 * result + (note != null ? note.hashCode() : 0);
        return result;
    }
}
