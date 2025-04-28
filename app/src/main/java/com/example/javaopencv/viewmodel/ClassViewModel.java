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
 * ViewModel quản lý danh sách lớp học (có thể theo môn) và hỗ trợ đưa vào spinner
 */
public class ClassViewModel extends AndroidViewModel {
    private final ClassRepository repository;
    private final int subjectId;
    private final LiveData<List<SchoolClass>> classes;
    private final LiveData<List<ClassWithCount>> classesWithCount;

    /**
     * @param application application context
     * @param subjectId   0 để lấy tất cả lớp, khác 0 để lấy theo môn
     */
    public ClassViewModel(@NonNull Application application, int subjectId) {
        super(application);
        this.subjectId = subjectId;
        repository = new ClassRepository(application);

        // LiveData chỉ chứa SchoolClass
        if (subjectId == 0) {
            classes = repository.getAllClasses();
        } else {
            classes = repository.getClassesForSubject(subjectId);
        }

        // LiveData kèm số lượng học sinh
        classesWithCount = repository.getClassesWithCount(subjectId);
    }

    /**
     * Trả về LiveData danh sách SchoolClass (đã lọc theo subjectId)
     */
    public LiveData<List<SchoolClass>> getClasses() {
        return classes;
    }

    /**
     * Trả về LiveData danh sách ClassWithCount (có kèm số học sinh)
     */
    public LiveData<List<ClassWithCount>> getClassesWithCount() {
        return classesWithCount;
    }

    /**
     * Nếu cần lấy tất cả lớp (ignore subjectId)
     */
    public LiveData<List<SchoolClass>> getAllClasses() {
        return repository.getAllClasses();
    }

    /**
     * Lấy thông tin một lớp theo id
     */
    public LiveData<SchoolClass> getClassById(int classId) {
        return repository.getClassById(classId);
    }

    /**
     * Thêm lớp mới
     */
    public void insertClass(SchoolClass sc) {
        repository.insertClass(sc);
    }

    /**
     * Cập nhật lớp
     */
    public void updateClass(SchoolClass sc) {
        repository.updateClass(sc);
    }

    /**
     * Xóa lớp
     */
    public void deleteClass(SchoolClass sc) {
        repository.deleteClass(sc);
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
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}
