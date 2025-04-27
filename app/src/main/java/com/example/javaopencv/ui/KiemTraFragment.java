package com.example.javaopencv.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;              // ← Import đúng AppCompat SearchView
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

public class KiemTraFragment extends Fragment
        implements ExamAdapter.OnExamItemClickListener,
        ExamAdapter.OnExamItemLongClickListener {

    private KiemTraViewModel viewModel;
    private RecyclerView     rvExams;
    private ExamAdapter      examAdapter;
    private FloatingActionButton fabAdd;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_kiem_tra, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1) Toolbar title
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle("Kiểm Tra");
        }

        // 2) RecyclerView + Adapter
        rvExams = view.findViewById(R.id.rv_exams);
        rvExams.setLayoutManager(new LinearLayoutManager(requireContext()));
        examAdapter = new ExamAdapter();
        examAdapter.setOnExamItemClickListener(this);
        examAdapter.setOnExamItemLongClickListener(exam -> {
            NewExamDialogFragment
                    .newInstanceForEdit(exam)
                    .show(getParentFragmentManager(), "EditExam");
        });        rvExams.setAdapter(examAdapter);

        // 3) ViewModel
        viewModel = new ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())
        ).get(KiemTraViewModel.class);

        viewModel.getExams().observe(getViewLifecycleOwner(), exams -> {
            Log.d("KiemTraFragment", "LiveData emit, size = "
                    + (exams == null ? 0 : exams.size()));
            examAdapter.setExamList(exams);
        });

        // 4) FAB tạo bài thi (dùng chung dialog)
        fabAdd = view.findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v ->
                NewExamDialogFragment
                        .newInstanceForCreate(0)
                        .show(getParentFragmentManager(), "NewExam")
        );
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_kiem_tra, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();  // ← AppCompat SearchView
        searchView.setQueryHint("Tìm bài thi…");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
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
        String[] codes   = {"name_asc",    "name_desc",   "date_asc",     "date_desc"};
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
        Bundle args = new Bundle();
        args.putInt("questionCount", exam.getSoCau());
        args.putInt("examId",        exam.getId());
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_kiemTraFragment_to_examDetailFragment, args);
    }

    @Override
    public void onExamItemLongClick(Exam exam) {
        String[] opts = {"Sửa", "Xóa"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn hành động")
                .setItems(opts, (dlg, which) -> {
                    if (which == 0) {
                        // mở dialog edit chung
                        NewExamDialogFragment
                                .newInstanceForEdit(exam)
                                .show(getParentFragmentManager(), "EditExam");
                    } else {
                        viewModel.deleteExam(exam);
                        Toast.makeText(requireContext(),
                                "Đã xóa bài", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("HỦY", null)
                .show();
    }

    /** InputFilter để giới hạn số câu */
    public static class InputFilterMinMax implements InputFilter {
        private final int min, max;
        public InputFilterMinMax(int min, int max) { this.min = min; this.max = max; }
        @Override
        public CharSequence filter(CharSequence src, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            try {
                String result = dest.toString().substring(0, dstart)
                        + src + dest.toString().substring(dend);
                int val = Integer.parseInt(result);
                if (val >= min && val <= max) return null;
            } catch (NumberFormatException ignored) {}
            return "";
        }
    }
}
