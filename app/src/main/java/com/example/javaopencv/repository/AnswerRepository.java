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
        new Thread(() -> answerDao.insertAnswer(answer)).start();
    }

    public void deleteAnswersByCode(final int examId, final String code) {
        new Thread(() -> answerDao.deleteAnswersByCode(examId, code)).start();
    }

    public void getDistinctCodes(final int examId, final LoadCallback callback) {
        new Thread(() -> {
            try {
                List<String> codes = answerDao.getDistinctCodes(examId);
                callback.onLoad(codes);
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    public interface LoadCallback {
        void onLoad(List<String> codes);
        void onError(Exception e);
    }
}
