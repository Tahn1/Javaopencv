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


    public List<String> getDistinctCodesSync(int examId) {
        return answerDao.getDistinctCodesSync(examId);
    }

    public List<Answer> getAnswersByExamAndCodeSync(int examId, String code) {
        return answerDao.getAnswersByExamAndCodeSync(examId, code);
    }

    public Answer findSingleAnswerSync(int examId, String code, int cauSo) {
        return answerDao.findSingleAnswer(examId, code, cauSo);
    }

    public void insertAnswerSync(Answer answer) {
        answerDao.insertAnswer(answer);
        android.util.Log.d("AnswerRepository",
                "insertAnswerSync => examId=" + answer.examId
                        + ", code=" + answer.code
                        + ", cauSo=" + answer.cauSo
                        + ", dapAn=" + answer.dapAn);
    }


    public void deleteAnswersByCodeSync(int examId, String code) {
        answerDao.deleteAnswersByCode(examId, code);
        android.util.Log.d("AnswerRepository",
                "deleteAnswersByCodeSync => examId=" + examId
                        + ", code=" + code);
    }

    public void updateSingleAnswerSync(int examId, String code, int cauSo, String dapAn) {
        answerDao.updateSingleAnswerSync(examId, code, cauSo, dapAn);
        android.util.Log.d("AnswerRepository",
                "updateSingleAnswerSync => examId=" + examId
                        + ", code=" + code
                        + ", cauSo=" + cauSo
                        + ", dapAn=" + dapAn);
    }

    public void updateAnswerSync(Answer answer) {
        answerDao.updateAnswer(answer);
        android.util.Log.d("AnswerRepository",
                "updateAnswerSync => examId=" + answer.examId
                        + ", code=" + answer.code
                        + ", cauSo=" + answer.cauSo
                        + ", dapAn=" + answer.dapAn);
    }


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

    public void deleteAnswersByCode(final int examId, final String code) {
        new Thread(() -> {
            answerDao.deleteAnswersByCode(examId, code);
            android.util.Log.d("AnswerRepository",
                    "deleteAnswersByCode (async) => examId=" + examId
                            + ", code=" + code);
        }).start();
    }
    public interface LoadCallback {
        void onLoad(List<String> codes);
        void onError(Exception e);
    }
}
