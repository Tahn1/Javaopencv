package com.example.javaopencv.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.javaopencv.data.entity.GradeResult;

import java.util.List;

@Dao
public interface GradeResultDao {

    /**
     * Chèn mới hoặc thay thế GradeResult (REPLACE) nếu đã tồn tại.
     * Trả về id của bản ghi.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(GradeResult gradeResult);

    /**
     * Lấy LiveData danh sách GradeResult cho một đề thi (examId).
     */
    @Query("SELECT * FROM GradeResult WHERE examId = :examId ORDER BY timestamp DESC")
    LiveData<List<GradeResult>> getResultsForExam(int examId);

    /**
     * Cập nhật score và note cho học sinh (sbd) trong exam.
     */
    @Query("UPDATE GradeResult " +
            "SET score = :score, note = :note " +
            "WHERE examId = :examId AND sbd = :sbd")
    void updateScoreAndNote(int examId, String sbd, double score, String note);

    /**
     * Lấy LiveData một GradeResult theo id để hiển thị hoặc sửa chi tiết.
     */
    @Query("SELECT * FROM GradeResult WHERE id = :gradeId")
    LiveData<GradeResult> getGradeResultById(long gradeId);

    /**
     * Cập nhật toàn bộ đối tượng GradeResult.
     */
    @Update
    void updateResult(GradeResult gradeResult);

    /**
     * Xóa một GradeResult.
     */
    @Delete
    void deleteResult(GradeResult gradeResult);

    /**
     * Xóa tất cả GradeResult của đề thi (examId).
     */
    @Query("DELETE FROM GradeResult WHERE examId = :examId")
    void deleteAllByExamId(int examId);

    /**
     * Lấy danh sách GradeResult đồng bộ (không LiveData), dùng khi xuất file.
     */
    @Query("SELECT * FROM GradeResult WHERE examId = :examId ORDER BY timestamp DESC")
    List<GradeResult> getResultsListSync(int examId);

    /**
     * Đếm số bản ghi cùng examId và cùng sbd, loại trừ bản ghi đang sửa (currentId).
     * Dùng để kiểm tra trùng mã sinh viên trước khi cập nhật.
     */
    @Query("SELECT COUNT(*) FROM GradeResult " +
            "WHERE examId = :examId AND sbd = :sbd AND id != :currentId")
    int countByExamAndStudent(int examId, String sbd, long currentId);
}
