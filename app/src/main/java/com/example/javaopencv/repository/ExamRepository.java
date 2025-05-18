package com.example.javaopencv.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.javaopencv.data.AppDatabase;
import com.example.javaopencv.data.dao.ExamDao;
import com.example.javaopencv.data.entity.Exam;

import java.util.List;

public class ExamRepository {
    private final ExamDao examDao;
    private final LiveData<List<Exam>> allExams;

    public ExamRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        examDao = db.examDao();
        allExams = examDao.getAllExamsWithClass();
    }

    public LiveData<List<Exam>> getExamsForClass(int classId) {
        return examDao.getExamsForClassWithClassName(classId);
    }

    public LiveData<List<Exam>> getAllExams() {
        return allExams;
    }

    public LiveData<Exam> getExamById(int id) {
        return examDao.getExamById(id);
    }

    public Exam getExamSync(int examId) {
        return examDao.getExamSync(examId);
    }

    public void insertExam(Exam exam) {
        new Thread(() -> examDao.insertExam(exam)).start();
    }

    public void updateExam(Exam exam) {
        new Thread(() -> examDao.updateExam(exam)).start();
    }

    public void deleteExam(Exam exam) {
        new Thread(() -> examDao.deleteExam(exam)).start();
    }
}