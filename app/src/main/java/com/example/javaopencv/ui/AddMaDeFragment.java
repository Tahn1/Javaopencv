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
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.javaopencv.R;
import com.example.javaopencv.ui.adapter.MaDeViewPagerAdapter;
import com.example.javaopencv.viewmodel.DapAnViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

public class AddMaDeFragment extends Fragment {

    private ViewPager2 viewPager;
    private MaDeViewPagerAdapter viewPagerAdapter;
    private ImageButton btnBack, btnSave;
    private DapAnViewModel viewModel;

    private String maDeToEdit = null;    // ✅ Mã đề đang chỉnh sửa
    private int positionToEdit = -1;      // ✅ Vị trí mã đề
    private List<String> oldAnswerList = null; // ✅ Đáp án cũ

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_ma_de, container, false);

        btnBack = view.findViewById(R.id.btn_back);
        btnSave = view.findViewById(R.id.btn_save);
        viewPager = view.findViewById(R.id.view_pager);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);

        viewPagerAdapter = new MaDeViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Mã đề" : "Đáp án");
        }).attach();

        viewModel = new ViewModelProvider(requireActivity()).get(DapAnViewModel.class);

        Bundle args = getArguments();
        if (args != null) {
            maDeToEdit = args.getString("maDeToEdit", null);
            positionToEdit = args.getInt("positionToEdit", -1);
            oldAnswerList = args.getStringArrayList("oldAnswerList"); // lấy đáp án cũ nếu có
        }

        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        btnSave.setOnClickListener(v -> saveMaDe());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // ✅ Đảm bảo ViewPager2 đã load xong trước khi lấy Fragment con
        viewPager.post(() -> {
            MaDeTabFragment maDeTabFragment = viewPagerAdapter.getMaDeTabFragment();
            DapAnTabFragment dapAnTabFragment = viewPagerAdapter.getDapAnTabFragment();

            if (maDeTabFragment != null && maDeToEdit != null) {
                maDeTabFragment.setSelectedMaDe(maDeToEdit);  // 👉 Highlight lại Mã đề
            }

            if (dapAnTabFragment != null && positionToEdit != -1) {
                List<String> answerList = viewModel.getAnswerListByPosition(positionToEdit);
                if (answerList != null) {
                    dapAnTabFragment.setAnswerListToEdit(answerList);  // 👉 Highlight lại đáp án
                }
            }
        });
    }

    private void saveMaDe() {
        MaDeTabFragment maDeTabFragment = viewPagerAdapter.getMaDeTabFragment();
        DapAnTabFragment dapAnTabFragment = viewPagerAdapter.getDapAnTabFragment();

        if (maDeTabFragment == null || dapAnTabFragment == null) return;

        String maDe = maDeTabFragment.getMaDe();
        List<String> answers = dapAnTabFragment.getAnswerList(); // ✅ Đáp án có thể null hoặc rỗng cũng được

        if (maDe == null || maDe.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập mã đề", Toast.LENGTH_SHORT).show();
            return;
        }

        if (maDeToEdit != null && positionToEdit != -1) {
            // 👉 Nếu đang sửa, hỏi xác nhận
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Xác nhận")
                    .setMessage("Bạn có chắc chắn muốn thay thế mã đề này không?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        viewModel.updateMaDe(positionToEdit, maDe, answers);
                        requireActivity().onBackPressed();
                    })
                    .setNegativeButton("Không", null)
                    .show();
        } else {
            // 👉 Nếu đang thêm mới, kiểm tra không được trùng mã đề
            List<DapAnViewModel.MaDeItem> currentList = viewModel.getMaDeList().getValue();
            if (currentList != null) {
                boolean exists = false;
                for (DapAnViewModel.MaDeItem item : currentList) {
                    if (item.maDe.equals(maDe)) {
                        exists = true;
                        break;
                    }
                }
                if (exists) {
                    Toast.makeText(getContext(), "Mã đề đã tồn tại. Vui lòng nhập mã đề khác.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Nếu không trùng, thêm bình thường
            viewModel.addMaDe(maDe, answers);
            requireActivity().onBackPressed();
        }
    }
}