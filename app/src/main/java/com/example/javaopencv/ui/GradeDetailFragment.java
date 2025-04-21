package com.example.javaopencv.ui;

import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.GradeResult;
import com.example.javaopencv.viewmodel.GradeDetailViewModel;
import com.google.android.material.appbar.MaterialToolbar;

public class GradeDetailFragment extends Fragment {

    public GradeDetailFragment() {
        super(R.layout.fragment_grade_detail);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar_grade_detail);
        ImageView ivResult     = view.findViewById(R.id.ivResult);

        // Back arrow
        toolbar.setNavigationOnClickListener(v ->
                requireActivity().onBackPressed()
        );

        // Edit button
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit) {
                long gradeId = getArguments().getLong("gradeId", -1L);
                Bundle args = new Bundle();
                args.putLong("gradeId", gradeId);
                // điều hướng sang màn edit (EditGradeFragment)
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_gradeDetailFragment_to_editGradeFragment, args);
                return true;
            }
            return false;
        });

        // Lấy gradeId từ arguments
        long gradeId = getArguments().getLong("gradeId", -1L);
        GradeDetailViewModel vm =
                new ViewModelProvider(this, new GradeDetailViewModel.Factory(requireActivity().getApplication(), gradeId))
                        .get(GradeDetailViewModel.class);

        // Observe và bind dữ liệu
        vm.getGradeResult().observe(getViewLifecycleOwner(), gr -> {
            if (gr == null) return;
            // set title: "Mã đề 189 – Điểm: 4/20 = 2.00"
            toolbar.setTitle(
                    String.format("Mã đề %s – Điểm: %d/%d = %.2f",
                            gr.maDe, gr.correctCount, gr.totalQuestions, gr.score)
            );
            // load ảnh đã chấm
            if (gr.imagePath != null) {
                ivResult.setImageURI(Uri.parse(gr.imagePath));
            }
        });
    }
}
