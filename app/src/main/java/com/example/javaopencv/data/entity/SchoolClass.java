package com.example.javaopencv.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity(tableName = "classes")
public class SchoolClass {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    @NonNull
    @ColumnInfo(name = "dateCreated")
    private String dateCreated;

    /**
     * Constructor cho Room khi đọc dữ liệu (có đủ 3 trường).
     */
    public SchoolClass(int id, @NonNull String name, @NonNull String dateCreated) {
        this.id = id;
        this.name = name;
        this.dateCreated = dateCreated;
    }

    /**
     * Constructor tiện lợi khi tạo mới: chỉ cần tên, ngày tạo tự sinh.
     * Room sẽ bỏ qua constructor này vì có @Ignore.
     */
    @Ignore
    public SchoolClass(@NonNull String name) {
        this.name = name;
        this.dateCreated = new SimpleDateFormat("d/M/yyyy", Locale.getDefault())
                .format(new Date());
    }

    // ----- Getters & Setters -----

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(@NonNull String dateCreated) {
        this.dateCreated = dateCreated;
    }
}
