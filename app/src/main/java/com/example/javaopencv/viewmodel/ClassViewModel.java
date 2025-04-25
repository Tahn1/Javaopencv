package com.example.javaopencv.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.javaopencv.data.entity.SchoolClass;
import com.example.javaopencv.repository.ClassRepository;

import java.util.List;

public class ClassViewModel extends AndroidViewModel {
    private final ClassRepository repository;
    private final LiveData<List<SchoolClass>> classes;

    public ClassViewModel(@NonNull Application application, int subjectId) {
        super(application);
        repository = new ClassRepository(application);
        if (subjectId == 0) {
            // Hiển thị tất cả lớp
            classes = repository.getAllClasses();
        } else {
            classes = repository.getClassesForSubject(subjectId);
        }
    }

    public LiveData<List<SchoolClass>> getClasses() {
        return classes;
    }

    public void insertClass(SchoolClass schoolClass) {
        repository.insert(schoolClass);
    }

    public void updateClass(SchoolClass schoolClass) {
        repository.update(schoolClass);
    }

    public void deleteClass(SchoolClass schoolClass) {
        repository.delete(schoolClass);
    }

    /**
     * Factory để truyền subjectId vào ViewModel
     */
    public static class Factory implements ViewModelProvider.Factory {
        private final Application app;
        private final int subjectId;

        public Factory(Application app, int subjectId) {
            this.app = app;
            this.subjectId = subjectId;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ClassViewModel.class)) {
                return (T) new ClassViewModel(app, subjectId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}