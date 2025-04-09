package com.example.javaopencv.ui;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.Exam;
import com.example.javaopencv.viewmodel.ExamViewModel;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ExamViewModel examViewModel;
    private RecyclerView recyclerView;
    private ExamAdapter examAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo RecyclerView và Adapter
        recyclerView = findViewById(R.id.recycler_view_exams);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        examAdapter = new ExamAdapter();
        recyclerView.setAdapter(examAdapter);

        // Lấy instance của ExamViewModel
        examViewModel = new ViewModelProvider(this,
                new ViewModelProvider.AndroidViewModelFactory(getApplication()))
                .get(ExamViewModel.class);

        // Quan sát LiveData từ ViewModel để tự động cập nhật UI khi dữ liệu thay đổi
        examViewModel.getAllExams().observe(this, new Observer<List<Exam>>() {
            @Override
            public void onChanged(List<Exam> exams) {
                // Cập nhật Adapter với danh sách exam mới
                examAdapter.setExamList(exams);
            }
        });

        // Ví dụ: thêm một Exam (bạn có thể thực hiện từ một nút nhấn, dialog, v.v.)
        // examViewModel.addExam(new Exam(1, "Bài kiểm tra 1", "Phieu1", 20, "2023-04-07"));
    }
}
