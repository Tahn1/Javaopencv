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

    /**
     * Trả về danh sách sinh viên theo lớp
     */
    public LiveData<List<Student>> getStudentsForClass(int classId) {
        return repo.getStudentsForClass(classId);
    }

    /**
     * Thêm sinh viên mới
     */
    public void insertStudent(Student student) {
        repo.insertStudent(student);
    }

    /**
     * Cập nhật thông tin sinh viên (chỉ tên)
     */
    public void updateStudent(Student student) {
        repo.updateStudent(student);
    }

    /**
     * Xóa sinh viên
     */
    public void deleteStudent(Student student) {
        repo.deleteStudent(student);
    }
}
