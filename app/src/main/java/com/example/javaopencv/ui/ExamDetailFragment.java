package com.example.javaopencv.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private MenuAdapter adapter;
    private List<MenuItem> menuItems;
    private int examId = -1;
    private int questionCount = 20;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_exam_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Nhận args
        Bundle args = getArguments();
        if (args != null) {
            examId = args.getInt("examId", -1);
            questionCount = args.getInt("questionCount", 20);
        }

        // 1) RecyclerView
        rvMenu = view.findViewById(R.id.rv_menu);
        rvMenu.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 2) Chuẩn bị dữ liệu menu
        initializeMenuItems();

        // 3) Adapter
        adapter = new MenuAdapter(menuItems, item -> {
            Bundle bundle = new Bundle();
            bundle.putInt("examId", examId);
            bundle.putInt("questionCount", questionCount);
            switch (item.label) {
                case "Đáp án":
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_examDetailFragment_to_dapAnFragment, bundle);
                    break;
                case "Chấm bài":
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_examDetailFragment_to_chamBaiFragment, bundle);
                    break;
                case "Xem lại":
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_examDetailFragment_to_xemLaiFragment, bundle);
                    break;
                case "Thống kê":
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_examDetailFragment_to_thongKeFragment, bundle);
                    break;
                case "Danh sách học sinh":  // xử lý mục mới
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_examDetailFragment_to_studentListFragment, bundle);
                    break;
                case "Thông tin":
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_examDetailFragment_to_thongTinFragment, bundle);
                    break;
                default:
                    Toast.makeText(requireContext(),
                            "Chọn: " + item.label, Toast.LENGTH_SHORT).show();
            }
        });
        rvMenu.setAdapter(adapter);
    }

    private void initializeMenuItems() {
        menuItems = new ArrayList<>();
        menuItems.add(new MenuItem("1", "Đáp án",      "DapAn",            "ic_key"));
        menuItems.add(new MenuItem("2", "Chấm bài",    "ChamBai",          "ic_camera"));
        menuItems.add(new MenuItem("3", "Xem lại",      "XemLai",           "ic_chatbox"));
        menuItems.add(new MenuItem("4", "Thống kê",     "ThongKe",          "ic_bar_chart"));
        menuItems.add(new MenuItem("5", "Danh sách học sinh", "StudentList", "ic_students"));  // thêm ở đây
        menuItems.add(new MenuItem("6", "Thông tin",    "ThongTin",         "ic_information"));
    }

    public static class MenuItem {
        public final String id;
        public final String label;
        public final String screen;
        public final String icon;
        public MenuItem(String id, String label, String screen, String icon) {
            this.id = id;
            this.label = label;
            this.screen = screen;
            this.icon = icon;
        }
    }

    private class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {
        private final List<MenuItem> items;
        private final OnMenuItemClickListener listener;
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
            MenuItem item = items.get(position);
            holder.tvLabel.setText(item.label);
            int resId = holder.itemView.getContext().getResources()
                    .getIdentifier(item.icon, "drawable",
                            holder.itemView.getContext().getPackageName());
            if (resId != 0) holder.ivIcon.setImageResource(resId);

            holder.itemView.setOnClickListener(v -> listener.onMenuItemClick(item));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class MenuViewHolder extends RecyclerView.ViewHolder {
            android.widget.ImageView ivIcon, ivChevron;
            android.widget.TextView tvLabel;
            MenuViewHolder(@NonNull View itemView) {
                super(itemView);
                ivIcon    = itemView.findViewById(R.id.iv_menu_icon);
                ivChevron = itemView.findViewById(R.id.iv_chevron);
                tvLabel   = itemView.findViewById(R.id.tv_menu_label);
            }
        }
    }

    private interface OnMenuItemClickListener {
        void onMenuItemClick(MenuItem item);
    }
}
