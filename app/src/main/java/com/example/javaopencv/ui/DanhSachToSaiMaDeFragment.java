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
import com.example.javaopencv.ui.adapter.WrongMaDeAdapter;
import com.example.javaopencv.viewmodel.GradeResultViewModel;

public class DanhSachToSaiMaDeFragment extends Fragment {
    private GradeResultViewModel viewModel;
    private RecyclerView recyclerView;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_danh_sach_to_sai_ma_de,
                container, false
        );
        recyclerView = view.findViewById(R.id.recyclerViewWrongMaDe);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        return view;
    }

    @Override public void onViewCreated(@NonNull View view,
                                        @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int examId = requireArguments().getInt("examId", -1);

        viewModel = new ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory
                        .getInstance(requireActivity().getApplication())
        ).get(GradeResultViewModel.class);

        viewModel.getWrongMaDeResults(examId)
                .observe(getViewLifecycleOwner(), wrongList -> {
                    // Dùng adapter mới, và click chuyển sang chi tiết
                    WrongMaDeAdapter adapter = new WrongMaDeAdapter(
                            wrongList,
                            gr -> {
                                Bundle args = new Bundle();
                                args.putLong("gradeId", gr.getId());
                                NavHostFragment.findNavController(
                                        DanhSachToSaiMaDeFragment.this
                                ).navigate(
                                        R.id.action_danhSachToSaiMaDeFragment_to_gradeDetailFragment,
                                        args
                                );
                            }
                    );
                    recyclerView.setAdapter(adapter);
                });
    }
}
