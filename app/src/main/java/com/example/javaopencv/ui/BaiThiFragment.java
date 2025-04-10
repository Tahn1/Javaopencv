package com.example.javaopencv.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;

import java.util.ArrayList;
import java.util.List;

public class BaiThiFragment extends Fragment {

    private RecyclerView rvMenu;
    private MenuAdapter adapter;

    // Các parameter truyền từ màn hình trước (ví dụ, từ KiemTraScreen)
    private String examId;
    private String examTitle;
    private String phieu;
    private String soCau;

    // Danh sách menu items (theo mã React Native)
    private List<MenuItem> menuItems;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_baithi, container, false);

        // Lấy các giá trị truyền qua Bundle (nếu có)
        Bundle args = getArguments();
        if (args != null) {
            examId = args.getString("examId");
            examTitle = args.getString("title");
            phieu = args.getString("phieu");
            soCau = args.getString("soCau");
        }

        // Cài đặt nút back trong header
        ImageButton btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requireActivity().onBackPressed();
                }
            });
        }

        // Thiết lập RecyclerView
        rvMenu = view.findViewById(R.id.rv_menu);
        rvMenu.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khởi tạo danh sách menu và adapter
        initializeMenuItems();
        adapter = new MenuAdapter(menuItems);
        rvMenu.setAdapter(adapter);

        return view;
    }

    private void initializeMenuItems() {
        menuItems = new ArrayList<>();
        menuItems.add(new MenuItem("1", "Đáp án", "DapAn", "key"));
        menuItems.add(new MenuItem("2", "Chấm bài", "ChamBai", "camera"));
        menuItems.add(new MenuItem("3", "Xem lại", "XemLai", "chatbox-ellipses"));
        menuItems.add(new MenuItem("4", "Thống kê", "ThongKe", "bar-chart"));
        menuItems.add(new MenuItem("5", "Thông tin", "ThongTin", "information-circle"));
    }

    // Lớp model cho menu item
    public static class MenuItem {
        public String id;
        public String label;
        public String screen; // tên màn hình đích hoặc identifier để điều hướng
        public String icon;

        public MenuItem(String id, String label, String screen, String icon) {
            this.id = id;
            this.label = label;
            this.screen = screen;
            this.icon = icon;
        }
    }

    // Adapter cho RecyclerView
    private class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

        private List<MenuItem> items;

        public MenuAdapter(List<MenuItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_menu, parent, false);
            return new MenuViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
            final MenuItem item = items.get(position);
            holder.tvLabel.setText(item.label);
            // Nếu bạn có hỗ trợ icon, có thể set icon ở đây, ví dụ:
            // holder.ivIcon.setImageResource(getIconResourceId(item.icon));
            // Xử lý click: Ví dụ chuyển màn hình bằng Toast hoặc gọi phương thức điều hướng
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Ví dụ: Hiển thị Toast và chuyển sang màn hình đích
                    Toast.makeText(getContext(), "Chuyển đến: " + item.screen, Toast.LENGTH_SHORT).show();

                    // TODO: Sử dụng Navigation Component để chuyển (nếu đã được cấu hình)
                    // Bundle bundle = new Bundle();
                    // bundle.putString("examId", examId);
                    // bundle.putString("title", examTitle);
                    // bundle.putString("phieu", phieu);
                    // bundle.putString("soCau", soCau);
                    // NavHostFragment.findNavController(BaiThiFragment.this)
                    //      .navigate(R.id.action_baiThiFragment_to_targetFragment, bundle);
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class MenuViewHolder extends RecyclerView.ViewHolder {
            android.widget.TextView tvLabel;
            // Nếu muốn thêm hình icon và chevron, khai báo các view tương ứng:
            // ImageView ivIcon, ivChevron;

            public MenuViewHolder(@NonNull View itemView) {
                super(itemView);
                tvLabel = itemView.findViewById(R.id.tv_menu_label);
                // ivIcon = itemView.findViewById(R.id.iv_menu_icon);
                // ivChevron = itemView.findViewById(R.id.iv_chevron);
            }
        }
    }
}
