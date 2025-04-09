package com.example.javaopencv.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.javaopencv.data.entity.ExamStats;

@Dao
public interface ExamStatsDao {
    @Query("SELECT * FROM exam_stats WHERE examId = :examId")
    ExamStats getExamStats(int examId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertExamStats(ExamStats stats);

    @Update
    void updateExamStats(ExamStats stats);
}
