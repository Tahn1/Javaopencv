package com.example.javaopencv.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.javaopencv.data.entity.Exam;
import com.example.javaopencv.repository.ExamRepository;

import java.util.List;

public class KiemTraViewModel extends AndroidViewModel {
    private final ExamRepository repository;
    private final LiveData<List<Exam>> exams;

    public KiemTraViewModel(@NonNull Application application) {
        super(application);
        repository = new ExamRepository(application);
        exams = repository.getAllExams();
    }

    /** LiveData để fragment quan sát */
    public LiveData<List<Exam>> getExams() {
        return exams;
    }

    /** Thêm bài kiểm tra mới */
    public void insertExam(Exam exam) {
        repository.insertExam(exam);
    }

    /** Cập nhật bài kiểm tra */
    public void updateExam(Exam exam) {
        repository.updateExam(exam);
    }

    /** Xóa bài kiểm tra */
    public void deleteExam(Exam exam) {
        repository.deleteExam(exam);
    }
}
