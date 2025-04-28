package com.example.javaopencv.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.javaopencv.data.AppDatabase;
import com.example.javaopencv.data.dao.ExamDao;
import com.example.javaopencv.data.entity.Exam;
import java.util.List;

public class ExamRepository {
    private final ExamDao examDao;

    public ExamRepository(Application app) {
        examDao = AppDatabase.getInstance(app).examDao();
    }

    /** Lấy tất cả exams kèm tên lớp */
    public LiveData<List<Exam>> getAllExams() {
        return examDao.getAllExamsWithClass();
    }

    /** Lấy exams cho 1 lớp cụ thể kèm tên lớp */
    public LiveData<List<Exam>> getExamsForClass(int classId) {
        return examDao.getExamsForClassWithClassName(classId);
    }

    public void insertExam(Exam exam) {
        new Thread(() -> examDao.insert(exam)).start();
    }

    public void updateExam(Exam exam) {
        new Thread(() -> examDao.updateExam(exam)).start();
    }

    public void deleteExam(Exam exam) {
        new Thread(() -> examDao.deleteExam(exam)).start();
    }

    public LiveData<Exam> getExamById(int id) {
        return examDao.getExamById(id);
    }

}