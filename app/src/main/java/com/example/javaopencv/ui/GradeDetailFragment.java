package com.example.javaopencv.ui;

import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.GradeResult;
import com.example.javaopencv.viewmodel.GradeDetailViewModel;

public class GradeDetailFragment extends Fragment {

    public GradeDetailFragment() {
        super(R.layout.fragment_grade_detail);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // bật menu edit
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu,
                                    @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_grade_detail, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_edit) {
            long gradeId = getArguments().getLong("gradeId", -1L);
            Bundle args = new Bundle();
            args.putLong("gradeId", gradeId);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_gradeDetailFragment_to_editGradeFragment, args);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1) Toolbar của fragment
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar_grade_detail);
        AppCompatActivity act = (AppCompatActivity) requireActivity();
        act.setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v ->
                act.onBackPressed()
        );
        // thu nhỏ font title nếu muốn
        toolbar.setTitleTextAppearance(
                requireContext(),
                R.style.TextAppearance_Toolbar_Title_Small
        );

        // 2) ImageView cho ảnh chi tiết
        ImageView ivResult = view.findViewById(R.id.ivResult);

        // 3) ViewModel và observe kết quả chấm
        long gradeId = getArguments().getLong("gradeId", -1L);
        GradeDetailViewModel vm = new ViewModelProvider(
                this,
                new GradeDetailViewModel.Factory(
                        act.getApplication(), gradeId
                )
        ).get(GradeDetailViewModel.class);

        vm.getGradeResult().observe(getViewLifecycleOwner(), gr -> {
            if (gr == null) return;

            // a) Hiển thị title: "Mã đề X – Đúng a/b = c"
            toolbar.setTitle(String.format(
                    "Mã đề %s – Đúng %d/%d = %.2f",
                    gr.maDe, gr.correctCount, gr.totalQuestions, gr.score
            ));

            // b) Hiển thị ảnh chi tiết
            if (gr.imagePath != null) {
                ivResult.setImageURI(Uri.parse(gr.imagePath));
            }
        });
    }
}
