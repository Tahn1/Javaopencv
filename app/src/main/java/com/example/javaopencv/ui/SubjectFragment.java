package com.example.javaopencv.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.Subject;
import com.example.javaopencv.ui.adapter.SubjectAdapter;
import com.example.javaopencv.viewmodel.SubjectViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SubjectFragment extends Fragment {
    private SubjectViewModel viewModel;
    private SubjectAdapter adapter;

    @Nullable @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_subject, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // 1) Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(SubjectViewModel.class);

        // 2) Thiết lập RecyclerView + Adapter
        RecyclerView rv = view.findViewById(R.id.rvSubjects);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SubjectAdapter(item -> {
            // Khi click một môn, điều hướng tới ClassFragment với subjectId
            Bundle args = new Bundle();
            args.putInt("subjectId", item.id);
            requireActivity()
                    .getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment)
                    .getParentFragmentManager()
                    .setFragmentResult("openClass", args);
        });
        rv.setAdapter(adapter);

        // 3) Observe dữ liệu từ ViewModel
        viewModel.getAllSubjects().observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
        });

        // 4) Thêm button “+” để thêm môn mới
        FloatingActionButton fab = view.findViewById(R.id.fab_add_subject);
        fab.setOnClickListener(v -> {
            // Mở dialog hoặc fragment để tạo subject mới
            // Ví dụ: new AddEditSubjectDialog().show(...)
        });
    }
}
