package com.example.javaopencv.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.Subject;
import com.example.javaopencv.ui.adapter.SubjectAdapter;
import com.example.javaopencv.viewmodel.SubjectViewModel;

public class SubjectFragment extends Fragment {
    private RecyclerView recyclerView;
    private SubjectAdapter adapter;
    private SubjectViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subject, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.rvSubjects);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new SubjectAdapter(item -> {
            Bundle args = new Bundle();
            // sử dụng getter thay vì truy cập trực tiếp
            args.putInt("subjectId", item.getId());
            Navigation.findNavController(requireView()).navigate(R.id.action_subject_to_class, args);
        });
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(SubjectViewModel.class);
        viewModel.getAllSubjects().observe(getViewLifecycleOwner(), subjects -> {
            adapter.submitList(subjects);
        });
    }
}
