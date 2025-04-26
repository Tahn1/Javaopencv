package com.example.javaopencv.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // inflate layout fragment_subject.xml
        return inflater.inflate(R.layout.fragment_subject, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1) RecyclerView
        RecyclerView rv = view.findViewById(R.id.rvSubjects);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 2) Adapter với listener click 1 subject -> đi ClassFragment với subjectId
        adapter = new SubjectAdapter(item -> {
            Bundle args = new Bundle();
            args.putInt("subjectId", item.id);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.classFragment, args);
        });
        rv.setAdapter(adapter);

        // 3) ViewModel lấy LiveData<List<Subject>>
        viewModel = new ViewModelProvider(this)
                .get(SubjectViewModel.class);
        viewModel.getAllSubjects()
                .observe(getViewLifecycleOwner(), list -> adapter.submitList(list));

        // 4) Nút thêm môn học (ví dụ mở dialog)
        FloatingActionButton fab = view.findViewById(R.id.fab_add_subject);
        fab.setOnClickListener(v -> {
            // TODO: show dialog nhập tên môn, rồi viewModel.insert(...)
        });
    }
}