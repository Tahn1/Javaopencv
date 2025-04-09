package com.example.javaopencv.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import com.example.javaopencv.data.dao.AnswerDao;
import com.example.javaopencv.data.dao.ExamDao;
import com.example.javaopencv.data.dao.ExamStatsDao;
import com.example.javaopencv.data.entity.Answer;
import com.example.javaopencv.data.entity.Exam;
import com.example.javaopencv.data.entity.ExamStats;

@Database(entities = {Exam.class, Answer.class, ExamStats.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ExamDao examDao();
    public abstract AnswerDao answerDao();
    public abstract ExamStatsDao examStatsDao();
}
