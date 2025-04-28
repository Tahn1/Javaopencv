package com.example.javaopencv.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.javaopencv.data.entity.Subject;
import com.example.javaopencv.repository.SubjectRepository;

import java.util.List;

public class SubjectViewModel extends AndroidViewModel {
    private final SubjectRepository repository;
    private final LiveData<List<Subject>> subjects;

    public SubjectViewModel(@NonNull Application application) {
        super(application);
        repository = new SubjectRepository(application);
        subjects = repository.getAllSubjects();
    }

    public LiveData<List<Subject>> getAllSubjects() {
        return subjects;
    }

    public void insertSubject(Subject subject) {
        repository.insert(subject);
    }

    public void updateSubject(Subject subject) {
        repository.update(subject);
    }

    public void deleteSubject(Subject subject) {
        repository.delete(subject);
    }
}
