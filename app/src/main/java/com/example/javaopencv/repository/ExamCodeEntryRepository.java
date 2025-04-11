package com.example.javaopencv.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.javaopencv.data.AppDatabase;
import com.example.javaopencv.data.dao.ExamCodeEntryDao;
import com.example.javaopencv.data.entity.ExamCodeEntry;
import java.util.List;

public class ExamCodeEntryRepository {

    private ExamCodeEntryDao examCodeEntryDao;

    public ExamCodeEntryRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        examCodeEntryDao = db.examCodeEntryDao();
    }

    // Sử dụng tên mới: getExamCodeEntryByExamId
    public LiveData<List<ExamCodeEntry>> getExamCodeEntryByExamId(int examId) {
        return examCodeEntryDao.getExamCodeEntryByExamId(examId);
    }

    public void updateExamCodeEntry(ExamCodeEntry entry) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            examCodeEntryDao.updateExamCodeEntry(entry);
        });
    }

    public void insertExamCodeEntry(ExamCodeEntry entry) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            examCodeEntryDao.insertExamCodeEntry(entry);
        });
    }

    public void deleteExamCodeEntry(ExamCodeEntry entry) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            examCodeEntryDao.deleteExamCodeEntry(entry);
        });
    }
}
