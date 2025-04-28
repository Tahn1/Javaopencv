package com.example.javaopencv.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.example.javaopencv.data.AppDatabase;
import com.example.javaopencv.data.dao.ClassDao;
import com.example.javaopencv.data.entity.ClassWithCount;
import com.example.javaopencv.data.entity.SchoolClass;

import java.util.List;

public class ClassRepository {
    private final ClassDao classDao;

    public ClassRepository(Application app) {
        classDao = AppDatabase.getInstance(app).classDao();
    }

    /** Lấy tất cả lớp, không kèm số học sinh */
    public LiveData<List<SchoolClass>> getAllClasses() {
        return classDao.getAll();
    }

    /** Lấy lớp theo subjectId, không kèm số học sinh */
    public LiveData<List<SchoolClass>> getClassesForSubject(int subjectId) {
        return classDao.getClassesForSubject(subjectId);
    }

    /** Lấy lớp kèm số học sinh, với subjectId = 0 để lấy tất cả */
    public LiveData<List<ClassWithCount>> getClassesWithCount(int subjectId) {
        return classDao.getClassesWithCount(subjectId);
    }

    /** Lấy thông tin một lớp theo ID */
    public LiveData<SchoolClass> getClassById(int classId) {
        return classDao.getClassById(classId);
    }

    /** Insert */
    public void insertClass(SchoolClass sc) {
        new Thread(() -> classDao.insert(sc)).start();
    }

    /** Update */
    public void updateClass(SchoolClass sc) {
        new Thread(() -> classDao.update(sc)).start();
    }

    /** Delete */
    public void deleteClass(SchoolClass sc) {
        new Thread(() -> classDao.delete(sc)).start();
    }
}
