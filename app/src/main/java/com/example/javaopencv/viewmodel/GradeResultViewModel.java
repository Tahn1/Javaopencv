package com.example.javaopencv.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.javaopencv.data.AppDatabase;
import com.example.javaopencv.data.dao.AnswerDao;
import com.example.javaopencv.data.entity.GradeResult;
import com.example.javaopencv.repository.GradeResultRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * ViewModel cho GradeResult, hỗ trợ lấy kết quả, cập nhật,
 * và lọc các bài sai mã đề trên background thread để tránh khóa UI.
 */
public class GradeResultViewModel extends AndroidViewModel {
    private final GradeResultRepository repo;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AnswerDao answerDao;

    public GradeResultViewModel(@NonNull Application application) {
        super(application);
        this.repo = new GradeResultRepository(application);
        this.answerDao = AppDatabase.getInstance(application).answerDao();
    }

    /**
     * Factory để khởi tạo ViewModel
     */
    public static class Factory implements ViewModelProvider.Factory {
        private final Application application;
        public Factory(Application application) {
            this.application = application;
        }
        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(GradeResultViewModel.class)) {
                return (T) new GradeResultViewModel(application);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }

    /**
     * Lấy LiveData danh sách GradeResult cho một exam
     */
    public LiveData<List<GradeResult>> getResultsForExam(int examId) {
        return repo.getResultsForExam(examId);
    }

    /**
     * LiveData chỉ bao gồm những kết quả có mã đề sai:
     * - null hoặc không đủ 3 ký tự
     * - hoặc không nằm trong danh sách mã đề hợp lệ
     */
    public LiveData<List<GradeResult>> getWrongMaDeResults(int examId) {
        MediatorLiveData<List<GradeResult>> resultLive = new MediatorLiveData<>();
        LiveData<List<GradeResult>> source = repo.getResultsForExam(examId);
        resultLive.addSource(source, allResults -> {
            executor.execute(() -> {
                List<String> validCodes = answerDao.getDistinctCodesSync(examId);
                List<GradeResult> wrongList = new ArrayList<>();
                for (GradeResult r : allResults) {
                    String code = r.getMaDe();
                    // Điều kiện sai mã đề:
                    // 1) code null hoặc không đủ 3 ký tự
                    // 2) hoặc code không nằm trong validCodes
                    if (code == null || code.length() != 3 || !validCodes.contains(code)) {
                        wrongList.add(r);
                    }
                }
                mainHandler.post(() -> resultLive.setValue(wrongList));
            });
        });
        return resultLive;
    }

    /**
     * Cập nhật toàn bộ đối tượng GradeResult
     */
    public void updateGradeResult(GradeResult gr) {
        executor.execute(() -> repo.updateResult(gr));
    }

    /**
     * Lấy LiveData GradeResult theo ID
     */
    public LiveData<GradeResult> getGradeResultById(long gradeId) {
        return repo.getGradeResultById(gradeId);
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