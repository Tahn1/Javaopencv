package com.example.javaopencv.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.javaopencv.data.AppDatabase;
import com.example.javaopencv.data.dao.ClassDao;
import com.example.javaopencv.data.entity.SchoolClass;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ClassRepository {
    private final ClassDao classDao;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public ClassRepository(Application app) {
        AppDatabase db = AppDatabase.getInstance(app);
        classDao = db.classDao();
    }

    public LiveData<List<SchoolClass>> getAllClasses() {
        return classDao.getAllClasses();
    }

    public LiveData<List<SchoolClass>> getClassesForSubject(int subjectId) {
        return classDao.getClassesForSubject(subjectId);
    }

    public void insert(SchoolClass schoolClass) {
        executor.execute(() -> classDao.insert(schoolClass));
    }

    public void update(SchoolClass schoolClass) {
        executor.execute(() -> classDao.update(schoolClass));
    }

    public void delete(SchoolClass schoolClass) {
        executor.execute(() -> classDao.delete(schoolClass));
    }
}