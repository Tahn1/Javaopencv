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

    /** Lấy exam đồng bộ theo id */
    @Query("SELECT * FROM exams WHERE id = :examId")
    Exam getExamSync(int examId);

    /** Lấy tất cả exams kèm tên lớp (className) */
    @Query(
            "SELECT " +
                    " e.id, " +
                    " e.classId, " +
                    " e.title, " +
                    " e.phieu, " +
                    " e.so_cau AS so_cau, " +
                    " e.date, " +
                    " c.name AS className " +
                    "FROM exams e " +
                    "LEFT JOIN classes c ON e.classId = c.id " +
                    "ORDER BY e.date DESC"
    )
    LiveData<List<Exam>> getAllExamsWithClass();

    /** Lấy exams cho 1 lớp, kèm tên lớp */
    @Query(
            "SELECT " +
                    " e.id, " +
                    " e.classId, " +
                    " e.title, " +
                    " e.phieu, " +
                    " e.so_cau AS so_cau, " +
                    " e.date, " +
                    " c.name AS className " +
                    "FROM exams e " +
                    "LEFT JOIN classes c ON e.classId = c.id " +
                    "WHERE e.classId = :classId " +
                    "ORDER BY e.date DESC"
    )
    LiveData<List<Exam>> getExamsForClassWithClassName(int classId);

    @Insert
    long insert(Exam exam);

    @Insert
    void insertExam(Exam exam);

    @Update
    void updateExam(Exam exam);

    @Delete
    void deleteExam(Exam exam);
}
