package com.example.javaopencv.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.javaopencv.data.entity.Answer;
import java.util.List;

@Dao
public interface AnswerDao {

    @Query("SELECT DISTINCT code FROM answers WHERE examId = :examId")
    List<String> getDistinctCodes(int examId);

    @Query("SELECT * FROM answers WHERE examId = :examId ORDER BY code, cauSo")
    List<Answer> getAnswersByExam(int examId);

    @Query("SELECT * FROM answers WHERE examId = :examId AND code = :code ORDER BY cauSo")
    List<Answer> getAnswersByExamAndCode(int examId, String code);
    @Insert
    void insertAnswer(Answer answer);

    @Update
    void updateAnswer(Answer answer);

    @Query("DELETE FROM answers WHERE examId = :examId AND code = :code")
    void deleteAnswersByCode(int examId, String code);

    @Query("SELECT COUNT(DISTINCT code) FROM answers WHERE examId = :examId")
    int countDistinctCode(int examId);
}
