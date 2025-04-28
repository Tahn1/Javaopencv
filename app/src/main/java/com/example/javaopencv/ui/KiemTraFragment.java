package com.example.javaopencv.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.Exam;
import com.example.javaopencv.ui.NewExamDialogFragment;
import com.example.javaopencv.ui.adapter.ExamAdapter;
import com.example.javaopencv.viewmodel.KiemTraViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Fragment hiển thị danh sách bài thi, hỗ trợ tìm kiếm, sắp xếp, thêm, sửa tiêu đề và xóa.
 */
public class KiemTraFragment extends Fragment
        implements ExamAdapter.OnExamItemClickListener,
        ExamAdapter.OnExamItemLongClickListener {

    private KiemTraViewModel viewModel;
    private RecyclerView rvExams;
    private ExamAdapter examAdapter;
    private FloatingActionButton fabAdd;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_kiem_tra, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Thiết lập toolbar title
        AppCompatActivity act = (AppCompatActivity) requireActivity();
        if (act.getSupportActionBar() != null) {
            act.getSupportActionBar().setTitle("Kiểm Tra");
        }

        // Setup RecyclerView và Adapter
        rvExams = view.findViewById(R.id.rv_exams);
        rvExams.setLayoutManager(new LinearLayoutManager(requireContext()));
        examAdapter = new ExamAdapter();
        examAdapter.setOnExamItemClickListener(this);
        examAdapter.setOnExamItemLongClickListener(this);
        rvExams.setAdapter(examAdapter);

        // ViewModel và quan sát dữ liệu
        viewModel = new ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())
        ).get(KiemTraViewModel.class);
        viewModel.getExams().observe(getViewLifecycleOwner(), exams -> {
            examAdapter.setExamList(exams);
        });

        // FAB thêm bài thi
        fabAdd = view.findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v ->
                NewExamDialogFragment.newInstanceForCreate(0)
                        .show(getParentFragmentManager(), "NewExam")
        );
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu,
                                    @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_kiem_tra, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Tìm bài thi…");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                examAdapter.filter(newText);
                return true;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_filter) {
            showSortDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSortDialog() {
        String[] options = {"Tên (A-Z)", "Tên (Z-A)", "Ngày (Tăng dần)", "Ngày (Giảm dần)"};
        String[] codes = {"name_asc", "name_desc", "date_asc", "date_desc"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Sắp xếp bài thi")
                .setItems(options, (dlg, which) ->
                        examAdapter.sortByOption(codes[which])
                )
                .setNegativeButton("HỦY", null)
                .show();
    }

    @Override
    public void onExamItemClick(Exam exam) {
        // Điều hướng đến ExamDetailFragment, kèm examId và questionCount
        Bundle args = new Bundle();
        args.putInt("examId", exam.getId());
        args.putInt("questionCount", exam.getSoCau());
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_kiemTraFragment_to_examDetailFragment, args);
    }

    @Override
    public void onExamItemLongClick(Exam exam) {
        // Long-press: chọn Sửa hoặc Xóa
        String[] opts = {"Chỉnh sửa bài thi", "Xóa bài thi"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn hành động")
                .setItems(opts, (dlg, which) -> {
                    if (which == 0) {
                        // Mở dialog edit, chỉ sửa tiêu đề
                        NewExamDialogFragment
                                .newInstanceForEdit(exam)
                                .show(getParentFragmentManager(), "EditExam");
                    } else {
                        // Xác nhận trước khi xóa bài thi
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Xóa bài thi")
                                .setMessage("Bạn có chắc muốn xóa bài thi này không?")
                                .setNegativeButton("Hủy", null)
                                .setPositiveButton("Xóa", (d3, w3) -> {
                                    viewModel.deleteExam(exam);
                                    Toast.makeText(requireContext(),
                                                    "Đã xóa bài thi", Toast.LENGTH_SHORT)
                                            .show();
                                })
                                .show();
                    }
                })
                .show();
    }
}