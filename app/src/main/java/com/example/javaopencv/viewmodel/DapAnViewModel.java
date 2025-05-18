package com.example.javaopencv.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.javaopencv.data.entity.Answer;
import com.example.javaopencv.repository.AnswerRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DapAnViewModel extends AndroidViewModel {

    private final MutableLiveData<List<MaDeItem>> maDeList =
            new MutableLiveData<>(new ArrayList<>());
    private final AnswerRepository answerRepository;
    private int examId = -1;

    public DapAnViewModel(@NonNull Application application) {
        super(application);
        answerRepository = AnswerRepository.getInstance(application);
    }

    public void setExamId(int examId) {
        this.examId = examId;
        loadMaDeList();
    }

    public int getExamId() {
        return examId;
    }

    public LiveData<List<MaDeItem>> getMaDeList() {
        return maDeList;
    }

    private void loadMaDeList() {
        if (examId < 0) return;
        new Thread(() -> {
            List<String> codes = answerRepository.getDistinctCodesSync(examId);
            List<MaDeItem> items = new ArrayList<>();
            for (String code : codes) {
                List<Answer> ansList =
                        answerRepository.getAnswersByExamAndCodeSync(examId, code);
                List<String> answersStr = new ArrayList<>();
                for (Answer a : ansList) {
                    answersStr.add(a.dapAn);
                }
                items.add(new MaDeItem(code, answersStr));
            }
            maDeList.postValue(items);
        }).start();
    }

    public void addMaDe(String code, List<String> answerList, int questionCount) {
        new Thread(() -> {
            if (examId < 0) {
                Log.e("DapAnViewModel", "ExamId chưa set!");
                return;
            }
            List<String> finalAns = ensureSize(answerList, questionCount);
            for (int i = 0; i < questionCount; i++) {
                Answer row = new Answer(examId, code, i + 1, finalAns.get(i));
                answerRepository.insertAnswerSync(row);
            }
            loadMaDeList();
        }).start();
    }

    public void updateMaDe(int position,
                           String newCode,
                           List<String> newAnsList,
                           int questionCount) {
        List<String> finalNew = ensureSize(newAnsList, questionCount);

        new Thread(() -> {
            List<MaDeItem> current = maDeList.getValue();
            if (current == null || position < 0 || position >= current.size()) return;
            if (examId < 0) {
                Log.e("DapAnViewModel", "ExamId not set!");
                return;
            }

            MaDeItem oldItem = current.get(position);
            String oldCode = oldItem.code;

            // 1) Tạo map đáp án cũ
            Map<Integer, String> oldMap = new HashMap<>();
            if (!newCode.equals(oldCode)) {
                List<Answer> dbAns = answerRepository
                        .getAnswersByExamAndCodeSync(examId, oldCode);
                for (Answer a : dbAns) {
                    oldMap.put(a.cauSo, a.dapAn);
                }
            } else {
                for (int i = 0; i < oldItem.answers.size(); i++) {
                    oldMap.put(i + 1, oldItem.answers.get(i));
                }
            }

            // 2) Merge đáp án: ưu tiên finalNew, fallback sang oldMap
            List<String> merged = new ArrayList<>(questionCount);
            for (int i = 0; i < questionCount; i++) {
                String cand = finalNew.get(i);
                merged.add(cand != null ? cand : oldMap.get(i + 1));
            }

            // 3) Nếu đổi mã đề: rename toàn bộ row
            if (!newCode.equals(oldCode)) {
                answerRepository.renameCodeSync(examId, oldCode, newCode);
            }

            // 4) Duyệt từng câu để insert/update
            for (int i = 0; i < questionCount; i++) {
                int cauSo = i + 1;
                String ans = merged.get(i);
                Answer exist = answerRepository.findSingleAnswerSync(
                        examId, newCode, cauSo);
                if (exist == null) {
                    if (ans != null) {
                        answerRepository.insertAnswerSync(
                                new Answer(examId, newCode, cauSo, ans));
                    }
                } else {
                    String oldAns = exist.dapAn;
                    if (ans == null) {
                        answerRepository.updateSingleAnswerSync(
                                examId, newCode, cauSo, null);
                    } else if (!ans.equals(oldAns)) {
                        answerRepository.updateSingleAnswerSync(
                                examId, newCode, cauSo, ans);
                    }
                }
            }

            // 5) cập nhật LiveData
            current.set(position, new MaDeItem(newCode, merged));
            maDeList.postValue(current);
        }).start();
    }

    public void removeMaDe(int position) {
        new Thread(() -> {
            List<MaDeItem> current = maDeList.getValue();
            if (current == null || position < 0 || position >= current.size()) return;
            if (examId < 0) return;

            String code = current.get(position).code;
            answerRepository.deleteAnswersByCodeSync(examId, code);
            current.remove(position);
            maDeList.postValue(current);
        }).start();
    }

    public List<String> getAnswerListByPosition(int position) {
        List<MaDeItem> current = maDeList.getValue();
        if (current != null && position >= 0 && position < current.size()) {
            return current.get(position).answers;
        }
        return null;
    }

    private List<String> ensureSize(List<String> base, int questionCount) {
        List<String> copy = new ArrayList<>(base);
        while (copy.size() > questionCount) copy.remove(copy.size() - 1);
        while (copy.size() < questionCount) copy.add(null);
        return copy;
    }

    public static class MaDeItem {
        public String code;
        public List<String> answers;
        public MaDeItem(String code, List<String> answers) {
            this.code = code;
            this.answers = answers;
        }
    }
}
