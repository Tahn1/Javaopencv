package com.example.javaopencv.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.javaopencv.data.entity.ExamCodeEntry;
import java.util.List;

@Dao
public interface ExamCodeEntryDao {

    @Insert
    void insertExamCodeEntry(ExamCodeEntry entry);

    @Update
    void updateExamCodeEntry(ExamCodeEntry entry);

    @Delete
    void deleteExamCodeEntry(ExamCodeEntry entry);

    // Đổi tên phương thức thành getExamCodeEntryByExamId
    @Query("SELECT * FROM ExamCodeEntry WHERE examId = :examId ORDER BY code")
    LiveData<List<ExamCodeEntry>> getExamCodeEntryByExamId(int examId);
}
