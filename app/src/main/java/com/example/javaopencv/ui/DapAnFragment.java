package com.example.javaopencv.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;
import com.example.javaopencv.ui.adapter.ExamCodeAdapter;
import com.example.javaopencv.viewmodel.DapAnViewModel;

import java.util.ArrayList;
import java.util.List;

public class DapAnFragment extends Fragment {

    private ImageButton btnBack, btnCamera, btnAdd;
    private TextView tvNoExamCode;
    private RecyclerView recyclerView;
    private ExamCodeAdapter adapter;
    private DapAnViewModel viewModel;

    // THÊM: Số câu hỏi, mặc định 20, sẽ được cập nhật từ Bundle nếu có
    private int questionCount = 20;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dap_an, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnBack = view.findViewById(R.id.btn_back);
        btnCamera = view.findViewById(R.id.btn_camera);
        btnAdd = view.findViewById(R.id.btn_add);
        tvNoExamCode = view.findViewById(R.id.tv_no_exam_code);
        recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ExamCodeAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(requireActivity()).get(DapAnViewModel.class);

        // THÊM: Lấy questionCount từ Bundle nếu có (được truyền từ ExamDetailFragment)
        Bundle args = getArguments();
        if (args != null && args.containsKey("questionCount")) {
            questionCount = args.getInt("questionCount");
        }

        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        btnAdd.setOnClickListener(v -> {
            // Bấm nút thêm → mở AddMaDeFragment (tạo mới)
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            AddMaDeFragment fragment = new AddMaDeFragment();
            // CHỈ LẠI: truyền examId và questionCount qua Bundle
            Bundle bundle = new Bundle();
            // Giả sử examId đã được thiết lập trong ViewModel
            // Và questionCount là biến trong DapAnFragment (đã lấy từ Bundle trước đó)
            bundle.putInt("examId", viewModel != null ? viewModel.getExamId() : -1);
            bundle.putInt("questionCount", questionCount);
            fragment.setArguments(bundle);
            transaction.replace(R.id.nav_host_fragment, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        btnCamera.setOnClickListener(v -> {
            // TODO: mở Camera
        });

        // Lắng nghe LiveData thay đổi danh sách mã đề
        viewModel.getMaDeList().observe(getViewLifecycleOwner(), maDeItemList -> {
            List<String> maDeStrings = new ArrayList<>();
            for (DapAnViewModel.MaDeItem item : maDeItemList) {
                maDeStrings.add(item.maDe);
            }
            adapter.updateData(maDeStrings);
            updateUI(maDeStrings);
        });

        // Xử lý click vào mã đề
        adapter.setOnExamCodeClickListener(new ExamCodeAdapter.OnExamCodeClickListener() {
            @Override
            public void onExamCodeClick(int position, String maDe) {
                // Tạo Bundle chứa Mã đề, vị trí và số câu
                Bundle bundle = new Bundle();
                bundle.putString("maDeToEdit", maDe);
                bundle.putInt("positionToEdit", position);
                // Thêm số câu vào Bundle (đảm bảo giá trị questionCount đã được thiết lập trong DapAnFragment)
                bundle.putInt("questionCount", questionCount);

                // ✅ Lấy oldAnswerList từ ViewModel nếu có
                List<String> oldAnswerList = viewModel.getAnswerListByPosition(position);
                if (oldAnswerList != null) {
                    bundle.putStringArrayList("oldAnswerList", new ArrayList<>(oldAnswerList));
                }

                AddMaDeFragment fragment = new AddMaDeFragment();
                fragment.setArguments(bundle);

                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.nav_host_fragment, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }

            @Override
            public void onExamCodeLongClick(int position, String maDe) {
                viewModel.removeMaDe(position);
            }
        });
    }

    private void updateUI(List<String> maDeList) {
        if (maDeList == null || maDeList.isEmpty()) {
            tvNoExamCode.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvNoExamCode.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
