package com.example.javaopencv.data.entity;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "exams",
        indices = {@Index("classId")}  // Chỉ giữ index, bỏ FK constraint
)
public class Exam {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @Nullable
    @ColumnInfo(name = "classId")
    private Integer classId;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "phieu")
    public String phieu;

    @ColumnInfo(name = "so_cau")
    public int soCau;

    @ColumnInfo(name = "date")
    public String date;

    /** Constructor khi tạo mới */
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

    /** Constructor Room khôi phục từ DB */
    @Ignore
    public Exam(int id,
                @Nullable Integer classId,
                String title,
                String phieu,
                int soCau,
                String date) {
        this.id      = id;
        this.classId = classId;
        this.title   = title;
        this.phieu   = phieu;
        this.soCau   = soCau;
        this.date    = date;
    }

    // Setter & Getter cho classId
    public void setClassId(@Nullable Integer classId) {
        this.classId = classId;
    }

    @Nullable
    public Integer getClassId() {
        return classId;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getPhieu() {
        return phieu;
    }

    public int getSoCau() {
        return soCau;
    }

    public String getDate() {
        return date;
    }
}
