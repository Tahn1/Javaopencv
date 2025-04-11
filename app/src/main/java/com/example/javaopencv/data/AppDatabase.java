package com.example.javaopencv.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.javaopencv.data.dao.AnswerDao;
import com.example.javaopencv.data.dao.ExamDao;
import com.example.javaopencv.data.dao.ExamStatsDao;
import com.example.javaopencv.data.entity.Answer;
import com.example.javaopencv.data.entity.Exam;
import com.example.javaopencv.data.entity.ExamStats;

@Database(entities = {Exam.class, Answer.class, ExamStats.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract ExamDao examDao();
    public abstract AnswerDao answerDao();
    public abstract ExamStatsDao examStatsDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "exams.db")
                    .fallbackToDestructiveMigration()  // Xoá và tái tạo DB nếu schema thay đổi
                    .build();
        }
        return instance;
    }
}
