package com.example.javaopencv.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.javaopencv.data.entity.GradeResult;

import java.util.List;

@Dao
public interface GradeResultDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(GradeResult gradeResult);

    @Query("SELECT * FROM GradeResult WHERE examId = :examId ORDER BY timestamp DESC")
    LiveData<List<GradeResult>> getResultsForExam(int examId);

    @Query("SELECT * FROM GradeResult ORDER BY timestamp DESC")
    LiveData<List<GradeResult>> getAllGradeResults();
    @Query("SELECT * FROM GradeResult WHERE id = :gradeId")
    LiveData<GradeResult> getById(long gradeId);
    @Query("SELECT * FROM GradeResult WHERE id = :id")
    LiveData<GradeResult> getGradeResultById(long id);


}
