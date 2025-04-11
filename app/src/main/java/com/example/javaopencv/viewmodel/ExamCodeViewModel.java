package com.example.javaopencv.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.javaopencv.data.AppDatabase;
import com.example.javaopencv.data.dao.ExamCodeEntryDao;
import com.example.javaopencv.data.entity.ExamCodeEntry;
import com.example.javaopencv.repository.ExamCodeEntryRepository;
import java.util.List;

public class ExamCodeViewModel extends AndroidViewModel {

    private ExamCodeEntryRepository repository;

    public ExamCodeViewModel(@NonNull Application application) {
        super(application);
        repository = new ExamCodeEntryRepository(application);
    }

    public LiveData<List<ExamCodeEntry>> getExamCodeEntries(int examId) {
        // Sử dụng tên mới trong repository
        return repository.getExamCodeEntryByExamId(examId);
    }

    public void updateExamCodeEntry(ExamCodeEntry entry) {
        repository.updateExamCodeEntry(entry);
    }
}
