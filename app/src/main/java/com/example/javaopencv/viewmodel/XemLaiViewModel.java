package com.example.javaopencv.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.javaopencv.data.AppDatabase;
import com.example.javaopencv.data.dao.GradeResultDao;
import com.example.javaopencv.data.dao.StudentDao;
import com.example.javaopencv.data.entity.GradeResult;
import com.example.javaopencv.data.entity.Student;

import java.util.List;

public class XemLaiViewModel extends AndroidViewModel {
    private final GradeResultDao gradeDao;
    private final StudentDao     studentDao;

    public XemLaiViewModel(@NonNull Application app) {
        super(app);
        AppDatabase db       = AppDatabase.getInstance(app);
        gradeDao             = db.gradeResultDao();
        studentDao           = db.studentDao();
    }

    /** Trả về LiveData danh sách GradeResult của một đề (examId) */
    public LiveData<List<GradeResult>> getResultsForExam(int examId) {
        return gradeDao.getResultsForExam(examId);
    }

    /** Trả về LiveData GradeResult theo id */
    public LiveData<GradeResult> getGradeResultById(long gradeId) {
        return gradeDao.getGradeResultById(gradeId);
    }

    /** Trả về LiveData danh sách Student đã chấm cho examId */
    public LiveData<List<Student>> getStudentsForExam(int examId) {
        return studentDao.getStudentsForExam(examId);
    }

    /** Chèn mới GradeResult vào DB (background thread) */
    public void addResult(GradeResult result) {
        new Thread(() -> gradeDao.insert(result)).start();
    }

    /** Cập nhật GradeResult đã có (background thread) */
    public void updateResult(GradeResult result) {
        new Thread(() -> gradeDao.updateResult(result)).start();
    }

    /** Xóa GradeResult (background thread) */
    public void deleteResult(GradeResult result) {
        new Thread(() -> gradeDao.deleteResult(result)).start();
    }
}
