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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class KiemTraViewModel extends AndroidViewModel {

    private final ExamRepository repository;
    private final LiveData<List<Exam>> exams;

    public KiemTraViewModel(@NonNull Application application) {
        super(application);
        // ★ Sử dụng singleton thay vì Room.databaseBuilder trực tiếp
        AppDatabase db = AppDatabase.getInstance(application.getApplicationContext());
        repository = new ExamRepository(db);
        exams      = repository.getAllExams();
    }

    public LiveData<List<Exam>> getExams() {
        return exams;
    }

    public void insertExam(Exam exam) {
        repository.insertExam(exam);
    }

    public void updateExam(Exam exam) {
        repository.updateExam(exam);
    }

    public void deleteExam(Exam exam) {
        repository.deleteExam(exam);
    }
}
