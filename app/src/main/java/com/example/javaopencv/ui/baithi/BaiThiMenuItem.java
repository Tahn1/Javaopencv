package com.example.javaopencv.ui.baithi;

public class BaiThiMenuItem {
    public int id;             // ví dụ: 1
    public String label;       // ví dụ: "Đáp án"
    public int iconRes;        // ví dụ: R.drawable.ic_key
    public String screenName;  // ví dụ: "DapAn" (nếu bạn muốn xử lý theo tên)

    public BaiThiMenuItem(int id, String label, int iconRes, String screenName) {
        this.id = id;
        this.label = label;
        this.iconRes = iconRes;
        this.screenName = screenName;
    }
}
