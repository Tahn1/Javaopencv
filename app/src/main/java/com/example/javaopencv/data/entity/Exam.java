package com.example.javaopencv.data.entity;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entity Exam với fields id, classId, title, phieu, soCau, date, className, subjectName.
 * SuppressWarnings vì các setter/getter được Room sử dụng, IDE có thể báo unused.
 */
@SuppressWarnings("unused")
@Entity(
        tableName = "exams",
        indices = @Index("classId")
)
public class Exam {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @Nullable
    @ColumnInfo(name = "classId")
    private Integer classId;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "phieu")
    private String phieu;

    @ColumnInfo(name = "so_cau")
    private int soCau;

    @ColumnInfo(name = "date")
    private String date;

    /** alias từ JOIN */
    @Nullable
    @ColumnInfo(name = "className")
    private String className;

    /** Tên môn học */
    @ColumnInfo(name = "subject_name", defaultValue = "")
    private String subjectName;

    /**
     * Constructor chính Room sẽ dùng (8 tham số: gồm className và subjectName)
     */
    public Exam(int id,
                @Nullable Integer classId,
                String title,
                String phieu,
                int soCau,
                String date,
                @Nullable String className,
                String subjectName) {
        this.id = id;
        this.classId = classId;
        this.title = title;
        this.phieu = phieu;
        this.soCau = soCau;
        this.date = date;
        this.className = className;
        this.subjectName = subjectName;
    }

    /**
     * Constructor để insert mới (6 tham số: classId, title, phieu, soCau, date, subjectName)
     */
    @Ignore
    public Exam(@Nullable Integer classId,
                String title,
                String phieu,
                int soCau,
                String date,
                String subjectName) {
        this.classId = classId;
        this.title = title;
        this.phieu = phieu;
        this.soCau = soCau;
        this.date = date;
        this.subjectName = subjectName;
    }

    /**
     * Constructor để update/chỉnh sửa (7 tham số: id, classId, title, phieu, soCau, date, subjectName)
     */
    @Ignore
    public Exam(int id,
                @Nullable Integer classId,
                String title,
                String phieu,
                int soCau,
                String date,
                String subjectName) {
        this.id = id;
        this.classId = classId;
        this.title = title;
        this.phieu = phieu;
        this.soCau = soCau;
        this.date = date;
        this.subjectName = subjectName;
    }

    // --- getters & setters ---

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    @Nullable
    public Integer getClassId() {
        return classId;
    }
    public void setClassId(@Nullable Integer classId) {
        this.classId = classId;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getPhieu() {
        return phieu;
    }
    public void setPhieu(String phieu) {
        this.phieu = phieu;
    }

    public int getSoCau() {
        return soCau;
    }
    public void setSoCau(int soCau) {
        this.soCau = soCau;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    @Nullable
    public String getClassName() {
        return className;
    }
    public void setClassName(@Nullable String className) {
        this.className = className;
    }

    public String getSubjectName() {
        return subjectName;
    }
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }
}