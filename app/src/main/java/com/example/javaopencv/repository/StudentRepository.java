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
                // Duplicate trong cùng lớp
                mainHandler.post(() ->
                        Toast.makeText(
                                app,
                                "Số báo danh đã tồn tại trong lớp này",
                                Toast.LENGTH_SHORT
                        ).show()
                );
            } else {
                // Insert thành công (nếu muốn bạn có thể show Toast success)
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

    /** Xóa sinh viên */
    public void deleteStudent(Student student) {
        new Thread(() -> studentDao.delete(student)).start();
    }
}
