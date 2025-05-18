package com.example.javaopencv.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.javaopencv.R;
import com.example.javaopencv.viewmodel.ClassViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;


public class ClassDetailFragment extends Fragment {
    private static final String ARG_CLASS_ID = "classId";
    private int classId;
    private ClassViewModel viewModel;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_class_detail, container, false);
    }

    @Override public void onViewCreated(@NonNull View view,
                                        @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Nhận classId từ arguments
        if (getArguments() != null) {
            classId = getArguments().getInt(ARG_CLASS_ID, -1);
        }

        // Khởi tạo ViewModel với Factory (subjectId không dùng ở đây)
        viewModel = new ViewModelProvider(
                this,
                new ClassViewModel.Factory(requireActivity().getApplication())
        ).get(ClassViewModel.class);

        // Quan sát tên lớp và đặt làm title
        viewModel.getClassById(classId)
                .observe(getViewLifecycleOwner(), klass -> {
                    if (klass != null) {
                        AppCompatActivity act = (AppCompatActivity) requireActivity();
                        if (act.getSupportActionBar() != null) {
                            act.getSupportActionBar().setTitle(klass.getName());
                        }
                    }
                });

        // Thiết lập ViewPager2 và TabLayout
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        ViewPager2 viewPager = view.findViewById(R.id.viewPager);

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @Override public int getItemCount() { return 2; }

            @NonNull @Override
            public Fragment createFragment(int position) {
                if (position == 0) {
                    return ClassDetailExamFragment.newInstance(classId);
                } else {
                    return ClassDetailStudentFragment.newInstance(classId);
                }
            }
        });

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, pos) -> tab.setText(pos == 0 ? "KIỂM TRA" : "HỌC SINH")
        ).attach();
    }
}
