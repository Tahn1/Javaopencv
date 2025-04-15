package com.example.javaopencv.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.javaopencv.data.entity.Answer;

import java.util.List;

@Dao
public interface AnswerDao {

    @Query("UPDATE answers SET code=:newCode WHERE examId=:examId AND code=:oldCode")
    void renameCodeSync(int examId, String oldCode, String newCode);

    @Query("UPDATE answers SET dapAn = :dapAn " +
            "WHERE examId = :examId AND code = :code AND cauSo = :cauSo")
    void updateSingleAnswerSync(int examId, String code, int cauSo, String dapAn);

    @Query("SELECT DISTINCT code FROM answers WHERE examId = :examId")
    List<String> getDistinctCodesSync(int examId);

    @Query("SELECT * FROM answers WHERE examId = :examId AND code = :code ORDER BY cauSo")
    List<Answer> getAnswersByExamAndCodeSync(int examId, String code); // <-- tên mới đồng nhất với OmrGrader

    @Insert
    void insertAnswer(Answer answer);

    @Update
    void updateAnswer(Answer answer);

    @Query("DELETE FROM answers WHERE examId = :examId AND code = :code")
    void deleteAnswersByCode(int examId, String code);

    @Query("SELECT COUNT(DISTINCT code) FROM answers WHERE examId = :examId")
    int countDistinctCode(int examId);

    @Query("SELECT * FROM answers WHERE examId = :examId AND code = :code AND cauSo = :cauSo LIMIT 1")
    Answer findSingleAnswer(int examId, String code, int cauSo);
}
