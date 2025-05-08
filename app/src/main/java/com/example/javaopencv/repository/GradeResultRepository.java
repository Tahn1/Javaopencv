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

    /** Lấy LiveData danh sách kết quả chấm của đề thi */
    public LiveData<List<GradeResult>> getResultsForExam(int examId) {
        return dao.getResultsForExam(examId);
    }

    /** Lấy danh sách kết quả chấm đồng bộ để xuất file */
    public List<GradeResult> getResultsListSync(int examId) {
        return dao.getResultsListSync(examId);
    }

    /** Thêm hoặc thay thế GradeResult */
    public void addResult(GradeResult result) {
        executor.execute(() -> dao.insert(result));
    }

    /** Cập nhật GradeResult */
    public void updateResult(GradeResult result) {
        executor.execute(() -> dao.updateResult(result));
    }

    /** Xóa một kết quả chấm */
    public void deleteResult(GradeResult result) {
        executor.execute(() -> dao.deleteResult(result));
    }

    /** Xóa tất cả kết quả của một đề thi */
    public void deleteAllByExamId(int examId) {
        executor.execute(() -> dao.deleteAllByExamId(examId));
    }

    /** Xuất kết quả chấm ra file CSV (hoặc định dạng khác) */
    public void exportResultsToCsv(int examId) {
        executor.execute(() -> {
            List<GradeResult> list = dao.getResultsListSync(examId);
            // TODO: xử lý ghi danh sách `list` ra file CSV, PDF, v.v.
        });
    }
}
