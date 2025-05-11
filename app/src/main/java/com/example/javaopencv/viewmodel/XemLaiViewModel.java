package com.example.javaopencv.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.javaopencv.data.AppDatabase;
import com.example.javaopencv.data.dao.AnswerDao;
import com.example.javaopencv.data.dao.ExamDao;
import com.example.javaopencv.data.dao.GradeResultDao;
import com.example.javaopencv.data.dao.StudentDao;
import com.example.javaopencv.data.entity.Answer;
import com.example.javaopencv.data.entity.Exam;
import com.example.javaopencv.data.entity.GradeResult;
import com.example.javaopencv.data.entity.Student;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XemLaiViewModel extends AndroidViewModel {
    private final ExamDao examDao;
    private final StudentDao studentDao;
    private final GradeResultDao gradeDao;
    private final AnswerDao answerDao;
    private final LiveData<List<Answer>> keyLive;
    private final int examId;

    public XemLaiViewModel(@NonNull Application app, int examId) {
        super(app);
        this.examId    = examId;
        AppDatabase db = AppDatabase.getInstance(app);
        examDao    = db.examDao();
        studentDao = db.studentDao();
        gradeDao   = db.gradeResultDao();
        answerDao  = db.answerDao();

        // Khi answer-key thay đổi, tự động tái tính điểm
        keyLive = answerDao.getAnswersForExamLive(examId);
        keyLive.observeForever(this::applyNewKeyAndRegrade);
    }

    /** Factory để truyền examId vào ViewModel */
    public static class Factory implements ViewModelProvider.Factory {
        private final Application app;
        private final int examId;
        public Factory(Application app, int examId) {
            this.app    = app;
            this.examId = examId;
        }
        @SuppressWarnings("unchecked")
        @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(XemLaiViewModel.class)) {
                return (T) new XemLaiViewModel(app, examId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }

    /** Lấy classId (lớp) của kỳ thi */
    public LiveData<Integer> getClassIdForExam() {
        return Transformations.map(
                examDao.getExamById(examId),
                e -> e != null ? e.getClassId() : null
        );
    }

    /** Danh sách GradeResult (LiveData) cho exam hiện tại */
    public LiveData<List<GradeResult>> getResultsForExam() {
        return gradeDao.getResultsForExam(examId);
    }

    /** Danh sách GradeResult đồng bộ, thường dùng để xuất file */
    public List<GradeResult> getResultsListSync() {
        return gradeDao.getResultsListSync(examId);
    }

    /** Lấy GradeResult theo id (LiveData) */
    public LiveData<GradeResult> getGradeResultById(long gradeId) {
        return gradeDao.getGradeResultById(gradeId);
    }

    /** Danh sách Student của 1 lớp (LiveData) */
    public LiveData<List<Student>> getStudentsForClass(int classId) {
        return studentDao.getStudentsForClass(classId);
    }

    /** Lấy thông tin Exam đồng bộ */
    public Exam getExamSync() {
        return examDao.getExamSync(examId);
    }

    /** Xóa 1 GradeResult */
    public void deleteResult(GradeResult result) {
        new Thread(() -> gradeDao.deleteResult(result)).start();
    }

    /** Xóa tất cả GradeResult của đề thi này */
    public void deleteAllResultsForExam() {
        new Thread(() -> gradeDao.deleteAllByExamId(examId)).start();
    }

    /**
     * Regrade tất cả GradeResult mỗi khi answer-key thay đổi.
     * Nếu mã đề không có trong map thì cho 0 điểm.
     */
    private void applyNewKeyAndRegrade(List<Answer> allAnswers) {
        new Thread(() -> {
            // 1) Nhóm answer-key theo mã đề
            Map<String, Map<Integer,String>> keysByCode = new HashMap<>();
            for (Answer a : allAnswers) {
                keysByCode
                        .computeIfAbsent(a.code, k -> new HashMap<>())
                        .put(a.cauSo, a.dapAn);
            }

            // 2) Lấy tất cả GradeResult
            List<GradeResult> results = gradeDao.getResultsListSync(examId);

            // 3) Re-calc & update nếu khác
            for (GradeResult r : results) {
                // đáp án đã chọn
                String[] picks = r.getAnswersCsv().split(",");
                int totalPicks = picks.length;

                Map<Integer,String> keyMap = keysByCode.get(r.getMaDe());
                int correctCount;
                int totalQ;

                if (keyMap != null) {
                    // số câu của đề
                    totalQ = keyMap.size();
                    correctCount = 0;
                    for (int i = 0; i < picks.length && i < totalQ; i++) {
                        String correct = keyMap.get(i + 1);
                        if (correct != null && picks[i].equals(correct)) {
                            correctCount++;
                        }
                    }
                } else {
                    // mã đề không tồn tại → 0 điểm, vẫn giữ totalQ = số câu đã chọn (hoặc bạn có thể set bằng 0)
                    totalQ = totalPicks;
                    correctCount = 0;
                }

                double newScore = totalQ > 0 ? correctCount * 10.0 / totalQ : 0;
                if (correctCount != r.getCorrectCount()
                        || totalQ != r.getTotalQuestions()
                        || Double.compare(newScore, r.getScore()) != 0) {

                    r.correctCount   = correctCount;
                    r.totalQuestions = totalQ;
                    r.score          = newScore;
                    gradeDao.updateResult(r);
                }
            }
        }).start();
    }
}
