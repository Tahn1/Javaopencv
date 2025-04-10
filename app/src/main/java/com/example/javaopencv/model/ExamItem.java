// ExamItem.java
package com.example.javaopencv.model;

public class ExamItem {
    private long id;
    private String title;
    private String phieu;
    private int soCau;
    private String date;

    public ExamItem(long id, String title, String phieu, int soCau, String date) {
        this.id = id;
        this.title = title;
        this.phieu = phieu;
        this.soCau = soCau;
        this.date = date;
    }

    // Getters & Setters

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPhieu() {
        return phieu;
    }

    public void setPhieu(String phieu) {
        this.phieu = phieu;
    }

    public int getSoCau() {
        return soCau;
    }

    public void setSoCau(int soCau) {
        this.soCau = soCau;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
