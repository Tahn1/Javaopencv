package com.example.javaopencv;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.example.javaopencv.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // Sử dụng activity_main.xml ở trên

        // Lấy NavHostFragment từ SupportFragmentManager bằng ID mà bạn đã khai báo
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment == null) {
            throw new IllegalStateException("NavHostFragment không tìm thấy. Kiểm tra layout của MainActivity.");
        }
        NavController navController = navHostFragment.getNavController();

        // Nếu bạn sử dụng Drawer Navigation, có thể setup NavigationUI cho NavigationView
        // NavigationUI.setupWithNavController(navView, navController);
    }
}
