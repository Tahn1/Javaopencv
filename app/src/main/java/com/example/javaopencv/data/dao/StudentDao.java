package com.example.javaopencv.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.javaopencv.data.entity.Student;

import java.util.List;

@Dao
public interface StudentDao {

    /** Lấy danh sách học sinh của 1 lớp, sắp theo tên */
    @Query("SELECT * FROM student WHERE class_id = :classId ORDER BY name")
    LiveData<List<Student>> getStudentsForClass(int classId);

    /** Lấy 1 học sinh theo lớp & số báo danh */
    @Query("SELECT * FROM student WHERE class_id = :classId AND student_number = :studentNumber LIMIT 1")
    Student getStudentByNumber(int classId, String studentNumber);

    /** Đếm xem trong lớp đã có số báo danh đó chưa */
    @Query("SELECT COUNT(*) FROM student WHERE class_id = :classId AND student_number = :studentNumber")
    int countByClassAndNumber(int classId, String studentNumber);

    /** Insert IGNORE để khi trùng (classId, studentNumber) thì trả về -1, không crash */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Student student);

    /** Cập nhật */
    @Update
    void update(Student student);

    /** Xóa */
    @Delete
    void delete(Student student);

    /**
     * Lấy danh sách học sinh đã chấm thi cho một đề thi,
     * chỉ bao gồm những học sinh cùng lớp với đề thi đó.
     */
    @Query(
            "SELECT s.* " +
                    "  FROM student AS s " +
                    "  JOIN GradeResult AS g ON s.student_number = g.sbd " +
                    " WHERE g.examId = :examId " +
                    "   AND s.class_id = :classId " +
                    " GROUP BY s.student_number"
    )
    LiveData<List<Student>> getStudentsForExam(int examId, int classId);
}
