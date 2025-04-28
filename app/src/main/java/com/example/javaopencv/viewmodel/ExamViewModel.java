package com.example.javaopencv.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.javaopencv.data.entity.Exam;
import com.example.javaopencv.repository.ExamRepository;

import java.util.List;

/**
 * ViewModel quản lý dữ liệu Exam cho từng Class
 */
public class ExamViewModel extends AndroidViewModel {
    private final ExamRepository repo;

    public ExamViewModel(@NonNull Application application) {
        super(application);
        // Khởi tạo repository với context app
        repo = new ExamRepository(application);
    }

    /**
     * Lấy danh sách Exam của classId (LiveData tự động cập nhật khi DB thay đổi)
     * @param classId ID của Class
     * @return LiveData chứa List<Exam>
     */
    public LiveData<List<Exam>> getExamsForClass(int classId) {
        return repo.getExamsForClass(classId);
    }

    /**
     * Chèn một Exam mới vào database
     * @param exam Exam cần tạo
     */
    public void insertExam(Exam exam) {
        repo.insertExam(exam);
    }

    public void updateExam(Exam exam) {
        repo.updateExam(exam);
    }

    /** Xóa Exam */
    public void deleteExam(Exam exam) {
        repo.deleteExam(exam);
    }
    public LiveData<Exam> getExamById(int id) {
        return repo.getExamById(id);
    }
}
