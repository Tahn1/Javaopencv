package com.example.javaopencv.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import com.example.javaopencv.data.AppDatabase;
import com.example.javaopencv.data.dao.ExamDao;
import com.example.javaopencv.data.dao.GradeResultDao;
import com.example.javaopencv.data.dao.StudentDao;
import com.example.javaopencv.data.entity.Exam;
import com.example.javaopencv.data.entity.GradeResult;
import com.example.javaopencv.data.entity.Student;

import java.util.List;

public class XemLaiViewModel extends AndroidViewModel {
    private final ExamDao examDao;
    private final StudentDao studentDao;
    private final GradeResultDao gradeDao;

    public XemLaiViewModel(@NonNull Application app) {
        super(app);
        AppDatabase db = AppDatabase.getInstance(app);
        examDao    = db.examDao();
        studentDao = db.studentDao();
        gradeDao   = db.gradeResultDao();
    }

    /**
     * Lấy classId (lớp) của kỳ thi
     */
    public LiveData<Integer> getClassIdForExam(int examId) {
        return Transformations.map(
                examDao.getExamById(examId),
                exam -> exam != null ? exam.getClassId() : null
        );
    }

    /**
     * Trả về LiveData danh sách GradeResult của một đề (examId)
     */
    public LiveData<List<GradeResult>> getResultsForExam(int examId) {
        return gradeDao.getResultsForExam(examId);
    }

    /**
     * Trả về danh sách GradeResult đồng bộ để xuất file
     */
    public List<GradeResult> getResultsListSync(int examId) {
        return gradeDao.getResultsListSync(examId);
    }

    /**
     * Trả về LiveData GradeResult theo id
     */
    public LiveData<GradeResult> getGradeResultById(long gradeId) {
        return gradeDao.getGradeResultById(gradeId);
    }

    /**
     * Trả về LiveData danh sách Student của một lớp (classId)
     */
    public LiveData<List<Student>> getStudentsForClass(int classId) {
        return studentDao.getStudentsForClass(classId);
    }

    /**
     * Lấy Exam đồng bộ theo id để lấy title và date
     */
    public Exam getExamSync(int examId) {
        return examDao.getExamSync(examId);
    }

    /**
     * Chèn mới GradeResult vào DB (background thread)
     */
    public void addResult(GradeResult result) {
        new Thread(() -> gradeDao.insert(result)).start();
    }

    /**
     * Cập nhật GradeResult đã có (background thread)
     */
    public void updateResult(GradeResult result) {
        new Thread(() -> gradeDao.updateResult(result)).start();
    }

    /**
     * Xóa GradeResult (background thread)
     */
    public void deleteResult(GradeResult result) {
        new Thread(() -> gradeDao.deleteResult(result)).start();
    }

    /**
     * Xóa tất cả kết quả chấm của một đề (background thread)
     */
    public void deleteAllResultsForExam(int examId) {
        new Thread(() -> gradeDao.deleteAllByExamId(examId)).start();
    }
}
