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

    @Query("SELECT * FROM student WHERE student_number = :studentNumber LIMIT 1")
    Student getStudentByNumber(String studentNumber);

    @Insert long insert(Student student);
    @Update void update(Student student);
    @Delete void delete(Student student);

    // ★ join với GradeResult (bảng mặc định tên GradeResult)
    @Query(
            "SELECT s.* " +
                    "  FROM student AS s " +
                    "  JOIN GradeResult AS g " +
                    "    ON s.student_number = g.sbd " +
                    " WHERE g.examId = :examId " +
                    " GROUP BY s.student_number"
    )
    LiveData<List<Student>> getStudentsForExam(int examId);
}
