package com.example.javaopencv.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.example.javaopencv.data.AppDatabase;
import com.example.javaopencv.data.dao.GradeResultDao;
import com.example.javaopencv.data.entity.Answer;
import com.example.javaopencv.data.entity.GradeResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GradeResultRepository {
    private final GradeResultDao dao;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public GradeResultRepository(Application app) {
        AppDatabase db = AppDatabase.getInstance(app);
        dao = db.gradeResultDao();
    }

    public LiveData<GradeResult> getGradeResultById(long gradeId) {
        return dao.getGradeResultById(gradeId);
    }


    public LiveData<List<GradeResult>> getResultsForExam(int examId) {
        return dao.getResultsForExam(examId);
    }


    public List<GradeResult> getResultsListSync(int examId) {
        return dao.getResultsListSync(examId);
    }

    public void addResult(GradeResult result) {
        executor.execute(() -> dao.insert(result));
    }

    public void updateResult(GradeResult result) {
        executor.execute(() -> dao.updateResult(result));
    }


    public void deleteResult(GradeResult result) {
        executor.execute(() -> dao.deleteResult(result));
    }

    public void deleteAllByExamId(int examId) {
        executor.execute(() -> dao.deleteAllByExamId(examId));
    }


    public void updateScoreAndNote(int examId, String sbd, double score, String note) {
        executor.execute(() -> dao.updateScoreAndNote(examId, sbd, score, note));
    }


    public int countByExamAndStudent(int examId, String sbd, long currentId) {
        return dao.countByExamAndStudent(examId, sbd, currentId);
    }

    public void regradeResults(int examId, List<Answer> newKey) {
        executor.execute(() -> {
            // 1) Load tất cả GradeResult
            List<GradeResult> all = dao.getResultsListSync(examId);

            // 2) Build map của key mới: câu số → đáp án
            Map<Integer, String> keyMap = new HashMap<>();
            for (Answer a : newKey) {
                keyMap.put(a.cauSo, a.dapAn);
            }
            // Tổng số câu thực sự của bộ key mới
            int totalQ = newKey.size();

            // 3) Re-calc và update nếu khác điểm cũ
            for (GradeResult r : all) {
                String[] picks = r.getAnswersCsv().split(",");
                int cnt = 0;
                for (int i = 0; i < picks.length; i++) {
                    if (picks[i].equals(keyMap.get(i + 1))) cnt++;
                }
                double newScore = cnt * 10.0 / totalQ;
                if (Double.compare(newScore, r.score) != 0
                        || r.totalQuestions != totalQ) {
                    r.correctCount = cnt;
                    r.totalQuestions = totalQ;
                    r.score = newScore;
                    dao.updateResult(r);
                }
            }
        });
    }
}