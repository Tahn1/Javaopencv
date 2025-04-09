package com.example.javaopencv.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import com.example.javaopencv.data.entity.Exam;
import java.util.List;

@Dao
public interface ExamDao {
    @Query("SELECT * FROM exams ORDER BY id DESC")
    LiveData<List<Exam>> getAllExams();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertExam(Exam exam);

    @Update
    void updateExam(Exam exam);

    @Delete
    void deleteExam(Exam exam);
}
