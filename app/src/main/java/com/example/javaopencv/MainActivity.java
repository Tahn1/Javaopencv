package com.example.javaopencv;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.javaopencv.ui.KiemTraFragment;

public class MainActivity extends AppCompatActivity {

    // Nếu sử dụng Toolbar được định nghĩa trong activity_main.xml
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Nếu muốn sử dụng Toolbar từ layout activity_main.xml:
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Load Fragment vào container
        // MainActivity.java
        if (savedInstanceState == null) {
//            Fragment fragment = new KiemTraFragment();
//            FragmentManager fm = getSupportFragmentManager();
//            FragmentTransaction ft = fm.beginTransaction();
//            ft.replace(R.id.container_fragment, fragment);
//            ft.commit();
        }

    }
}

