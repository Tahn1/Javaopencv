package com.example.javaopencv.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.javaopencv.R;

public class GiayThiFragment extends Fragment {

    private static final String DRIVE_URL_PHIEU_20     = "https://drive.google.com/file/d/14cYBbLpQmaxVuOjcGjKRAfxdKEw1jggx/view";
    private static final String DRIVE_URL_EXCEL_MAU    = "https://drive.google.com/drive/folders/1w7csyZA_tKK2yy9SLG-8u7KMdrLvGU6S?usp=sharing";

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_giay_thi, container, false);

        // Tải phiếu 20
        view.findViewById(R.id.btn_tai_phieu_20)
                .setOnClickListener(v ->
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(DRIVE_URL_PHIEU_20)))
                );

        // Tải file Excel mẫu
        view.findViewById(R.id.btn_tai_excel_mau)
                .setOnClickListener(v ->
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(DRIVE_URL_EXCEL_MAU)))
                );

        return view;
    }
}
