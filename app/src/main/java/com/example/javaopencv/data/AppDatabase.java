package com.example.javaopencv.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.javaopencv.data.dao.AnswerDao;
import com.example.javaopencv.data.dao.ClassDao;
import com.example.javaopencv.data.dao.ExamDao;
import com.example.javaopencv.data.dao.ExamStatsDao;
import com.example.javaopencv.data.dao.GradeResultDao;
import com.example.javaopencv.data.dao.StudentDao;
import com.example.javaopencv.data.dao.SubjectDao;
import com.example.javaopencv.data.entity.Answer;
import com.example.javaopencv.data.entity.Exam;
import com.example.javaopencv.data.entity.ExamStats;
import com.example.javaopencv.data.entity.GradeResult;
import com.example.javaopencv.data.entity.SchoolClass;
import com.example.javaopencv.data.entity.Student;
import com.example.javaopencv.data.entity.Subject;

@Database(
        entities = {
                Subject.class,
                SchoolClass.class,
                Student.class,
                Exam.class,
                Answer.class,
                GradeResult.class,
                ExamStats.class
        },
        version = 16,    // bạn đã bump lên 15
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract SubjectDao subjectDao();
    public abstract ClassDao classDao();
    public abstract StudentDao studentDao();
    public abstract ExamDao examDao();
    public abstract AnswerDao answerDao();
    public abstract GradeResultDao gradeResultDao();
    public abstract ExamStatsDao examStatsDao();

    public static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE GradeResult ADD COLUMN imagePath TEXT");
        }
    };

    public static final Migration MIGRATION_11_12 = new Migration(11, 12) {
        @Override public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL(
                    "CREATE TABLE exams_new (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "classId INTEGER, " +
                            "title TEXT, " +
                            "phieu TEXT, " +
                            "so_cau INTEGER NOT NULL, " +
                            "date TEXT, " +
                            "FOREIGN KEY(classId) REFERENCES SchoolClass(id) ON DELETE CASCADE" +
                            ")"
            );
            db.execSQL(
                    "INSERT INTO exams_new (id, classId, title, phieu, so_cau, date) " +
                            "SELECT id, classId, title, phieu, so_cau, date FROM exams"
            );
            db.execSQL("DROP TABLE exams");
            db.execSQL("ALTER TABLE exams_new RENAME TO exams");
            db.execSQL("CREATE INDEX index_exams_classId ON exams(classId)");
        }
    };

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "exams.db"
                    )
                    .addMigrations(MIGRATION_5_6, MIGRATION_11_12)
                    .fallbackToDestructiveMigration()
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .addCallback(new RoomDatabase.Callback() {
                        @Override
                        public void onOpen(@NonNull SupportSQLiteDatabase db) {
                            super.onOpen(db);
                            // Tắt kiểm tra FOREIGN KEY để không còn crash nữa
                            db.execSQL("PRAGMA foreign_keys = OFF");
                        }
                    })
                    .build();
        }
        return instance;
    }
}
