package com.example.javaopencv.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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

    private TextView      tvNoExamCode;
    private RecyclerView  recyclerView;
    private ExamCodeAdapter adapter;
    private DapAnViewModel   viewModel;
    private int questionCount = 20;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Thông báo fragment có menu (chỉ Add)
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate chỉ phần nội dung, đã bỏ header khỏi XML
        return inflater.inflate(R.layout.fragment_dap_an, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvNoExamCode = view.findViewById(R.id.tv_no_exam_code);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ExamCodeAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(requireActivity())
                .get(DapAnViewModel.class);

        // Lấy questionCount và examId từ args
        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey("questionCount")) {
                questionCount = args.getInt("questionCount");
                Log.d("DapAnFragment", "Question count: " + questionCount);
            }
            if (args.containsKey("examId")) {
                viewModel.setExamId(args.getInt("examId"));
            }
        }

        // Quan sát LiveData để cập nhật danh sách mã đề
        viewModel.getMaDeList().observe(getViewLifecycleOwner(), maDeItemList -> {
            List<String> codes = new ArrayList<>();
            for (DapAnViewModel.MaDeItem it : maDeItemList) {
                codes.add(it.code);
            }
            adapter.updateData(codes);
            updateUI(codes);
        });

        // Đăng ký listener bằng anonymous class để override cả 2 phương thức
        adapter.setOnExamCodeClickListener(new ExamCodeAdapter.OnExamCodeClickListener() {
            @Override
            public void onExamCodeClick(int position, String maDe) {
                // Chuyển sang AddMaDeFragment để sửa mã đề
                Bundle bundle = new Bundle();
                bundle.putString("maDeToEdit", maDe);
                bundle.putInt   ("positionToEdit", position);
                bundle.putInt   ("questionCount", questionCount);

                List<String> oldList = viewModel.getAnswerListByPosition(position);
                if (oldList != null) {
                    bundle.putStringArrayList(
                            "oldAnswerList",
                            new ArrayList<>(oldList)
                    );
                }


                AddMaDeFragment frag = new AddMaDeFragment();
                frag.setArguments(bundle);
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment, frag)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onExamCodeLongClick(int position, String maDe) {
                // Xác nhận xóa
                new AlertDialog.Builder(requireContext())
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có muốn xóa mã đề \"" + maDe + "\" không?")
                        .setPositiveButton("Có", (dlg, which) -> {
                            viewModel.removeMaDe(position);
                        })
                        .setNegativeButton("Không", null)
                        .show();
            }
        });
    }

    private void updateUI(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            tvNoExamCode.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvNoExamCode.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    // Inflate chỉ menu_add (không có camera)
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu,
                                    @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_dap_an, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // Xử lý chỉ action_add
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            Bundle bundle = new Bundle();
            bundle.putInt("examId",      viewModel.getExamId());
            bundle.putInt("questionCount", questionCount);
            AddMaDeFragment frag = new AddMaDeFragment();
            frag.setArguments(bundle);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, frag)
                    .addToBackStack(null)
                    .commit();
            return true;
        }
        // Nút back/up do Toolbar của Activity xử lý
        return super.onOptionsItemSelected(item);
    }
}
