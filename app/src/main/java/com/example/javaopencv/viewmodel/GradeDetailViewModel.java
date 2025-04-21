package com.example.javaopencv.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.javaopencv.data.AppDatabase;
import com.example.javaopencv.data.dao.GradeResultDao;
import com.example.javaopencv.data.entity.GradeResult;

public class GradeDetailViewModel extends AndroidViewModel {
    private final LiveData<GradeResult> grade;

    public GradeDetailViewModel(@NonNull Application app, long gradeId) {
        super(app);
        GradeResultDao dao = AppDatabase.getInstance(app).gradeResultDao();
        grade = dao.getGradeResultById(gradeId);
    }

    public LiveData<GradeResult> getGradeResult() {
        return grade;
    }

    /** Factory để truyền gradeId vào ViewModel */
    public static class Factory implements ViewModelProvider.Factory {
        private final Application app;
        private final long gradeId;
        public Factory(Application app, long gradeId) {
            this.app = app;
            this.gradeId = gradeId;
        }
        @NonNull @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(GradeDetailViewModel.class)) {
                return (T) new GradeDetailViewModel(app, gradeId);
            }
            throw new IllegalArgumentException("Unknown VM class");
        }
    }
}
