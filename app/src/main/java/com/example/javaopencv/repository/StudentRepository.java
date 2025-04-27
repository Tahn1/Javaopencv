package com.example.javaopencv.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.javaopencv.data.AppDatabase;
import com.example.javaopencv.data.dao.StudentDao;
import com.example.javaopencv.data.entity.Student;
import java.util.List;

public class StudentRepository {
    private final StudentDao studentDao;

    public StudentRepository(Application app) {
        AppDatabase db = AppDatabase.getInstance(app);
        studentDao = db.studentDao();
    }

    public LiveData<List<Student>> getStudentsForClass(int classId) {
        return studentDao.getStudentsForClass(classId);
    }

    public void insertStudent(Student student) {
        new Thread(() -> studentDao.insert(student)).start();
    }
}