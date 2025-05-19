package com.example.javaopencv;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
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

        // 1) Xin quyền READ + WRITE external storage
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
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

        // 2) Thiết lập Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        // 3) DrawerLayout & NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);

        // 4) NavController từ NavHostFragment
        NavHostFragment host = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (host == null) {
            throw new IllegalStateException("NavHostFragment không tìm thấy!");
        }
        navController = host.getNavController();

        // 5) Cấu hình top-level destinations (đã loại bỏ subjectFragment)
        appBarConfig = new AppBarConfiguration.Builder(
                R.id.kiemTraFragment,
                R.id.giayThiFragment,
                R.id.classFragment
        )
                .setOpenableLayout(drawerLayout)
                .build();

        // 6) Kết nối Toolbar với NavController
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfig);

        // 7) Kết nối NavigationView với NavController
        NavigationUI.setupWithNavController(navView, navController);

        // 8) Đổi title/subtitle khi chuyển màn
        navController.addOnDestinationChangedListener((controller, destination, args) -> {
            ActionBar ab = getSupportActionBar();
            if (ab == null) return;

            int destId = destination.getId();
            if (destId == R.id.gradeDetailFragment) {
                ab.setTitle("");
                ab.setSubtitle("");
            } else {
                ab.setSubtitle("");
                CharSequence label = destination.getLabel();
                ab.setTitle(label != null ? label : "");
            }
        });
    }

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
            boolean grantedAll = true;
            if (grantResults.length > 0) {
                for (int r : grantResults) {
                    if (r != PackageManager.PERMISSION_GRANTED) {
                        grantedAll = false;
                        break;
                    }
                }
            } else grantedAll = false;

            if (grantedAll) {
                Toast.makeText(this,
                        "Đã cấp quyền Storage",
                        Toast.LENGTH_SHORT
                ).show();
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Nếu user từ chối nhưng chưa chọn "Không hỏi lại"
                new AlertDialog.Builder(this)
                        .setTitle("Cần quyền Storage")
                        .setMessage("Ứng dụng cần quyền Storage để lưu và đọc ảnh chấm bài.")
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
