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

/**
 * ViewModel đơn giản:
 * - Tạo mã đề => chèn đủ N row
 * - Sửa mã đề:
 *   + Nếu code thay => rename code cũ sang code mới (giữ đáp án),
 *     rồi partial update
 *   + Nếu code giữ => lặp 1..N, updateSingleAnswer hay insert tùy row
 */
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
        loadMaDeList(); // Tải data -> maDeList
    }

    public int getExamId() {
        return examId;
    }

    public LiveData<List<MaDeItem>> getMaDeList() {
        return maDeList;
    }

    //================== LOADDATA ==================//
    private void loadMaDeList() {
        if (examId < 0) return;

        new Thread(() -> {
            List<String> codes = answerRepository.getDistinctCodesSync(examId);
            List<MaDeItem> items = new ArrayList<>();

            for (String code : codes) {
                List<Answer> ansList = answerRepository.getAnswersByExamAndCodeSync(examId, code);

                // Chuyển sang List<String> (đáp án)
                List<String> answersStr = new ArrayList<>();
                for (Answer a : ansList) {
                    answersStr.add(a.dapAn); // có thể null hoặc "A/B/C/D"
                }
                items.add(new MaDeItem(code, answersStr));
            }
            maDeList.postValue(items);
        }).start();
    }

    //================== ADD MA DE ==================//
    public void addMaDe(String code, List<String> answerList, int questionCount) {
        new Thread(() -> {
            if (examId < 0) {
                Log.e("DapAnViewModel", "ExamId chưa set!");
                return;
            }
            // Đảm bảo kích cỡ list = questionCount
            List<String> finalAns = ensureSize(answerList, questionCount);

            // Chèn row cauSo=1..questionCount
            for (int i = 0; i < questionCount; i++) {
                String dapAn = finalAns.get(i);
                Answer row = new Answer(examId, code, i + 1, dapAn);
                answerRepository.insertAnswerSync(row);
            }

            // 1) Bỏ updateLocalList(...) thủ công
            //    updateLocalList(code, finalAns, true);  // <-- XÓA hoặc comment

            // 2) Thay bằng loadMaDeList() => để LiveData tự cập nhật 1 lần
            loadMaDeList();
        }).start();
    }


    //================== UPDATE MA DE ==================//
    public void updateMaDe(int position,
                           String newCode,
                           List<String> newAnsList,
                           int questionCount) {
        // Đảm bảo list có đúng questionCount phần tử
        final List<String> finalAns = ensureSize(newAnsList, questionCount);

        new Thread(() -> {
            List<MaDeItem> current = maDeList.getValue();
            if (current == null || position<0 || position>=current.size()) {
                return;
            }
            if (examId<0) {
                Log.e("DapAnViewModel","ExamId not set!");
                return;
            }

            MaDeItem oldItem = current.get(position);
            String oldCode = oldItem.code;

            //---1) Nếu user đổi code => rename code cũ => newCode---//
            if (!newCode.equals(oldCode)) {
                Log.d("DapAnViewModel",
                        "Code changed => rename " + oldCode + " -> " + newCode);

                // Rename code cũ sang code mới => GIỮ đáp án cũ
                answerRepository.renameCodeSync(examId, oldCode, newCode);

                // Giờ code cũ đã thành newCode trong DB,
                // partial update cũ => so sánh oldAns vs newAns

                // Lấy row (examId,newCode)
                List<Answer> oldAnswers =
                        answerRepository.getAnswersByExamAndCodeSync(examId, newCode);

                // map cauSo => dapAn cũ
                Map<Integer, String> oldMap = new HashMap<>();
                for (Answer a : oldAnswers) {
                    oldMap.put(a.cauSo, a.dapAn);
                }

                // partial update 1..questionCount
                for (int i=0; i<finalAns.size(); i++) {
                    int cauSo = i+1;
                    String oldAns = oldMap.get(cauSo);
                    String newAns = finalAns.get(i);

                    if (newAns == null) {
                        newAns = oldAns;
                    }

                    if (oldAns == null && newAns != null) {
                        // row chưa có => insert
                        Answer row = new Answer(examId, newCode, cauSo, newAns);
                        answerRepository.insertAnswerSync(row);
                    }
                    else if (oldAns != null && newAns == null) {
                        // update => dapAn=null
                        answerRepository.updateSingleAnswerSync(
                                examId, newCode, cauSo, null);
                    }
                    else if (oldAns != null && newAns != null
                            && !oldAns.equals(newAns)) {
                        // update => dapAn=newAns
                        answerRepository.updateSingleAnswerSync(
                                examId, newCode, cauSo, newAns);
                    }
                    // cũ == mới => không làm gì
                }
                // Cập nhật local list
                current.set(position, new MaDeItem(newCode, finalAns));
                maDeList.postValue(current);

            }
            //---2) code không đổi => partial update---//
            else {
                // lặp i=0..questionCount-1 => update row i+1
                for (int i=0; i<questionCount; i++) {
                    int cauSo = i+1;
                    String newAns = finalAns.get(i);

                    // Tìm row cũ
                    Answer oldRow = answerRepository.findSingleAnswerSync(
                            examId, oldCode, cauSo);
                    if (oldRow == null) {
                        // row chưa có => insert
                        Answer row = new Answer(examId, oldCode, cauSo, newAns);
                        answerRepository.insertAnswerSync(row);
                    } else {
                        // row có => updateSingleAnswer
                        answerRepository.updateSingleAnswerSync(
                                examId, oldCode, cauSo, newAns);
                    }
                }
                // Cập nhật local list
                current.set(position, new MaDeItem(oldCode, finalAns));
                maDeList.postValue(current);
            }
        }).start();
    }

    //================== REMOVE MA DE ==================//
    public void removeMaDe(int position) {
        new Thread(() -> {
            List<MaDeItem> current = maDeList.getValue();
            if (current == null || position<0 || position>=current.size()) {
                return;
            }
            if (examId<0) return;

            MaDeItem item = current.get(position);
            // Xóa toàn bộ code
            answerRepository.deleteAnswersByCodeSync(examId, item.code);

            current.remove(position);
            maDeList.postValue(current);
        }).start();
    }

    //================== GET ANSWERLIST BY POS ==================//
    public List<String> getAnswerListByPosition(int position) {
        List<MaDeItem> current = maDeList.getValue();
        if (current!=null && position>=0 && position<current.size()) {
            return current.get(position).answers;
        }
        return null;
    }

    //================== HELPER ==================//
    // cắt bớt + thêm null => length=questionCount
    private List<String> ensureSize(List<String> base, int questionCount) {
        List<String> copy = new ArrayList<>(base);
        while (copy.size()>questionCount) {
            copy.remove(copy.size()-1);
        }
        while (copy.size()<questionCount) {
            copy.add(null);
        }
        return copy;
    }

    // thêm/ cập nhật local list
    private void updateLocalList(String code, List<String> finalAns, boolean isAdd) {
        List<MaDeItem> current = maDeList.getValue();
        if (current == null) current = new ArrayList<>();
        if (isAdd) {
            current.add(new MaDeItem(code, finalAns));
        } else {
            // Tìm code cũ -> thay
            for (int i=0; i<current.size(); i++) {
                if (current.get(i).code.equals(code)) {
                    current.set(i, new MaDeItem(code, finalAns));
                    break;
                }
            }
        }
        maDeList.postValue(current);
    }

    //================== CLASS MADEITEM ==================//
    public static class MaDeItem {
        public String code;
        public List<String> answers;
        public MaDeItem(String code, List<String> answers) {
            this.code = code;
            this.answers = answers;
        }
    }
}
