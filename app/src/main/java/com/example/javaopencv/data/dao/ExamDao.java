package com.example.javaopencv.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.javaopencv.data.entity.Exam;

import java.util.List;

@Dao
public interface ExamDao {



    @Query("SELECT * FROM exams WHERE id = :examId")
    Exam getExamSync(int examId);
    @Query("SELECT * FROM exams ORDER BY id DESC")
    LiveData<List<Exam>> getAllExams();

    @Insert
    void insertExam(Exam exam);

    @Update
    void updateExam(Exam exam);

    @Delete
    void deleteExam(Exam exam);
}
