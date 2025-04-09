package com.example.javaopencv.repository;

import androidx.lifecycle.LiveData;

import com.example.javaopencv.data.AppDatabase;
import com.example.javaopencv.data.dao.AnswerDao;
import com.example.javaopencv.data.dao.ExamDao;
import com.example.javaopencv.data.dao.ExamStatsDao;
import com.example.javaopencv.data.entity.Answer;
import com.example.javaopencv.data.entity.Exam;
import com.example.javaopencv.data.entity.ExamStats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ExamRepository {
    private final ExamDao examDao;
    private final AnswerDao answerDao;
    private final ExamStatsDao examStatsDao;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public ExamRepository(AppDatabase db) {
        this.examDao = db.examDao();
        this.answerDao = db.answerDao();
        this.examStatsDao = db.examStatsDao();
    }

    // Lấy danh sách Exam (sử dụng LiveData để tự động cập nhật UI)
    public LiveData<List<Exam>> getAllExams() {
        return examDao.getAllExams();
    }

    // Thêm một bài kiểm tra (Exam) và khởi tạo ExamStats mặc định
    public void addExam(final Exam exam) {
        executor.execute(() -> {
            examDao.insertExam(exam);
            // Tạo một bản ghi exam_stats với giá trị mặc định
            examStatsDao.insertExamStats(new ExamStats(exam.id, 0, 0, 0.0, 0.0, 0.0));
        });
    }

    // Cập nhật thông tin bài kiểm tra
    public void updateExam(final Exam exam) {
        executor.execute(() -> examDao.updateExam(exam));
    }

    // Xóa bài kiểm tra và các dữ liệu liên quan (ExamStats, Answers)
    public void deleteExam(final Exam exam) {
        executor.execute(() -> {
            examDao.deleteExam(exam);
            // Tùy vào nghiệp vụ, bạn có thể xóa hoặc reset ExamStats
            // Nếu DAO có phương thức xóa exam_stats theo examId, bạn có thể gọi ở đây.
            // answerDao.deleteAnswersByExamId(exam.id); // Nếu có phương thức này
        });
    }

    // Thêm một câu trả lời (Answer)
    public void addAnswer(final Answer answer) {
        executor.execute(() -> {
            answerDao.insertAnswer(answer);
            updateSoDapAn(answer.examId);
        });
    }

    // Cập nhật câu trả lời
    public void updateAnswer(final Answer answer) {
        executor.execute(() -> answerDao.updateAnswer(answer));
    }

    // Xóa các câu trả lời theo code
    public void deleteAnswersByCode(final int examId, final String code) {
        executor.execute(() -> {
            answerDao.deleteAnswersByCode(examId, code);
            updateSoDapAn(examId);
        });
    }

    // Cập nhật số mã đề (distinct code) trong ExamStats sau mỗi thay đổi ở bảng Answers
    private void updateSoDapAn(final int examId) {
        int count = answerDao.countDistinctCode(examId);
        ExamStats stats = examStatsDao.getExamStats(examId);
        if (stats != null) {
            stats.soDapAn = count;
            examStatsDao.updateExamStats(stats);
        }
    }

    // Ví dụ về callback để trả dữ liệu bất đồng bộ (loadMaDe)
    public interface DataCallback<T> {
        void onDataLoaded(T data);
    }

    // Lớp chứa kết quả loadMaDe: nhóm các câu trả lời theo code
    public static class MaDeResult {
        public int examId;
        public String code;
        public Map<Integer, String> answers;

        public MaDeResult(int examId, String code, Map<Integer, String> answers) {
            this.examId = examId;
            this.code = code;
            this.answers = answers;
        }
    }

    /**
     * Hàm loadMaDe: Nhóm các câu trả lời theo code và tạo map (cauSo -> dapAn)
     * Ví dụ, sau khi lấy danh sách Answer, nhóm theo code:
     * { code1: { 1: "A", 2: "B", ... }, code2: { 1: "C", 2: "D", ... } }
     */
    public void loadMaDe(final int examId, final DataCallback<List<MaDeResult>> callback) {
        executor.execute(() -> {
            List<Answer> answers = answerDao.getAnswersByExam(examId);
            Map<String, Map<Integer, String>> map = new HashMap<>();

            for (Answer answer : answers) {
                if (!map.containsKey(answer.code)) {
                    map.put(answer.code, new HashMap<>());
                }
                map.get(answer.code).put(answer.cauSo, answer.dapAn);
            }

            List<MaDeResult> result = new ArrayList<>();
            for (Map.Entry<String, Map<Integer, String>> entry : map.entrySet()) {
                result.add(new MaDeResult(examId, entry.getKey(), entry.getValue()));
            }
            callback.onDataLoaded(result);
        });
    }
}
