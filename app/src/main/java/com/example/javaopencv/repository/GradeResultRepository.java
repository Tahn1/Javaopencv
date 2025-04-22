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

    public LiveData<List<GradeResult>> getResultsForExam(int examId) {
        return dao.getResultsForExam(examId);
    }

    public void deleteResult(GradeResult result) {
        executor.execute(() -> dao.deleteResult(result));
    }
}
