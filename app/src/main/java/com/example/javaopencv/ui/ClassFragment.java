package com.example.javaopencv.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.SchoolClass;
import com.example.javaopencv.ui.adapter.ClassAdapter;
import com.example.javaopencv.viewmodel.ClassViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ClassFragment extends Fragment {
    private ClassViewModel viewModel;
    private ClassAdapter adapter;
    private int subjectId;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_class, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            subjectId = getArguments().getInt("subjectId", 0);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Factory truyền subjectId
        ClassViewModel.Factory factory =
                new ClassViewModel.Factory(requireActivity().getApplication(), subjectId);
        viewModel = new ViewModelProvider(this, factory)
                .get(ClassViewModel.class);

        RecyclerView rv = view.findViewById(R.id.rvClasses);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ClassAdapter(item -> {
            Bundle args = new Bundle();
            args.putInt("classId", item.id);
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.classDetailFragment, args);
        });
        rv.setAdapter(adapter);

        // Sử dụng chung getClasses() (đã lấy all hoặc forSubject bên ViewModel)
        viewModel.getClasses().observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
        });

        FloatingActionButton fab = view.findViewById(R.id.fab_add_class);
        fab.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("subjectId", subjectId);
            // TODO: show add/edit class dialog
        });
    }
}