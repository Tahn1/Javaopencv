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

    /** Trả về LiveData danh sách GradeResult chỉ của examId này */
    public LiveData<List<GradeResult>> getResultsForExam(int examId) {
        return dao.getResultsForExam(examId);
    }
}
