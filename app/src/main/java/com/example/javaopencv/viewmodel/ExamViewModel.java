package com.example.javaopencv.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.javaopencv.data.AppDatabase;
import com.example.javaopencv.data.entity.Exam;
import com.example.javaopencv.repository.ExamRepository;

import java.util.List;

public class ExamViewModel extends AndroidViewModel {

    private ExamRepository repository;
    private LiveData<List<Exam>> allExams;

    public ExamViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        repository = new ExamRepository(db);
        allExams = repository.getAllExams();
    }

    public LiveData<List<Exam>> getAllExams() {
        return allExams;
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
