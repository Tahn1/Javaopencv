package com.example.javaopencv.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.SchoolClass;
import com.example.javaopencv.ui.adapter.ClassAdapter;
import com.example.javaopencv.viewmodel.ClassViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class ClassFragment extends Fragment {
    private ClassViewModel viewModel;
    private ClassAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_class, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1) Lấy subjectId từ Bundle (default = 0)
        int subjectId = 0;
        Bundle args = getArguments();
        if (args != null && args.containsKey("subjectId")) {
            subjectId = args.getInt("subjectId", 0);
        }

        // 2) RecyclerView
        RecyclerView rv = view.findViewById(R.id.rvClasses);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ClassAdapter(item -> {
            // Click vào 1 lớp -> đi ClassDetailFragment
            Bundle b = new Bundle();
            b.putInt("classId", item.id);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.classDetailFragment, b);
        });
        rv.setAdapter(adapter);

        // 3) ViewModel (dùng Factory nếu cần truyền subjectId)
        viewModel = new ViewModelProvider(this,
                new ClassViewModel.Factory(requireActivity().getApplication(), subjectId)
        ).get(ClassViewModel.class);

        // Quan sát data và submit cho adapter
        viewModel.getClasses().observe(getViewLifecycleOwner(),
                (List<SchoolClass> list) -> adapter.submitList(list)
        );

        // 4) Nút thêm lớp
        FloatingActionButton fab = view.findViewById(R.id.fab_add_class);
        fab.setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "TODO: mở dialog thêm lớp",
                        Toast.LENGTH_SHORT).show()
        );
    }
}
