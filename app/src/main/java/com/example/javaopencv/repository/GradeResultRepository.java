package com.example.javaopencv.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.example.javaopencv.data.AppDatabase;
import com.example.javaopencv.data.dao.GradeResultDao;
import com.example.javaopencv.data.entity.GradeResult;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GradeResultRepository {
    private final GradeResultDao dao;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public GradeResultRepository(Application app) {
        AppDatabase db = AppDatabase.getInstance(app);
        dao = db.gradeResultDao();
    }

    /**
     * Lấy LiveData GradeResult theo ID
     */
    public LiveData<GradeResult> getGradeResultById(long gradeId) {
        return dao.getGradeResultById(gradeId);
    }

    /**
     * LiveData danh sách kết quả chấm của đề thi
     */
    public LiveData<List<GradeResult>> getResultsForExam(int examId) {
        return dao.getResultsForExam(examId);
    }

    /**
     * Đồng bộ danh sách kết quả chấm (dùng xuất file)
     */
    public List<GradeResult> getResultsListSync(int examId) {
        return dao.getResultsListSync(examId);
    }

    /**
     * Chèn mới hoặc thay thế GradeResult
     */
    public void addResult(GradeResult result) {
        executor.execute(() -> dao.insert(result));
    }

    /**
     * Cập nhật toàn bộ GradeResult (đối tượng)
     */
    public void updateResult(GradeResult result) {
        executor.execute(() -> dao.updateResult(result));
    }

    /**
     * Xóa một GradeResult
     */
    public void deleteResult(GradeResult result) {
        executor.execute(() -> dao.deleteResult(result));
    }

    /**
     * Xóa tất cả GradeResult của đề thi
     */
    public void deleteAllByExamId(int examId) {
        executor.execute(() -> dao.deleteAllByExamId(examId));
    }

    /**
     * Cập nhật riêng score và note
     */
    public void updateScoreAndNote(int examId, String sbd, double score, String note) {
        executor.execute(() -> dao.updateScoreAndNote(examId, sbd, score, note));
    }

    /**
     * Đếm số bản ghi cùng examId + sbd, trừ bản ghi đang sửa
     */
    public int countByExamAndStudent(int examId, String sbd, long currentId) {
        return dao.countByExamAndStudent(examId, sbd, currentId);
    }

    /**
     * Export to CSV...
     */
    public void exportResultsToCsv(int examId) {
        executor.execute(() -> {
            List<GradeResult> list = dao.getResultsListSync(examId);
            // TODO: implement export
        });
    }
}
