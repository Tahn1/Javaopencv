package com.example.javaopencv.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.javaopencv.data.AppDatabase;
import com.example.javaopencv.data.dao.ClassDao;
import com.example.javaopencv.data.entity.ClassWithCount;
import com.example.javaopencv.data.entity.SchoolClass;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClassRepository {
    private final ClassDao classDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ClassRepository(Application app) {
        classDao = AppDatabase.getInstance(app).classDao();
    }

    /** Lấy tất cả lớp, không kèm số học sinh */
    public LiveData<List<SchoolClass>> getAllClasses() {
        return classDao.getAllClasses();
    }

    /** Lấy lớp theo ID */
    public LiveData<SchoolClass> getClassById(int classId) {
        return classDao.getClassById(classId);
    }

    /** Lấy danh sách lớp kèm số học sinh trong mỗi lớp */
    public LiveData<List<ClassWithCount>> getClassesWithCount() {
        return classDao.getClassesWithCount();
    }

    /** Thêm lớp mới */
    public void insertClass(SchoolClass sc) {
        executor.execute(() -> classDao.insert(sc));
    }

    /** Cập nhật thông tin lớp */
    public void updateClass(SchoolClass sc) {
        executor.execute(() -> classDao.update(sc));
    }

    /** Xóa lớp */
    public void deleteClass(SchoolClass sc) {
        executor.execute(() -> classDao.delete(sc));
    }
}
