package com.example.javaopencv.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.javaopencv.data.dao.ExamDao;
import com.example.javaopencv.data.entity.Exam;

@Database(entities = {Exam.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract ExamDao examDao();

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
