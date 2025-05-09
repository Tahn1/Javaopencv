package com.example.javaopencv.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.lifecycle.LiveData;

import com.example.javaopencv.data.AppDatabase;
import com.example.javaopencv.data.dao.StudentDao;
import com.example.javaopencv.data.entity.Student;

import java.util.List;

public class StudentRepository {
    private final StudentDao studentDao;
    private final Application app;
    private final Handler mainHandler;

    public StudentRepository(Application app) {
        this.app = app.getApplicationContext() instanceof Application
                ? (Application) app.getApplicationContext()
                : app;
        AppDatabase db = AppDatabase.getInstance(app);
        studentDao = db.studentDao();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    /** Lấy danh sách sinh viên theo lớp */
    public LiveData<List<Student>> getStudentsForClass(int classId) {
        return studentDao.getStudentsForClass(classId);
    }

    /**
     * Thêm sinh viên mới.
     * Nếu duplicate (classId + studentNumber), insert() trả về -1 và Toast báo lỗi.
     */
    public void insertStudent(Student student) {
        new Thread(() -> {
            long newId = studentDao.insert(student);
            if (newId == -1L) {
                mainHandler.post(() ->
                        Toast.makeText(
                                app,
                                "Số báo danh đã tồn tại trong lớp này",
                                Toast.LENGTH_SHORT
                        ).show()
                );
            } else {
                mainHandler.post(() ->
                        Toast.makeText(
                                app,
                                "Tạo học sinh thành công",
                                Toast.LENGTH_SHORT
                        ).show()
                );
            }
        }).start();
    }

    /** Cập nhật thông tin sinh viên */
    public void updateStudent(Student student) {
        new Thread(() -> studentDao.update(student)).start();
    }

    /** Xóa một sinh viên */
    public void deleteStudent(Student student) {
        new Thread(() -> studentDao.delete(student)).start();
    }

    /** Xóa tất cả sinh viên trong một lớp */
    public void deleteAllForClass(int classId) {
        new Thread(() -> {
            studentDao.deleteByClassId(classId);
            mainHandler.post(() ->
                    Toast.makeText(
                            app,
                            "Đã xóa tất cả học sinh của lớp",
                            Toast.LENGTH_SHORT
                    ).show()
            );
        }).start();
    }
}
