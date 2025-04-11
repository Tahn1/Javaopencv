package com.example.javaopencv.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class DapAnViewModel extends ViewModel {

    private final MutableLiveData<List<MaDeItem>> maDeList = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<MaDeItem>> getMaDeList() {
        return maDeList;
    }

    // ✅ Thêm mã đề mới (có cả mã đề và đáp án)
    public void addMaDe(String maDe, List<String> answerList) {
        List<MaDeItem> list = maDeList.getValue();
        if (list != null) {
            list.add(new MaDeItem(maDe, answerList));
            maDeList.setValue(list);
        }
    }

    // ✅ Cập nhật mã đề và đáp án mới
    public void updateMaDe(int position, String newMaDe, List<String> newAnswerList) {
        List<MaDeItem> list = maDeList.getValue();
        if (list != null && position >= 0 && position < list.size()) {
            list.set(position, new MaDeItem(newMaDe, newAnswerList));
            maDeList.setValue(list);
        }
    }

    // ✅ Xóa mã đề
    public void removeMaDe(int position) {
        List<MaDeItem> list = maDeList.getValue();
        if (list != null && position >= 0 && position < list.size()) {
            list.remove(position);
            maDeList.setValue(list);
        }
    }

    // ✅ Lấy danh sách đáp án của mã đề theo vị trí
    public List<String> getAnswerListByPosition(int position) {
        List<MaDeItem> list = maDeList.getValue();
        if (list != null && position >= 0 && position < list.size()) {
            return list.get(position).answerList;
        }
        return null;
    }

    // ✅ Định nghĩa lớp MaDeItem (gồm mã đề và danh sách đáp án)
    public static class MaDeItem {
        public String maDe;
        public List<String> answerList;

        public MaDeItem(String maDe, List<String> answerList) {
            this.maDe = maDe;
            this.answerList = answerList;
        }
    }
}
