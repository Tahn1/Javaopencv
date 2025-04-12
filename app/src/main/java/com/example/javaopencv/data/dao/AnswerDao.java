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

    /**
     * Partial update cột dapAn cho 1 row (examId, code, cauSo).
     * Khi ViewModel muốn sửa đáp án của 1 câu,
     * có thể gọi updateSingleAnswerSync(...) với examId, code, cauSo, dapAn.
     */
    @Query("UPDATE answers SET dapAn = :dapAn " +
            "WHERE examId = :examId AND code = :code AND cauSo = :cauSo")
    void updateSingleAnswerSync(int examId, String code, int cauSo, String dapAn);

    /**
     * Lấy danh sách mã đề distinct (mỗi examId có thể có nhiều code).
     */
    @Query("SELECT DISTINCT code FROM answers WHERE examId = :examId")
    List<String> getDistinctCodesSync(int examId);

    /**
     * Lấy toàn bộ Answer cho examId & code, sắp xếp theo cauSo.
     * Thường dùng để build danh sách đáp án (cauSo => dapAn).
     */
    @Query("SELECT * FROM answers WHERE examId = :examId AND code = :code ORDER BY cauSo")
    List<Answer> getAnswersByExamAndCodeSync(int examId, String code);

    /**
     * Insert thêm 1 row Answer (examId, code, cauSo, dapAn).
     * Dùng cho trường hợp chèn mới 1 câu chưa có (hoặc khi tạo mã đề).
     */
    @Insert
    void insertAnswer(Answer answer);

    /**
     * Update nhiều cột (theo @Update) nếu bạn có sẵn đối tượng Answer (có id).
     * Ít khi dùng nếu chỉ muốn update cột dapAn,
     * vì partial update @Query phía trên hiệu quả hơn.
     */
    @Update
    void updateAnswer(Answer answer);

    /**
     * Xóa toàn bộ đáp án của 1 mã đề (code) trong 1 bài thi (examId).
     * Dùng khi muốn xóa cũ chèn mới, hoặc xóa mã đề hoàn toàn.
     */
    @Query("DELETE FROM answers WHERE examId = :examId AND code = :code")
    void deleteAnswersByCode(int examId, String code);

    /**
     * Đếm số mã đề distinct của 1 bài thi (examId).
     */
    @Query("SELECT COUNT(DISTINCT code) FROM answers WHERE examId = :examId")
    int countDistinctCode(int examId);

    /**
     * Tìm row cụ thể (examId, code, cauSo). Giúp bạn kiểm tra row cũ
     * trước khi quyết định update hay insert (nếu row chưa tồn tại).
     */
    @Query("SELECT * FROM answers WHERE examId = :examId AND code = :code AND cauSo = :cauSo LIMIT 1")
    Answer findSingleAnswer(int examId, String code, int cauSo);

}
