package com.example.javaopencv.data.entity;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

@Entity(
        tableName = "student",
        foreignKeys = @ForeignKey(
                entity = SchoolClass.class,
                parentColumns = "id",
                childColumns = "class_id",
                onDelete = ForeignKey.SET_NULL
        ),
        indices = {
                // index thường cho class_id
                @Index("class_id"),
                // enforce unique student_number **trong mỗi class_id**
                @Index(value = {"class_id", "student_number"}, unique = true)
        }
)
public class Student {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "student_number")
    private String studentNumber;

    @Nullable
    @ColumnInfo(name = "class_id")
    private Integer classId;

    @ColumnInfo(name = "date_created")
    private String dateCreated;

    @Ignore
    public Student(String name,
                   String studentNumber,
                   @Nullable Integer classId,
                   String dateCreated) {
        this.name = name;
        this.studentNumber = studentNumber;
        this.classId = classId;
        this.dateCreated = dateCreated;
    }

    public Student(int id,
                   String name,
                   String studentNumber,
                   @Nullable Integer classId,
                   String dateCreated) {
        this.id = id;
        this.name = name;
        this.studentNumber = studentNumber;
        this.classId = classId;
        this.dateCreated = dateCreated;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStudentNumber() { return studentNumber; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }

    public Integer getClassId() { return classId; }
    public void setClassId(@Nullable Integer classId) { this.classId = classId; }

    public String getDateCreated() { return dateCreated; }
    public void setDateCreated(String dateCreated) { this.dateCreated = dateCreated; }
}
