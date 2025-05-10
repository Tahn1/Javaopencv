package com.example.javaopencv.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.javaopencv.data.entity.GradeResult;
import com.example.javaopencv.repository.GradeResultRepository;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GradeResultViewModel extends AndroidViewModel {
    private final GradeResultRepository repo;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final long gradeId;

    public GradeResultViewModel(@NonNull Application application, long gradeId) {
        super(application);
        this.repo = new GradeResultRepository(application);
        this.gradeId = gradeId;
    }

    /**
     * Overloaded constructor để hỗ trợ khởi tạo không cần gradeId.
     */
    public GradeResultViewModel(@NonNull Application application) {
        this(application, -1L);
    }

    /**
     * Factory để khởi tạo ViewModel với gradeId
     */
    public static class Factory implements ViewModelProvider.Factory {
        private final Application application;
        private final long gradeId;

        public Factory(Application application, long gradeId) {
            this.application = application;
            this.gradeId = gradeId;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(GradeResultViewModel.class)) {
                return (T) new GradeResultViewModel(application, gradeId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }

    /**
     * Lấy LiveData GradeResult theo ID
     */
    public LiveData<GradeResult> getGradeResultById() {
        return repo.getGradeResultById(gradeId);
    }

    /**
     * Lấy LiveData danh sách GradeResult cho một exam
     */
    public LiveData<List<GradeResult>> getResultsForExam(int examId) {
        return repo.getResultsForExam(examId);
    }

    /**
     * Cập nhật toàn bộ đối tượng GradeResult
     */
    public void updateGradeResult(GradeResult gr) {
        executor.execute(() -> repo.updateResult(gr));
    }

    /**
     * Kiểm tra duplicate SBD trước khi lưu
     */
    public void checkDuplicateAndUpdate(GradeResult gr, Callback callback) {
        executor.execute(() -> {
            int count = repo.countByExamAndStudent(gr.getExamId(), gr.getSbd(), gr.getId());
            boolean isDuplicate = count > 0;
            mainHandler.post(() -> callback.onResult(isDuplicate));
        });
    }

    public interface Callback {
        void onResult(boolean isDuplicate);
    }
}