package com.example.javaopencv.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.javaopencv.data.AppDatabase;
import com.example.javaopencv.data.dao.SubjectDao;
import com.example.javaopencv.data.entity.Subject;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SubjectRepository {
    private final SubjectDao subjectDao;
    private final LiveData<List<Subject>> allSubjects;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public SubjectRepository(Application app) {
        AppDatabase db = AppDatabase.getInstance(app);
        subjectDao = db.subjectDao();
        allSubjects = subjectDao.getAllSubjects();
    }

    public LiveData<List<Subject>> getAllSubjects() {
        return allSubjects;
    }

    public void insert(Subject subject) {
        executor.execute(() -> subjectDao.insert(subject));
    }

    public void update(Subject subject) {
        executor.execute(() -> subjectDao.update(subject));
    }

    public void delete(Subject subject) {
        executor.execute(() -> subjectDao.delete(subject));
    }
}
