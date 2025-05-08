package com.example.javaopencv.data;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.javaopencv.data.dao.AnswerDao;
import com.example.javaopencv.data.dao.ClassDao;
import com.example.javaopencv.data.dao.ExamDao;
import com.example.javaopencv.data.dao.ExamStatsDao;
import com.example.javaopencv.data.dao.GradeResultDao;
import com.example.javaopencv.data.dao.StudentDao;
import com.example.javaopencv.data.entity.Answer;
import com.example.javaopencv.data.entity.Exam;
import com.example.javaopencv.data.entity.ExamStats;
import com.example.javaopencv.data.entity.GradeResult;
import com.example.javaopencv.data.entity.SchoolClass;
import com.example.javaopencv.data.entity.Student;

@Database(
        entities = {
                SchoolClass.class,
                Student.class,
                Exam.class,
                Answer.class,
                GradeResult.class,
                ExamStats.class
        },
        version = 26,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase instance;
    private static final String DB_NAME = "exams.db";

    // --- Các DAO đã giữ lại ---
    public abstract ClassDao classDao();
    public abstract StudentDao studentDao();
    public abstract ExamDao examDao();
    public abstract AnswerDao answerDao();
    public abstract GradeResultDao gradeResultDao();
    public abstract ExamStatsDao examStatsDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DB_NAME
                    )
                    // Khôi phục destructive khi schema thay đổi
                    .fallbackToDestructiveMigration()
                    // Callback để tắt kiểm tra foreign key (nếu cần)
                    .addCallback(new RoomDatabase.Callback() {
                        @Override
                        public void onOpen(@NonNull SupportSQLiteDatabase db) {
                            super.onOpen(db);
                            db.execSQL("PRAGMA foreign_keys = OFF");
                        }
                    })
                    .build();
        }
        return instance;
    }
}
