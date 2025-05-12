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
import com.example.javaopencv.data.entity.GradeResult;
import com.example.javaopencv.ui.adapter.DapSaiAdapter;
import com.example.javaopencv.viewmodel.GradeResultViewModel;

import java.util.List;

/**
 * Fragment hiển thị danh sách các bài thi có mã đề sai so với đáp án gốc.
 */
public class DanhSachToSaiMaDeFragment extends Fragment {
    private GradeResultViewModel viewModel;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_danh_sach_to_sai_ma_de, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewWrongMaDe);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int examId = requireArguments().getInt("examId", -1);

        viewModel = new ViewModelProvider(
                this,
                new GradeResultViewModel.Factory(requireActivity().getApplication())
        ).get(GradeResultViewModel.class);

        viewModel.getWrongMaDeResults(examId)
                .observe(getViewLifecycleOwner(), wrongList -> {
                    DapSaiAdapter adapter = new DapSaiAdapter(wrongList);
                    recyclerView.setAdapter(adapter);
                });
    }
}
