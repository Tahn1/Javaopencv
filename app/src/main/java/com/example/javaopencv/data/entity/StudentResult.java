package com.example.javaopencv.data.entity;

public class StudentResult {
    private final long studentId;
    private final String name;
    private final String studentNumber;
    private Double score;    // Bỏ final để có thể thay đổi
    private String note;     // Bỏ final để có thể thay đổi

    public StudentResult(long studentId, String name, String studentNumber,
                         Double score, String note) {
        this.studentId     = studentId;
        this.name          = name;
        this.studentNumber = studentNumber;
        this.score         = score;
        this.note          = note;
    }

    public long   getStudentId()       { return studentId; }
    public String getName()            { return name; }
    public String getStudentNumber()   { return studentNumber; }
    public Double getScore()           { return score; }
    public String getNote()            { return note; }

    /** Cho phép cập nhật điểm trực tiếp nếu cần */
    public void setScore(Double score) {
        this.score = score;
    }

    /** Cho phép cập nhật ghi chú */
    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StudentResult)) return false;
        StudentResult other = (StudentResult) o;
        return studentId == other.studentId
                && name.equals(other.name)
                && studentNumber.equals(other.studentNumber)
                && ((score == null && other.score == null) ||
                (score != null && score.equals(other.score)))
                && ((note == null && other.note == null) ||
                (note != null && note.equals(other.note)));
    }

    @Override
    public int hashCode() {
        int result = Long.valueOf(studentId).hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + studentNumber.hashCode();
        result = 31 * result + (score != null ? score.hashCode() : 0);
        result = 31 * result + (note  != null ? note.hashCode()  : 0);
        return result;
    }
}
