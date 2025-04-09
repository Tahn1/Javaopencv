// MainActivity.java
package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // XML chứa DrawerLayout

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Cấu hình Toggle cho Drawer
        toggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView.setNavigationItemSelectedListener(this);

        // Khởi chạy màn hình mặc định (ví dụ, KiemTraActivity)
        if (savedInstanceState == null) {
            // Thay vì dùng fragment, bạn cũng có thể startActivity ở đây
            startActivity(new Intent(this, KiemTraActivity.class));
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(toggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Xử lý các mục trong Drawer
        switch (item.getItemId()){
            case R.id.nav_kiemtra:
                startActivity(new Intent(this, KiemTraActivity.class));
                break;
            case R.id.nav_giaythi:
                startActivity(new Intent(this, GiayThiActivity.class));
                break;
            // Thêm các mục khác nếu cần
        }
        drawerLayout.closeDrawers();
        return true;
    }
}
