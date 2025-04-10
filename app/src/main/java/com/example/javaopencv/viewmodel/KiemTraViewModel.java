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

    private ExamRepository repository;
    private LiveData<List<Exam>> exams;
    private Executor executor = Executors.newSingleThreadExecutor();

    public KiemTraViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = Room.databaseBuilder(application, AppDatabase.class, "exams.db")
                .fallbackToDestructiveMigration() // Nếu không có migration, dùng phương pháp này
                .build();
        repository = new ExamRepository(db);
        exams = repository.getAllExams();
    }

    public LiveData<List<Exam>> getExams() {
        return exams;
    }

    public void addExam(Exam exam) {
        repository.insertExam(exam);
    }

    public void updateExam(Exam exam) {
        repository.updateExam(exam);
    }

    public void deleteExam(Exam exam) {
        repository.deleteExam(exam);
    }
}
