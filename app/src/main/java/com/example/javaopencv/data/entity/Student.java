package com.example.javaopencv.data.entity;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "student",
        foreignKeys = @ForeignKey(
                entity = SchoolClass.class,
                parentColumns = "id",
                childColumns = "class_id",
                onDelete = ForeignKey.SET_NULL
        ),
        indices = @Index("class_id")
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

    /**
     * Constructor cho insert mới (Room sẽ bỏ qua nhờ @Ignore)
     */
    @Ignore
    public Student(String name, String studentNumber, @Nullable Integer classId) {
        this.name = name;
        this.studentNumber = studentNumber;
        this.classId = classId;
    }

    /**
     * Constructor Room sẽ sử dụng để khôi phục từ database
     */
    public Student(int id, String name, String studentNumber, @Nullable Integer classId) {
        this.id = id;
        this.name = name;
        this.studentNumber = studentNumber;
        this.classId = classId;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStudentNumber() { return studentNumber; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }

    public Integer getClassId() { return classId; }
    public void setClassId(@Nullable Integer classId) { this.classId = classId; }
}
