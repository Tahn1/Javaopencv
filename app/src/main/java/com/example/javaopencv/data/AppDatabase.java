package com.example.javaopencv.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.javaopencv.data.dao.AnswerDao;
import com.example.javaopencv.data.dao.ExamDao;
import com.example.javaopencv.data.dao.ExamStatsDao;
import com.example.javaopencv.data.dao.GradeResultDao;
import com.example.javaopencv.data.entity.Answer;
import com.example.javaopencv.data.entity.Exam;
import com.example.javaopencv.data.entity.ExamStats;
import com.example.javaopencv.data.entity.GradeResult;

@Database(entities = {Exam.class, Answer.class, ExamStats.class, GradeResult.class}, version = 9, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract ExamDao examDao();
    public abstract AnswerDao answerDao();
    public abstract ExamStatsDao examStatsDao();
    public abstract GradeResultDao gradeResultDao();


    public static synchronized AppDatabase getInstance(Context ctx) {
        if (instance == null) {
            instance = Room.databaseBuilder(ctx.getApplicationContext(),
                            AppDatabase.class, "exams.db")
                    // migration 5→6: thêm cột imagePath
                    .addMigrations(new Migration(5,6) {
                        @Override
                        public void migrate(@NonNull SupportSQLiteDatabase db) {
                            db.execSQL(
                                    "ALTER TABLE GradeResult ADD COLUMN imagePath TEXT");
                        }
                    })
                    .build();
        }
        return instance;
    }
}
