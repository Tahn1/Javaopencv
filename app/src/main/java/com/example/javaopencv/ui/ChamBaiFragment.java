package com.example.javaopencv.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.javaopencv.R;
import com.example.javaopencv.data.AppDatabase;
import com.example.javaopencv.data.entity.GradeResult;
import com.example.javaopencv.omr.OmrGrader;
import com.example.javaopencv.omr.OmrGrader.Result;
import com.google.android.material.appbar.MaterialToolbar;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ChamBaiFragment extends Fragment {
    private static final int   REQ_READ_EXTERNAL   = 200;
    private static final long  PREVIEW_DURATION_MS = 3000;

    private MaterialToolbar toolbar;
    private Button          btnPickImage;
    private ImageView       imageViewDebug;
    private int             examId, questionCount;
    private int             navigationIconRes;

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK
                                && result.getData() != null
                                && result.getData().getData() != null) {
                            Uri uri = result.getData().getData();
                            handleImageUri(uri);
                        }
                    }
            );

    public ChamBaiFragment() {
        super(R.layout.fragment_cham_bai);
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Layout inflation sẽ được xử lý bởi super(), nhưng ta vẫn giữ onCreateView nếu cần custom
        return inflater.inflate(R.layout.fragment_cham_bai, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // 1) Lấy examId và questionCount từ arguments
        Bundle args = getArguments();
        if (args != null) {
            examId        = args.getInt("examId", -1);
            questionCount = args.getInt("questionCount", 20);
        }

        // 2) Ánh xạ view
        toolbar        = view.findViewById(R.id.toolbar);
        btnPickImage   = view.findViewById(R.id.btnPickImage);
        imageViewDebug = view.findViewById(R.id.imageViewDebug);

        // 3) Lưu icon back để restore sau
        navigationIconRes = R.drawable.ic_arrow_back_white;

        // 4) Cấu hình back arrow
        toolbar.setNavigationIcon(navigationIconRes);
        toolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        // 5) Bấm nút chọn ảnh
        btnPickImage.setOnClickListener(v -> launchImagePicker());
    }

    private void launchImagePicker() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQ_READ_EXTERNAL
            );
        } else {
            Intent pick = new Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            );
            pick.setType("image/*");
            pickImageLauncher.launch(pick);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_READ_EXTERNAL
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            launchImagePicker();
        } else {
            Toast.makeText(requireContext(),
                    "Cần cấp quyền truy cập thư viện",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void handleImageUri(@NonNull Uri uri) {
        try {
            Bitmap bmp = MediaStore.Images.Media.getBitmap(
                    requireContext().getContentResolver(), uri);
            runOmrOnBitmap(bmp);
        } catch (IOException e) {
            Toast.makeText(requireContext(),
                    "Không đọc được ảnh",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void runOmrOnBitmap(@NonNull Bitmap bmp) {
        new Thread(() -> {
            OpenCVLoader.initDebug();
            Result res = OmrGrader.grade(bmp, examId, requireContext());

            requireActivity().runOnUiThread(() -> {
                if (res == null) {
                    Toast.makeText(requireContext(),
                            "Xử lý OMR thất bại",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // cập nhật toolbar
                toolbar.setTitle(String.format(
                        "Mã đề %s – Điểm: %d/%d = %.2f",
                        res.maDe, res.correctCount, questionCount, res.score
                ));

                // hiển thị ảnh debug
                imageViewDebug.setImageBitmap(res.annotatedBitmap);
                imageViewDebug.setVisibility(View.VISIBLE);
                btnPickImage.setVisibility(View.GONE);
                toolbar.setNavigationIcon(null);

                // lưu ảnh debug ra file
                String debugUri = null;
                try {
                    File dir = new File(
                            requireContext().getExternalFilesDir(null),
                            "debug_results"
                    );
                    if (!dir.exists()) dir.mkdirs();
                    File out = new File(dir, System.currentTimeMillis() + "_graded.jpg");
                    try (FileOutputStream fos = new FileOutputStream(out)) {
                        res.annotatedBitmap.compress(
                                Bitmap.CompressFormat.JPEG, 90, fos
                        );
                    }
                    debugUri = Uri.fromFile(out).toString();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // lưu vào DB
                final String imagePathToSave = debugUri;
                new Thread(() -> {
                    GradeResult gr = new GradeResult(
                            examId,
                            res.maDe,
                            res.sbd,
                            res.correctCount,
                            questionCount,
                            res.score,
                            imagePathToSave,
                            0.5f,  // focusX giả định, bạn có thể tính lại
                            0.5f   // focusY giả định
                    );
                    AppDatabase.getInstance(requireContext())
                            .gradeResultDao()
                            .insert(gr);
                }).start();

                // reset UI sau preview
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    imageViewDebug.setVisibility(View.GONE);
                    btnPickImage.setVisibility(View.VISIBLE);
                    toolbar.setNavigationIcon(navigationIconRes);
                    toolbar.setTitle(R.string.cham_bai_title);
                }, PREVIEW_DURATION_MS);
            });
        }).start();
    }
}
