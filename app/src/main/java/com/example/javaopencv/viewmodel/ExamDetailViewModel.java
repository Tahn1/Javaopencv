package com.example.javaopencv.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.javaopencv.data.dao.AnswerDao;
import com.example.javaopencv.data.entity.Answer;
import com.example.javaopencv.data.AppDatabase;

import java.util.List;

public class ExamDetailViewModel extends AndroidViewModel {
    private AppDatabase db;
    private AnswerDao answerDao;

    public ExamDetailViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
        answerDao = db.answerDao();
    }

    /**
     * Cập nhật hoặc chèn đáp án cho bài thi.
     * Với mỗi câu (cauSo), nếu đã có bản ghi, cập nhật; nếu chưa có, chèn mới.
     *
     * @param examId     ID của bài thi
     * @param maDe       Mã đề được chọn (lưu vào trường code của Answer)
     * @param answerList Danh sách đáp án được nhập từ UI, index 0 tương ứng với câu số 1, ...
     */
    public void updateExamAnswers(final int examId, final String maDe, final List<String> answerList) {
        new Thread(() -> {
            // Lấy danh sách đáp án hiện có cho bài thi với mã đề (code) cụ thể
            List<Answer> existingAnswers = answerDao.getAnswersByExamAndCodeSync(examId, maDe);

            // Duyệt qua danh sách đáp án từ UI
            for (int i = 0; i < answerList.size(); i++) {
                String dapAn = answerList.get(i);
                int cauSo = i + 1;
                Answer existing = null;
                if (existingAnswers != null) {
                    for (Answer ans : existingAnswers) {
                        if (ans.cauSo == cauSo) {
                            existing = ans;
                            break;
                        }
                    }
                }
                if (existing != null) {
                    // Nếu có bản ghi, cập nhật đáp án
                    existing.dapAn = dapAn;
                    answerDao.updateAnswer(existing);
                } else {
                    // Nếu chưa có, tạo mới bản ghi
                    Answer newAnswer = new Answer(examId, maDe, cauSo, dapAn);
                    answerDao.insertAnswer(newAnswer);
                }
            }
        }).start();
    }
}
