package com.example.javaopencv.ui;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;
import com.example.javaopencv.ui.adapter.GradeResultAdapter;
import com.example.javaopencv.viewmodel.XemLaiViewModel;
import com.google.android.material.appbar.MaterialToolbar;

public class XemLaiFragment extends Fragment {

    public XemLaiFragment() {
        super(R.layout.fragment_xem_lai);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // 1) Toolbar với nút Back
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar_xem_lai);
        toolbar.setNavigationOnClickListener(v ->
                requireActivity().onBackPressed()
        );

        // 2) RecyclerView + Adapter + click listener
        RecyclerView rv = view.findViewById(R.id.rvGradeResults);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        GradeResultAdapter adapter = new GradeResultAdapter();
        rv.setAdapter(adapter);

        adapter.setOnItemClickListener(item -> {
            // click thì chuyển sang GradeDetail, truyền gradeId
            Bundle args = new Bundle();
            args.putLong("gradeId", item.id);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_xemLaiFragment_to_gradeDetailFragment, args);
        });

        // 3) Lấy viewModel và examId
        XemLaiViewModel vm = new ViewModelProvider(this).get(XemLaiViewModel.class);
        int examId = requireArguments().getInt("examId", -1);

        // 4) Quan sát LiveData có filter examId
        vm.getResultsForExam(examId)
                .observe(getViewLifecycleOwner(), list -> {
                    adapter.submitList(list);  // sử dụng submitList(List<T>) để tránh ambiguous
                });
    }
}