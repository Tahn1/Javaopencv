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
     * Chèn mới hoặc cập nhật (REPLACE) nếu đã tồn tại cùng khóa.
     * Trả về id của bản ghi (dạng long).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(GradeResult gradeResult);

    /**
     * Lấy danh sách kết quả chấm của một đề (examId), sắp xếp theo timestamp giảm dần.
     */
    @Query("SELECT * FROM GradeResult WHERE examId = :examId ORDER BY timestamp DESC")
    LiveData<List<GradeResult>> getResultsForExam(int examId);

    /**
     * Lấy một GradeResult theo id để hiển thị chi tiết / sửa.
     */
    @Query("SELECT * FROM GradeResult WHERE id = :gradeId")
    LiveData<GradeResult> getGradeResultById(long gradeId);

    /**
     * Cập nhật các trường của GradeResult (maDe, sbd, answersCsv, score, …).
     */
    @Update
    void updateResult(GradeResult gradeResult);

    /**
     * Xóa một bản ghi GradeResult.
     */
    @Delete
    void deleteResult(GradeResult gradeResult);
}
