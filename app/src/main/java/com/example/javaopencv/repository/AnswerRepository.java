package com.example.javaopencv.repository;

import android.content.Context;

import com.example.javaopencv.data.AppDatabase;
import com.example.javaopencv.data.dao.AnswerDao;
import com.example.javaopencv.data.entity.Answer;

import java.util.List;

public class AnswerRepository {
    private AnswerDao answerDao;
    private static AnswerRepository instance;



    private AnswerRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        answerDao = db.answerDao();
    }

    public void renameCodeSync(int examId, String oldCode, String newCode) {
        answerDao.renameCodeSync(examId, oldCode, newCode);
        android.util.Log.d("AnswerRepository", "renameCodeSync => old="
                + oldCode + ", new=" + newCode);
    }

    public static synchronized AnswerRepository getInstance(Context context) {
        if (instance == null) {
            instance = new AnswerRepository(context.getApplicationContext());
        }
        return instance;
    }

    // ----------------------------------------------------------------
    // 1) Hàm “sync”: Gọi trực tiếp DAO (blocking).
    //    Thường chạy trên background thread (do ViewModel tạo).
    // ----------------------------------------------------------------

    /** Lấy danh sách code distinct cho examId */
    public List<String> getDistinctCodesSync(int examId) {
        return answerDao.getDistinctCodesSync(examId);
    }

    /** Lấy danh sách Answer cho examId, code => trả về List<Answer> */
    public List<Answer> getAnswersByExamAndCodeSync(int examId, String code) {
        return answerDao.getAnswersByExamAndCodeSync(examId, code);
    }

    /** Tìm 1 Answer duy nhất (examId, code, cauSo) */
    public Answer findSingleAnswerSync(int examId, String code, int cauSo) {
        return answerDao.findSingleAnswer(examId, code, cauSo);
    }

    /** Insert 1 Answer (sync) */
    public void insertAnswerSync(Answer answer) {
        answerDao.insertAnswer(answer);
        android.util.Log.d("AnswerRepository",
                "insertAnswerSync => examId=" + answer.examId
                        + ", code=" + answer.code
                        + ", cauSo=" + answer.cauSo
                        + ", dapAn=" + answer.dapAn);
    }

    /** Xóa đáp án theo examId & code (sync) */
    public void deleteAnswersByCodeSync(int examId, String code) {
        answerDao.deleteAnswersByCode(examId, code);
        android.util.Log.d("AnswerRepository",
                "deleteAnswersByCodeSync => examId=" + examId
                        + ", code=" + code);
    }

    /** Partial update qua 4 tham số, nếu trong DAO có updateSingleAnswerSync(...) @Query */
    public void updateSingleAnswerSync(int examId, String code, int cauSo, String dapAn) {
        answerDao.updateSingleAnswerSync(examId, code, cauSo, dapAn);
        android.util.Log.d("AnswerRepository",
                "updateSingleAnswerSync => examId=" + examId
                        + ", code=" + code
                        + ", cauSo=" + cauSo
                        + ", dapAn=" + dapAn);
    }

    /** Update Answer bằng @Update (nếu Dao có @Update on Answer) */
    public void updateAnswerSync(Answer answer) {
        answerDao.updateAnswer(answer);
        android.util.Log.d("AnswerRepository",
                "updateAnswerSync => examId=" + answer.examId
                        + ", code=" + answer.code
                        + ", cauSo=" + answer.cauSo
                        + ", dapAn=" + answer.dapAn);
    }

    // ----------------------------------------------------------------
    // 2) Hàm “async” cũ - vẫn giữ nếu code cũ cần
    // ----------------------------------------------------------------

    /** Insert Answer bằng 1 Thread riêng (async) */
    public void insertAnswer(final Answer answer) {
        new Thread(() -> {
            try {
                answerDao.insertAnswer(answer);
                android.util.Log.d("AnswerRepository",
                        "Inserted (async): examId=" + answer.examId
                                + ", code=" + answer.code
                                + ", cauSo=" + answer.cauSo);
            } catch(Exception e) {
                android.util.Log.e("AnswerRepository", "Insert error", e);
            }
        }).start();
    }

    /** Delete code bằng 1 Thread riêng (async) */
    public void deleteAnswersByCode(final int examId, final String code) {
        new Thread(() -> {
            answerDao.deleteAnswersByCode(examId, code);
            android.util.Log.d("AnswerRepository",
                    "deleteAnswersByCode (async) => examId=" + examId
                            + ", code=" + code);
        }).start();
    }

    // ----------------------------------------------------------------
    // Callback interface nếu code cũ cần
    // ----------------------------------------------------------------
    public interface LoadCallback {
        void onLoad(List<String> codes);
        void onError(Exception e);
    }
}
