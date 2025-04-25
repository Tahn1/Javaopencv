package com.example.javaopencv.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.javaopencv.data.entity.SchoolClass;

import java.util.List;

@Dao
public interface ClassDao {
    @Query("SELECT * FROM classes ORDER BY name")
    LiveData<List<SchoolClass>> getAllClasses();

    @Query("SELECT * FROM classes WHERE subjectId = :subjectId ORDER BY name")
    LiveData<List<SchoolClass>> getClassesForSubject(int subjectId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SchoolClass schoolClass);

    @Update
    void update(SchoolClass schoolClass);

    @Delete
    void delete(SchoolClass schoolClass);
}