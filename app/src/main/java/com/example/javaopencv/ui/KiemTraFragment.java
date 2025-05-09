package com.example.javaopencv.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.Exam;
import com.example.javaopencv.ui.adapter.ExamAdapter;
import com.example.javaopencv.viewmodel.KiemTraViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Fragment hiển thị danh sách bài thi, gọi dialog riêng để thêm/sửa.
 */
public class KiemTraFragment extends Fragment
        implements ExamAdapter.OnExamItemClickListener,
        ExamAdapter.OnExamItemLongClickListener {

    private KiemTraViewModel viewModel;
    private ExamAdapter examAdapter;

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

        // Toolbar title
        AppCompatActivity act = (AppCompatActivity) requireActivity();
        if (act.getSupportActionBar() != null) {
            act.getSupportActionBar().setTitle("Kiểm Tra");
        }

        // RecyclerView & Adapter
        RecyclerView rv = view.findViewById(R.id.rv_exams);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        examAdapter = new ExamAdapter();
        examAdapter.setOnExamItemClickListener(this);
        examAdapter.setOnExamItemLongClickListener(this);
        rv.setAdapter(examAdapter);

        // ViewModel
        viewModel = new ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())
        ).get(KiemTraViewModel.class);
        viewModel.getExams().observe(getViewLifecycleOwner(), exams -> {
            examAdapter.setExamList(exams);
        });

        // FAB
        FloatingActionButton fab = view.findViewById(R.id.fab_add);
        fab.setOnClickListener(v ->
                NewExamDialogFragment.newInstanceForCreate(/*classId*/0)
                        .show(getParentFragmentManager(), "NewExam")
        );
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu,
                                    @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_kiem_tra, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        androidx.appcompat.widget.SearchView sv =
                (androidx.appcompat.widget.SearchView) item.getActionView();
        sv.setQueryHint("Tìm bài thi…");
        sv.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { return false; }
            @Override public boolean onQueryTextChange(String t) { examAdapter.filter(t); return true; }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_filter) {
            showSortDialog(); return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSortDialog() {
        String[] opts = {"Tên (A-Z)","Tên (Z-A)","Ngày (Tăng dần)","Ngày (Giảm dần)"};
        String[] codes = {"name_asc","name_desc","date_asc","date_desc"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Sắp xếp bài thi")
                .setItems(opts,(d,i)->examAdapter.sortByOption(codes[i]))
                .setNegativeButton("HỦY",null)
                .show();
    }

    @Override
    public void onExamItemClick(Exam exam) {
        Bundle args = new Bundle();
        args.putInt("examId", exam.getId());
        args.putInt("questionCount", exam.getSoCau());
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_kiemTraFragment_to_examDetailFragment, args);
    }

    @Override
    public void onExamItemLongClick(Exam exam) {
        String[] items={"Chỉnh sửa bài thi","Xóa bài thi"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn hành động")
                .setItems(items,(d,i)->{
                    if(i==0) NewExamDialogFragment
                            .newInstanceForEdit(exam)
                            .show(getParentFragmentManager(),"EditExam");
                    else {
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Xóa bài thi")
                                .setMessage("Bạn có chắc muốn xóa?")
                                .setNegativeButton("Hủy",null)
                                .setPositiveButton("Xóa",(d2,w2)->{
                                    viewModel.deleteExam(exam);
                                    Toast.makeText(requireContext(),"Đã xóa bài thi",Toast.LENGTH_SHORT).show();
                                }).show();
                    }
                }).show();
    }

}