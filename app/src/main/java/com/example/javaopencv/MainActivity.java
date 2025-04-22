package com.example.javaopencv;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

public class MainActivity extends AppCompatActivity {
    private static final int STORAGE_PERMISSION_CODE = 100;

    // Tham chiếu tới DrawerLayout
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1) Yêu cầu quyền Storage nếu chưa có
        checkStoragePermission();

        // 2) Thiết lập NavController
        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment == null) {
            throw new IllegalStateException(
                    "NavHostFragment không tìm thấy. Kiểm tra layout của MainActivity."
            );
        }
        NavController navController = navHostFragment.getNavController();
        // Nếu bạn dùng Drawer + NavigationUI, thường sẽ có:
        // NavigationUI.setupWithNavController(navView, navController);

        // 3) Vô hiệu hóa cử chỉ vuốt mép trái để mở Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        if (drawerLayout != null) {
            drawerLayout.setDrawerLockMode(
                    DrawerLayout.LOCK_MODE_LOCKED_CLOSED,
                    GravityCompat.START
            );
        }
    }

    /**
     * Cho phép Fragment gọi mở Drawer bằng code.
     * Ví dụ trong KiemTraFragment: ((MainActivity)getActivity()).openDrawer();
     */
    public void openDrawer() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    STORAGE_PERMISSION_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Đã cấp quyền Storage", Toast.LENGTH_SHORT).show();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Cần quyền Storage")
                            .setMessage("Ứng dụng cần quyền Storage để lưu và đọc ảnh chấm bài.")
                            .setPositiveButton("Cho phép", (dlg, which) ->
                                    checkStoragePermission()
                            )
                            .setNegativeButton("Huỷ", null)
                            .show();
                } else {
                    Toast.makeText(this,
                            "Không thể lưu ảnh nếu không có quyền Storage",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        // Nếu Drawer đang mở, đóng nó trước khi thoát
        if (drawerLayout != null
                && drawerLayout.isDrawerVisible(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
