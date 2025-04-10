package com.example.javaopencv.repository;

import androidx.lifecycle.LiveData;

import com.example.javaopencv.data.AppDatabase;
import com.example.javaopencv.data.dao.ExamDao;
import com.example.javaopencv.data.entity.Exam;

import java.util.List;

public class ExamRepository {

    private ExamDao examDao;

    public ExamRepository(AppDatabase db) {
        examDao = db.examDao();
    }

    public LiveData<List<Exam>> getAllExams() {
        return examDao.getAllExams();
    }

    public void insertExam(final Exam exam) {
        new Thread(() -> examDao.insertExam(exam)).start();
    }

    public void updateExam(final Exam exam) {
        new Thread(() -> examDao.updateExam(exam)).start();
    }

    public void deleteExam(final Exam exam) {
        new Thread(() -> examDao.deleteExam(exam)).start();
    }
}
