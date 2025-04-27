package com.example.javaopencv.data.entity;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

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

    /** Constructor chính Room sẽ dùng (7 tham số) */
    public Exam(int id,
                @Nullable Integer classId,
                String title,
                String phieu,
                int soCau,
                String date,
                @Nullable String className) {
        this.id        = id;
        this.classId   = classId;
        this.title     = title;
        this.phieu     = phieu;
        this.soCau     = soCau;
        this.date      = date;
        this.className = className;
    }

    /** Constructor để insert mới (5 tham số) */
    @Ignore
    public Exam(@Nullable Integer classId,
                String title,
                String phieu,
                int soCau,
                String date) {
        this.classId = classId;
        this.title   = title;
        this.phieu   = phieu;
        this.soCau   = soCau;
        this.date    = date;
    }

    /** Constructor để update/chỉnh sửa tên (6 tham số) */
    @Ignore
    public Exam(int id,
                @Nullable Integer classId,
                String title,
                String phieu,
                int soCau,
                String date) {
        this.id       = id;
        this.classId  = classId;
        this.title    = title;
        this.phieu    = phieu;
        this.soCau    = soCau;
        this.date     = date;
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
}
