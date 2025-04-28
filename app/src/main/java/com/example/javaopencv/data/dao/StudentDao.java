package com.example.javaopencv.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.javaopencv.data.entity.Student;

import java.util.List;

@Dao
public interface StudentDao {
    @Query("SELECT * FROM student WHERE class_id = :classId ORDER BY name")
    LiveData<List<Student>> getStudentsForClass(int classId);

    @Insert
    long insert(Student student);

    @Update
    void update(Student student);

    @Delete
    void delete(Student student);
}
