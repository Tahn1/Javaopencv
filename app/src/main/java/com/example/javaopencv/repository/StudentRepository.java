package com.example.javaopencv.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.javaopencv.data.AppDatabase;
import com.example.javaopencv.data.dao.StudentDao;
import com.example.javaopencv.data.entity.Student;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class StudentRepository {
    private final StudentDao studentDao;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public StudentRepository(Application app) {
        AppDatabase db = AppDatabase.getInstance(app);
        studentDao = db.studentDao();
    }

    public LiveData<List<Student>> getAllStudents() {
        return studentDao.getAllStudents();
    }

    public LiveData<List<Student>> getStudentsForClass(int classId) {
        return studentDao.getStudentsForClass(classId);
    }

    public void insert(Student student) {
        executor.execute(() -> studentDao.insert(student));
    }

    public void update(Student student) {
        executor.execute(() -> studentDao.update(student));
    }

    public void delete(Student student) {
        executor.execute(() -> studentDao.delete(student));
    }
}
