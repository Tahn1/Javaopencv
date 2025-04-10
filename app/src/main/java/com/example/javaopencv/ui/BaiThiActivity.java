package com.example.javaopencv.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.Exam;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class BaiThiActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvHeaderTitle;
    private RecyclerView rvMenu;
    private MenuAdapter menuAdapter;

    // Giả sử các thông tin exam được truyền qua intent extras
    private int examId;
    private String examTitle;
    private String examPhieu;
    private int examSoCau;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bai_thi);

        // Lấy thông tin exam từ Intent extras (nếu có)
        examId = getIntent().getIntExtra("examId", 0);
        examTitle = getIntent().getStringExtra("title");
        examPhieu = getIntent().getStringExtra("phieu");
        examSoCau = getIntent().getIntExtra("soCau", 0);

        btnBack = findViewById(R.id.btn_back);
        tvHeaderTitle = findViewById(R.id.tv_header_title);
        rvMenu = findViewById(R.id.rv_menu);

        // Đặt tiêu đề (bạn có thể cập nhật thêm thông tin exam nếu cần)
        tvHeaderTitle.setText("Bài thi");

        // Nút back
        btnBack.setOnClickListener(v -> finish());

        // Thiết lập RecyclerView với danh sách các lựa chọn
        rvMenu.setLayoutManager(new LinearLayoutManager(this));
        menuAdapter = new MenuAdapter(getMenuItems());
        rvMenu.setAdapter(menuAdapter);
    }

    /**
     * Tạo danh sách các lựa chọn menu (ví dụ theo React Native code bạn gửi)
     */
    private List<MenuItem> getMenuItems() {
        List<MenuItem> list = new ArrayList<>();
        // Lưu ý: Bạn cần có các file drawable cho icon (ví dụ: ic_key, ic_camera, ic_chatbox, ic_bar_chart, ic_info)
        list.add(new MenuItem("Đáp án", "DapAn", R.drawable.ic_key));
        list.add(new MenuItem("Chấm bài", "ChamBai", R.drawable.ic_camera));
        list.add(new MenuItem("Xem lại", "XemLai", R.drawable.ic_chatbox));
        list.add(new MenuItem("Thống kê", "ThongKe", R.drawable.ic_bar_chart));
        list.add(new MenuItem("Thông tin", "ThongTin", R.drawable.ic_info));
        return list;
    }

    /**
     * Mô hình cho các mục menu.
     */
    public class MenuItem {
        public String label;
        public String screen;  // Có thể dùng để quyết định chuyển màn hình
        public int iconRes;

        public MenuItem(String label, String screen, int iconRes) {
            this.label = label;
            this.screen = screen;
            this.iconRes = iconRes;
        }
    }

    /**
     * Adapter hiển thị danh sách các lựa chọn menu.
     */
    public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

        private List<MenuItem> menuItems;

        public MenuAdapter(List<MenuItem> menuItems) {
            this.menuItems = menuItems;
        }

        @NonNull
        @Override
        public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu, parent, false);
            return new MenuViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
            MenuItem item = menuItems.get(position);
            holder.tvLabel.setText(item.label);
            // Nếu cần, thiết lập icon cho ImageButton (nếu có) tại đây.
        }

        @Override
        public int getItemCount() {
            return menuItems.size();
        }

        class MenuViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView tvLabel;
            // Bạn có thể khai báo ImageButton cho icon nếu có.

            public MenuViewHolder(@NonNull View itemView) {
                super(itemView);
                tvLabel = itemView.findViewById(R.id.tv_menu_label);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    MenuItem item = menuItems.get(pos);
                    // Ở đây bạn có thể chuyển sang màn hình được chỉ định theo item.screen
                    // Ví dụ: chuyển sang activity tương ứng, hoặc hiển thị dialog với các chức năng.
                    // Hiện tại, ta chỉ hiển thị Toast cho demo:
                    Toast.makeText(BaiThiActivity.this,
                            "Option: " + item.label +
                                    "\nExam: " + examTitle +
                                    "\nPhieu: " + examPhieu +
                                    "\nSố câu: " + examSoCau,
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
