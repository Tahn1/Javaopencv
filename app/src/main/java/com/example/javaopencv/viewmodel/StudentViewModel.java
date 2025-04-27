package com.example.javaopencv.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.javaopencv.data.entity.Student;
import com.example.javaopencv.repository.StudentRepository;
import java.util.List;

public class StudentViewModel extends AndroidViewModel {
    private final StudentRepository repo;

    public StudentViewModel(@NonNull Application application) {
        super(application);
        repo = new StudentRepository(application);
    }

    public LiveData<List<Student>> getStudentsForClass(int classId) {
        return repo.getStudentsForClass(classId);
    }

    public void insertStudent(Student student) {
        repo.insertStudent(student);
    }
}