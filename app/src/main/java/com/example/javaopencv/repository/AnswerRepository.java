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

    public static synchronized AnswerRepository getInstance(Context context) {
        if (instance == null) {
            instance = new AnswerRepository(context.getApplicationContext());
        }
        return instance;
    }

    public void insertAnswer(final Answer answer) {
        new Thread(() -> {
            try {
                answerDao.insertAnswer(answer);
                android.util.Log.d("AnswerRepository", "Inserted: examId=" + answer.examId
                        + ", code=" + answer.code + ", cauSo=" + answer.cauSo);
            } catch(Exception e) {
                android.util.Log.e("AnswerRepository", "Insert error", e);
            }
        }).start();
    }

    public void deleteAnswersByCode(final int examId, final String code) {
        new Thread(() -> {
            try {
                answerDao.deleteAnswersByCode(examId, code);
                android.util.Log.d("AnswerRepository", "Deleted answers: examId=" + examId + ", code=" + code);
            } catch(Exception e) {
                android.util.Log.e("AnswerRepository", "Delete error", e);
            }
        }).start();
    }

    // Phương thức load danh sách mã đề (distinct codes) từ bảng Answer
    // Sử dụng callback để trả về kết quả bất đồng bộ.
    public void getDistinctCodes(final int examId, final LoadCallback callback) {
        new Thread(() -> {
            try {
                List<String> codes = answerDao.getDistinctCodes(examId);
                android.util.Log.d("AnswerRepository", "Loaded distinct codes for examId=" + examId);
                if (callback != null) {
                    callback.onLoad(codes);
                }
            } catch (Exception e) {
                android.util.Log.e("AnswerRepository", "Load error", e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        }).start();
    }

    // Interface callback để trả về danh sách codes hoặc lỗi
    public interface LoadCallback {
        void onLoad(List<String> codes);
        void onError(Exception e);
    }
}
