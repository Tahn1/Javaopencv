package com.example.javaopencv.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

public class ClassWithCount {
    @Embedded
    public SchoolClass klass;

    @ColumnInfo(name = "studentCount")
    public int studentCount;
}