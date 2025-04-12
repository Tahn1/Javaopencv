package com.example.javaopencv.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.javaopencv.data.entity.Answer;
import com.example.javaopencv.repository.AnswerRepository;
import java.util.ArrayList;
import java.util.List;

public class DapAnViewModel extends AndroidViewModel {

    private final MutableLiveData<List<MaDeItem>> maDeList = new MutableLiveData<>(new ArrayList<>());
    private AnswerRepository answerRepository;

    // examId hiện hành của bài thi; giá trị mặc định là -1 (không hợp lệ)
    private int examId = -1;

    public DapAnViewModel(@NonNull Application application) {
        super(application);
        // Khởi tạo AnswerRepository với Application context
        answerRepository = AnswerRepository.getInstance(application);
        // Không gọi loadMaDeList tại đây vì examId chưa được xác định
    }

    // Setter để cập nhật examId từ Activity/Fragment
    public void setExamId(int examId) {
        this.examId = examId;
        // Tải lại danh sách mã đề của bài thi này từ database
        loadMaDeList(examId);
    }

    // Getter cho examId
    public int getExamId() {
        return examId;
    }

    public LiveData<List<MaDeItem>> getMaDeList() {
        return maDeList;
    }

    // Load danh sách mã đề (distinct codes) từ bảng Answer cho bài thi hiện hành
    public void loadMaDeList(final int examId) {
        answerRepository.getDistinctCodes(examId, new AnswerRepository.LoadCallback() {
            @Override
            public void onLoad(List<String> codes) {
                List<MaDeItem> items = new ArrayList<>();
                for (String code : codes) {
                    // Nếu cần, có thể tải thêm danh sách đáp án tương ứng cho mã đề đó
                    items.add(new MaDeItem(code, new ArrayList<>()));
                }
                // postValue cập nhật LiveData từ background thread
                maDeList.postValue(items);
            }
            @Override
            public void onError(Exception e) {
                android.util.Log.e("DapAnViewModel", "Error loading distinct codes", e);
            }
        });
    }

    // Thêm mã đề và danh sách đáp án, sử dụng examId đã set
    public void addMaDe(String maDe, List<String> answerList) {
        android.util.Log.d("DapAnViewModel", "Adding maDe: " + maDe + " with answers: " + answerList);
        List<MaDeItem> list = maDeList.getValue();
        if (list != null) {
            list.add(new MaDeItem(maDe, answerList));
            maDeList.setValue(list);
        }
        if (examId == -1) {
            android.util.Log.e("DapAnViewModel", "ExamId chưa được set!");
            return;
        }
        for (int i = 0; i < answerList.size(); i++) {
            int cauSo = i + 1;
            String dapAn = answerList.get(i);
            Answer answer = new Answer(examId, maDe, cauSo, dapAn);
            android.util.Log.d("DapAnViewModel", "Inserting answer: " + answer.dapAn);
            answerRepository.insertAnswer(answer);
        }
    }

    // Cập nhật mã đề: xóa dữ liệu cũ của mã đề cũ và thêm mới cho bài thi hiện hành
    public void updateMaDe(int position, String newMaDe, List<String> newAnswerList) {
        List<MaDeItem> list = maDeList.getValue();
        if (list != null && position >= 0 && position < list.size()) {
            MaDeItem oldItem = list.get(position);
            if (examId == -1) {
                android.util.Log.e("DapAnViewModel", "ExamId chưa được set!");
                return;
            }
            answerRepository.deleteAnswersByCode(examId, oldItem.maDe);
            list.set(position, new MaDeItem(newMaDe, newAnswerList));
            maDeList.setValue(list);
            for (int i = 0; i < newAnswerList.size(); i++) {
                int cauSo = i + 1;
                String dapAn = newAnswerList.get(i);
                Answer answer = new Answer(examId, newMaDe, cauSo, dapAn);
                answerRepository.insertAnswer(answer);
            }
        }
    }

    // Xóa mã đề cho bài thi hiện hành
    public void removeMaDe(int position) {
        List<MaDeItem> list = maDeList.getValue();
        if (list != null && position >= 0 && position < list.size()) {
            MaDeItem item = list.get(position);
            if (examId == -1) {
                android.util.Log.e("DapAnViewModel", "ExamId chưa được set!");
                return;
            }
            answerRepository.deleteAnswersByCode(examId, item.maDe);
            list.remove(position);
            maDeList.setValue(list);
        }
    }

    // Lấy danh sách đáp án của mã đề theo vị trí
    public List<String> getAnswerListByPosition(int position) {
        List<MaDeItem> list = maDeList.getValue();
        if (list != null && position >= 0 && position < list.size()) {
            return list.get(position).answerList;
        }
        return null;
    }

    // Lớp MaDeItem: chứa mã đề và danh sách đáp án
    public static class MaDeItem {
        public String maDe;
        public List<String> answerList;
        public MaDeItem(String maDe, List<String> answerList) {
            this.maDe = maDe;
            this.answerList = answerList;
        }
    }
}
