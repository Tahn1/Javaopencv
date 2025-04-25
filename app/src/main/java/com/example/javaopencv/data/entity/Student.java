package com.example.javaopencv.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "students",
        foreignKeys = @ForeignKey(
                entity = SchoolClass.class,
                parentColumns = "id",
                childColumns = "classId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index("classId")
)
public class Student {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int classId;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "student_number")
    public String studentNumber;

    public Student(int classId, String name, String studentNumber) {
        this.classId = classId;
        this.name = name;
        this.studentNumber = studentNumber;
    }
}
