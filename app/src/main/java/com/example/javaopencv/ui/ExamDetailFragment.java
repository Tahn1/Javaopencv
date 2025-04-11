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
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.javaopencv.R;
import java.util.ArrayList;
import java.util.List;

public class ExamDetailFragment extends Fragment {

    private RecyclerView rvMenu;
    private ImageButton btnBack;
    private MenuAdapter adapter;
    private List<MenuItem> menuItems;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate layout fragment_exam_detail.xml
        View view = inflater.inflate(R.layout.fragment_exam_detail, container, false);

        // Ánh xạ View
        btnBack = view.findViewById(R.id.btn_back);
        rvMenu = view.findViewById(R.id.rv_menu);

        // Bắt sự kiện nút back để quay lại
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        // Thiết lập RecyclerView
        rvMenu.setLayoutManager(new LinearLayoutManager(requireContext()));
        initializeMenuItems();

        // Tạo adapter, tích hợp điều hướng khi click vào mục "Đáp án"
        adapter = new MenuAdapter(menuItems, item -> {
            Toast.makeText(requireContext(), "Chọn: " + item.label, Toast.LENGTH_SHORT).show();
            if (item.label.equals("Đáp án")) {
                // Khi người dùng bấm vào "Đáp án" chuyển qua màn hình DapAnFragment
                Bundle bundle = getArguments();  // Chuyển tiếp Bundle từ KiemTraFragment
                NavHostFragment.findNavController(ExamDetailFragment.this)
                        .navigate(R.id.action_examDetailFragment_to_dapAnFragment, bundle);
            }
            // Xử lý các mục khác nếu cần...
        });
        rvMenu.setAdapter(adapter);

        return view;
    }

    private void initializeMenuItems() {
        menuItems = new ArrayList<>();
        menuItems.add(new MenuItem("1", "Đáp án", "DapAn", "key"));
        menuItems.add(new MenuItem("2", "Chấm bài", "ChamBai", "camera"));
        menuItems.add(new MenuItem("3", "Xem lại", "XemLai", "chatbox"));
        menuItems.add(new MenuItem("4", "Thống kê", "ThongKe", "chart"));
        menuItems.add(new MenuItem("5", "Thông tin", "ThongTin", "info"));
    }

    public static class MenuItem {
        public String id;
        public String label;
        public String screen;
        public String icon;
        public MenuItem(String id, String label, String screen, String icon) {
            this.id = id;
            this.label = label;
            this.screen = screen;
            this.icon = icon;
        }
    }

    private class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {
        private List<MenuItem> items;
        private OnMenuItemClickListener listener;
        public MenuAdapter(List<MenuItem> items, OnMenuItemClickListener listener) {
            this.items = items;
            this.listener = listener;
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
            holder.ivIcon.setImageResource(getIconResource(item.icon));
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMenuItemClick(item);
                }
            });
        }
        @Override
        public int getItemCount() {
            return items.size();
        }
        private int getIconResource(String iconName) {
            switch (iconName) {
                case "key":
                    return R.drawable.ic_key;
                case "camera":
                    return R.drawable.ic_camera;
                case "chatbox":
                    return R.drawable.ic_chatbox;
                case "chart":
                    return R.drawable.ic_bar_chart;
                case "info":
                    return R.drawable.ic_information;
                default:
                    return R.drawable.ic_information;
            }
        }
        class MenuViewHolder extends RecyclerView.ViewHolder {
            android.widget.ImageView ivIcon, ivChevron;
            android.widget.TextView tvLabel;
            public MenuViewHolder(@NonNull View itemView) {
                super(itemView);
                ivIcon = itemView.findViewById(R.id.iv_menu_icon);
                ivChevron = itemView.findViewById(R.id.iv_chevron);
                tvLabel = itemView.findViewById(R.id.tv_menu_label);
            }
        }
    }
    private interface OnMenuItemClickListener {
        void onMenuItemClick(MenuItem item);
    }
}
