package com.example.javaopencv.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.javaopencv.data.entity.ClassWithCount;
import com.example.javaopencv.data.entity.SchoolClass;

import java.util.List;

@Dao
public interface ClassDao {

    @Insert
    long insert(SchoolClass schoolClass);

    @Update
    int update(SchoolClass schoolClass);

    @Delete
    int delete(SchoolClass schoolClass);

    /** Lấy danh sách lớp nguyên bản (dành cho spinner hoặc cần SchoolClass) */
    @Query("SELECT * FROM classes WHERE subjectId = :subjectId")
    LiveData<List<SchoolClass>> getClassesForSubject(int subjectId);

    /** Lấy tất cả lớp, sắp xếp theo dateCreated DESC */
    @Query("SELECT * FROM classes ORDER BY dateCreated DESC")
    LiveData<List<SchoolClass>> getAll();

    /** Lấy thông tin một lớp theo ID */
    @Query("SELECT * FROM classes WHERE id = :id")
    LiveData<SchoolClass> getClassById(int id);

    /** Lấy lớp kèm số học sinh */
    @Query("SELECT c.id AS id, c.subjectId AS subjectId, c.name AS name, c.dateCreated AS dateCreated, COUNT(s.id) AS studentCount " +
            "FROM classes c LEFT JOIN student s ON s.class_id = c.id " +
            "WHERE (:subjectId = 0 OR c.subjectId = :subjectId) " +
            "GROUP BY c.id " +
            "ORDER BY c.dateCreated DESC")
    LiveData<List<ClassWithCount>> getClassesWithCount(int subjectId);
}
