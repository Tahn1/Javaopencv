package com.example.javaopencv.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "classes",
        foreignKeys = @ForeignKey(
                entity = Subject.class,
                parentColumns = "id",
                childColumns = "subjectId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index("subjectId")
)
public class SchoolClass {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int subjectId;

    @ColumnInfo(name = "name")
    public String name;

    public SchoolClass(int subjectId, String name) {
        this.subjectId = subjectId;
        this.name = name;
    }
}