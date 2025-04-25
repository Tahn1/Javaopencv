package com.example.javaopencv.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "exams",
        foreignKeys = @ForeignKey(
                entity = SchoolClass.class,
                parentColumns = "id",
                childColumns = "classId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("classId")}
)
public class Exam {
    @PrimaryKey(autoGenerate = true)
    public int id;

    // Khóa ngoại liên kết đến lớp học
    public int classId;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "phieu")
    public String phieu;

    @ColumnInfo(name = "so_cau")
    public int soCau;

    @ColumnInfo(name = "date")
    public String date;

    /**
     * Constructor dùng để tạo Exam mới (Room sẽ tự gán id).
     */
    public Exam(int classId, String title, String phieu, int soCau, String date) {
        this.classId = classId;
        this.title = title;
        this.phieu = phieu;
        this.soCau = soCau;
        this.date = date;
    }

    /**
     * Constructor đầy đủ, dùng khi Room khôi phục đối tượng từ CSDL.
     */
    @Ignore
    public Exam(int id, int classId, String title, String phieu, int soCau, String date) {
        this.id = id;
        this.classId = classId;
        this.title = title;
        this.phieu = phieu;
        this.soCau = soCau;
        this.date = date;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getClassId() {
        return classId;
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
