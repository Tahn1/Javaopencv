package com.example.javaopencv.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.javaopencv.data.entity.Exam;
import com.example.javaopencv.repository.ExamRepository;

import java.util.List;

/**
 * ViewModel quản lý các thao tác với Exam repository
 */
public class ExamViewModel extends AndroidViewModel {
    private final ExamRepository repo;

    public ExamViewModel(@NonNull Application application) {
        super(application);
        // Khởi tạo repository
        repo = new ExamRepository(application);
    }

    /**
     * Lấy danh sách Exam của một lớp
     */
    public LiveData<List<Exam>> getExamsForClass(int classId) {
        return repo.getExamsForClass(classId);
    }

    /**
     * Tạo mới Exam
     */
    public void insertExam(Exam exam) {
        repo.insertExam(exam);
    }

    /**
     * Cập nhật Exam
     */
    public void updateExam(Exam exam) {
        repo.updateExam(exam);
    }

    /**
     * Xóa Exam
     */
    public void deleteExam(Exam exam) {
        repo.deleteExam(exam);
    }

    /**
     * Lấy chi tiết Exam theo id
     */
    public LiveData<Exam> getExamById(int id) {
        return repo.getExamById(id);
    }
}