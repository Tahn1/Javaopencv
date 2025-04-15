package com.example.javaopencv.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.javaopencv.R;
import com.example.javaopencv.omr.OmrGrader;
import com.google.common.util.concurrent.ListenableFuture;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChamBaiFragment extends Fragment {

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final String TAG = "ChamBaiFragment";

    private int examId;
    private PreviewView previewView;
    private Button btnCapture;
    private ImageView imageViewResult;
    private TextView textViewHeader;

    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "OpenCV init failed");
        } else {
            Log.d(TAG, "OpenCV init success");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cham_bai, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        previewView = view.findViewById(R.id.previewView);
        btnCapture = view.findViewById(R.id.btnCapture);
        imageViewResult = view.findViewById(R.id.imageViewResult);
        textViewHeader = view.findViewById(R.id.textViewHeader);

        if (getArguments() != null) {
            examId = getArguments().getInt("examId", -1);
        }

        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "OpenCV init failed!");
        } else {
            Log.d(TAG, "OpenCV init success");
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            startCamera();
        }

        cameraExecutor = Executors.newSingleThreadExecutor();

        btnCapture.setOnClickListener(v -> takePhoto());
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                imageCapture = new ImageCapture.Builder().build();

                androidx.camera.core.Preview preview = new androidx.camera.core.Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                cameraProvider.unbindAll();
                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Camera start error", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        File photoFile = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".jpg");

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputFileOptions, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                requireActivity().runOnUiThread(() -> handleCapturedImage(photoFile));
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, "Photo capture failed", exception);
            }
        });
    }

    private void handleCapturedImage(File photoFile) {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(photoFile));
            OmrGrader.Result result = OmrGrader.grade(bitmap, examId, requireContext());

            if (result != null) {
                textViewHeader.setText("Mã đề: " + result.maDe +
                        "\nSBD: " + result.sbd +
                        "\nSố câu đúng: " + result.correctCount + " / 20" +
                        "\nĐiểm: " + String.format(Locale.getDefault(), "%.1f", result.score));
                imageViewResult.setImageBitmap(result.annotatedBitmap);
            } else {
                textViewHeader.setText("Không thể xử lý bài làm.");
            }
        } catch (IOException e) {
            Log.e(TAG, "Lỗi khi đọc file ảnh", e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}