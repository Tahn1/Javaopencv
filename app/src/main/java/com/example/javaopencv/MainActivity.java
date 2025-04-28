package com.example.javaopencv;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {
    private static final int STORAGE_PERMISSION_CODE = 100;

    private DrawerLayout drawerLayout;
    private NavController navController;
    private AppBarConfiguration appBarConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1) Cấp quyền Storage nếu cần
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    STORAGE_PERMISSION_CODE
            );
        }

        // 2) Thiết lập Toolbar (hiển thị title)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Đảm bảo hiển thị tiêu đề do NavController điều khiển
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        // 3) DrawerLayout và NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);

        // 4) Lấy NavController từ NavHostFragment
        NavHostFragment host = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (host == null) {
            throw new IllegalStateException("NavHostFragment không tìm thấy!");
        }
        navController = host.getNavController();

        // 5) Cấu hình top-level destinations
        appBarConfig = new AppBarConfiguration.Builder(
                R.id.kiemTraFragment,
                R.id.giayThiFragment,
                R.id.subjectFragment,
                R.id.classFragment
        )
                .setOpenableLayout(drawerLayout)
                .build();

        // 6) Kết nối Toolbar với NavController
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfig);

        // 7) Kết nối NavigationView với NavController
        NavigationUI.setupWithNavController(navView, navController);

        // 8) Lắng nghe thay đổi destination để update title tự động
        navController.addOnDestinationChangedListener(
                (controller, destination, arguments) -> {
                    if (getSupportActionBar() != null) {
                        CharSequence label = destination.getLabel();
                        getSupportActionBar().setTitle(label != null ? label : "");
                    }
                });
    }

    /** Cho phép Fragment gọi để mở Drawer */
    public void openDrawer() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfig)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null
                && drawerLayout.isDrawerVisible(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,
                        "Đã cấp quyền Storage",
                        Toast.LENGTH_SHORT
                ).show();
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(this)
                        .setTitle("Cần quyền Storage")
                        .setMessage(
                                "Ứng dụng cần quyền Storage để lưu và đọc ảnh chấm bài."
                        )
                        .setPositiveButton("Cho phép", (dlg, which) ->
                                ActivityCompat.requestPermissions(
                                        this,
                                        new String[]{
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                Manifest.permission.READ_EXTERNAL_STORAGE
                                        },
                                        STORAGE_PERMISSION_CODE
                                )
                        )
                        .setNegativeButton("Hủy", null)
                        .show();
            } else {
                Toast.makeText(this,
                        "Không thể lưu ảnh nếu không có quyền Storage",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
