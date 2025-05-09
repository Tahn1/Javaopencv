package com.example.javaopencv.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RewriteQueriesToDropUnusedColumns;
import androidx.room.Update;

import com.example.javaopencv.data.entity.Exam;

import java.util.List;

@Dao
@RewriteQueriesToDropUnusedColumns
public interface ExamDao {

    @SuppressWarnings("unused")

    @Query("SELECT * FROM exams WHERE id = :id")
    LiveData<Exam> getExamById(int id);


    @Query("SELECT * FROM exams WHERE id = :examId")
    Exam getExamSync(int examId);


    @Query(
            "SELECT " +
                    " e.id, " +
                    " e.classId, " +
                    " e.title, " +
                    " e.phieu, " +
                    " e.so_cau AS so_cau, " +
                    " e.date, " +
                    " e.subject_name AS subject_name, " +
                    " c.name AS className " +
                    "FROM exams e " +
                    "LEFT JOIN classes c ON e.classId = c.id " +
                    "ORDER BY e.date DESC"
    )
    LiveData<List<Exam>> getAllExamsWithClass();

    /**
     * Lấy Exam của một lớp, kèm tên lớp và tên môn học
     */
    @Query(
            "SELECT " +
                    " e.id, " +
                    " e.classId, " +
                    " e.title, " +
                    " e.phieu, " +
                    " e.so_cau AS so_cau, " +
                    " e.date, " +
                    " e.subject_name AS subject_name, " +
                    " c.name AS className " +
                    "FROM exams e " +
                    "LEFT JOIN classes c ON e.classId = c.id " +
                    "WHERE e.classId = :classId " +
                    "ORDER BY e.date DESC"
    )
    LiveData<List<Exam>> getExamsForClassWithClassName(int classId);


    @Insert
    void insertExam(Exam exam);


    @Update
    void updateExam(Exam exam);


    @Delete
    void deleteExam(Exam exam);
}
