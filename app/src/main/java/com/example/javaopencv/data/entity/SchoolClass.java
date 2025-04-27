package com.example.javaopencv.data.entity;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity(
        tableName = "classes",
        foreignKeys = @ForeignKey(
                entity = Subject.class,
                parentColumns = "id",
                childColumns = "subjectId",
                onDelete = ForeignKey.SET_NULL
        ),
        indices = @Index("subjectId")
)
public class SchoolClass {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @Nullable
    @ColumnInfo(name = "subjectId")
    public Integer subjectId;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "dateCreated")
    public String dateCreated;

    /**
     * Constructor cho insert mới: gán dateCreated mặc định hôm nay.
     */
    @Ignore
    public SchoolClass(@Nullable Integer subjectId, String name) {
        this.subjectId = subjectId;
        this.name = name;
        // Đổi format thành ngày/tháng/năm bằng số
        this.dateCreated = new java.text.SimpleDateFormat(
                "d/M/yyyy",
                java.util.Locale.getDefault()
        ).format(new java.util.Date());
    }

    /**
     * Constructor Room khôi phục từ DB.
     */
    public SchoolClass(int id,
                       @Nullable Integer subjectId,
                       String name,
                       String dateCreated) {
        this.id = id;
        this.subjectId = subjectId;
        this.name = name;
        this.dateCreated = dateCreated;
    }

    // Getters & Setters...
    public int getId() { return id; }
    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(@Nullable Integer subjectId) { this.subjectId = subjectId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDateCreated() { return dateCreated; }
    public void setDateCreated(String dateCreated) { this.dateCreated = dateCreated; }
}