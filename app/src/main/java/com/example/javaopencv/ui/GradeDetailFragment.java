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
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.javaopencv.R;
import com.example.javaopencv.viewmodel.GradeDetailViewModel;

import java.util.Locale;

public class GradeDetailFragment extends Fragment {

    public GradeDetailFragment() {
        super(R.layout.fragment_grade_detail);
    }

    private ImageView ivResult;
    private ActionBar ab;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu,
                                    @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_grade_detail, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            requireActivity().onBackPressed();
            return true;
        }
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

        ivResult = view.findViewById(R.id.ivResult);

        AppCompatActivity act = (AppCompatActivity) requireActivity();
        ab = act.getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);

        }

        long gradeId = getArguments().getLong("gradeId", -1L);
        GradeDetailViewModel vm = new ViewModelProvider(
                this,
                new GradeDetailViewModel.Factory(act.getApplication(), gradeId)
        ).get(GradeDetailViewModel.class);

        vm.getGradeResult().observe(getViewLifecycleOwner(), gr -> {
            if (gr == null) return;

            if (ab != null) {
                ab.setTitle("Mã đề " + gr.maDe);
                ab.setSubtitle(String.format(
                        Locale.getDefault(),
                        "Đúng %d/%d = %.2f",
                        gr.correctCount,
                        gr.totalQuestions,
                        gr.score
                ));
            }

            if (gr.imagePath != null) {
                ivResult.setImageURI(Uri.parse(gr.imagePath));
            }
        });
    }
}
