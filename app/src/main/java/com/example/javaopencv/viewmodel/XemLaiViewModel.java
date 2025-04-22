package com.example.javaopencv.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.javaopencv.data.AppDatabase;
import com.example.javaopencv.data.dao.GradeResultDao;
import com.example.javaopencv.data.entity.GradeResult;

import java.util.List;

public class XemLaiViewModel extends AndroidViewModel {
    private final GradeResultDao dao;

    public XemLaiViewModel(@NonNull Application app) {
        super(app);
        dao = AppDatabase.getInstance(app).gradeResultDao();
    }

    /** Trả về LiveData danh sách GradeResult của một đề (examId) */
    public LiveData<List<GradeResult>> getResultsForExam(int examId) {
        return dao.getResultsForExam(examId);
    }

    /** Trả về LiveData để theo dõi một GradeResult theo id */
    public LiveData<GradeResult> getGradeResultById(long gradeId) {
        return dao.getGradeResultById(gradeId);
    }

    /** Chèn mới GradeResult vào DB (chạy background thread) */
    public void addResult(GradeResult result) {
        new Thread(() -> dao.insert(result)).start();
    }

    /** Cập nhật GradeResult đã có (chạy background thread) */
    public void updateResult(GradeResult result) {
        new Thread(() -> dao.updateResult(result)).start();
    }

    /** Xóa GradeResult (chạy background thread) */
    public void deleteResult(GradeResult result) {
        new Thread(() -> dao.deleteResult(result)).start();
    }
}
