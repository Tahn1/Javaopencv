package com.example.javaopencv.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

public class ClassWithCount {
    @Embedded(prefix = "klass_")
    public SchoolClass klass;

    @ColumnInfo(name = "studentCount")
    public int studentCount;

    // Getter
    public SchoolClass getKlass() {
        return klass;
    }
    public int getStudentCount() {
        return studentCount;
    }
}
