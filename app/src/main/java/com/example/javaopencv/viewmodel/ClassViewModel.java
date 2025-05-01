package com.example.javaopencv.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.javaopencv.data.entity.ClassWithCount;
import com.example.javaopencv.data.entity.SchoolClass;
import com.example.javaopencv.repository.ClassRepository;

import java.util.List;

/**
 * ViewModel quản lý danh sách lớp học và số học sinh.
 */
public class ClassViewModel extends AndroidViewModel {
    private final ClassRepository repository;
    private final LiveData<List<SchoolClass>> classes;
    private final LiveData<List<ClassWithCount>> classesWithCount;

    public ClassViewModel(@NonNull Application application) {
        super(application);
        repository = new ClassRepository(application);
        // Luôn lấy toàn bộ lớp
        classes = repository.getAllClasses();
        // Luôn lấy toàn bộ lớp kèm số học sinh
        classesWithCount = repository.getClassesWithCount();
    }

    /** Trả về LiveData danh sách SchoolClass */
    public LiveData<List<SchoolClass>> getAllClasses() {
        return classes;
    }

    /** Trả về LiveData danh sách ClassWithCount (có kèm số học sinh) */
    public LiveData<List<ClassWithCount>> getClassesWithCount() {
        return classesWithCount;
    }

    /** Lấy chi tiết một lớp theo ID */
    public LiveData<SchoolClass> getClassById(int classId) {
        return repository.getClassById(classId);
    }

    /** Thêm lớp mới */
    public void insertClass(SchoolClass sc) {
        repository.insertClass(sc);
    }

    /** Cập nhật lớp */
    public void updateClass(SchoolClass sc) {
        repository.updateClass(sc);
    }

    /** Xóa lớp */
    public void deleteClass(SchoolClass sc) {
        repository.deleteClass(sc);
    }

    /** Factory chỉ nhận Application */
    public static class Factory implements ViewModelProvider.Factory {
        private final Application app;

        public Factory(Application app) {
            this.app = app;
        }

        @NonNull @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ClassViewModel.class)) {
                return (T) new ClassViewModel(app);
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}
