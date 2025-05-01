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

    /** Lấy tất cả lớp, sắp xếp theo dateCreated DESC */
    @Query("SELECT * FROM classes ORDER BY dateCreated DESC")
    LiveData<List<SchoolClass>> getAllClasses();

    /** Lấy thông tin một lớp theo ID */
    @Query("SELECT * FROM classes WHERE id = :id")
    LiveData<SchoolClass> getClassById(int id);

    /**
     * Lấy danh sách lớp kèm số học sinh trong mỗi lớp.
     * Đảm bảo alias cột để map vào @Embedded(prefix="klass_"):
     *   klass_id, klass_name, klass_dateCreated
     */
    @Query(
            "SELECT " +
                    " c.id           AS klass_id, " +
                    " c.name         AS klass_name, " +
                    " c.dateCreated  AS klass_dateCreated, " +
                    " COUNT(s.id)    AS studentCount " +
                    "FROM classes c " +
                    "LEFT JOIN student s ON s.class_id = c.id " +
                    "GROUP BY c.id " +
                    "ORDER BY c.dateCreated DESC"
    )
    LiveData<List<ClassWithCount>> getClassesWithCount();
}
