package com.example.javaopencv.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.room.Room;
import com.example.javaopencv.data.AppDatabase;
import com.example.javaopencv.data.entity.Exam;
import com.example.javaopencv.repository.ExamRepository;
import java.util.List;

public class ExamViewModel extends AndroidViewModel {

    private final ExamRepository repository;
    private final LiveData<List<Exam>> allExams;

    public ExamViewModel(@NonNull Application application) {
        super(application);
        // Khởi tạo database với Room
        AppDatabase db = Room.databaseBuilder(application, AppDatabase.class, "exams.db").build();
        repository = new ExamRepository(db);
        allExams = repository.getAllExams();
    }

    // Cung cấp dữ liệu danh sách Exam cho UI thông qua LiveData
    public LiveData<List<Exam>> getAllExams() {
        return allExams;
    }

    // Phương thức thêm một bài kiểm tra
    public void addExam(Exam exam) {
        repository.addExam(exam);
    }

    // Phương thức cập nhật thông tin bài kiểm tra
    public void updateExam(Exam exam) {
        repository.updateExam(exam);
    }

    // Phương thức xóa bài kiểm tra
    public void deleteExam(Exam exam) {
        repository.deleteExam(exam);
    }

    // Nếu cần, bạn có thể thêm các phương thức xử lý Answer, ExamStats, v.v.
}
