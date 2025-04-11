package com.example.javaopencv.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.javaopencv.data.dao.ExamCodeEntryDao;
import com.example.javaopencv.data.dao.ExamDao; // Nếu bạn có bảng Exam
import com.example.javaopencv.data.entity.ExamCodeEntry;
import com.example.javaopencv.data.entity.Exam; // Nếu có bảng Exam

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {ExamCodeEntry.class, Exam.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract ExamCodeEntryDao examCodeEntryDao();
    public abstract ExamDao examDao(); // Nếu bạn sử dụng bảng Exam

    // Executor để chạy các truy vấn ghi dữ liệu ngoài main thread
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "exams.db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
