package com.example.javaopencv.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.javaopencv.R;
import com.example.javaopencv.omr.MarkerUtils;
import com.example.javaopencv.omr.OmrGrader;
import com.google.common.util.concurrent.ListenableFuture;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChamBaiFragment extends Fragment {
    private static final String TAG = "ChamBaiFragment";

    static {
        if (!OpenCVLoader.initDebug()) {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        }
    }

    private PreviewView previewView;
    private TextView tvSwipeHint;
    private ImageView imageViewResult;
    private TextView textViewHeader;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private boolean hasTriggered = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cham_bai, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ẩn toolbar của Activity khi vào màn hình camera
        AppCompatActivity act = (AppCompatActivity) getActivity();
        if (act != null && act.getSupportActionBar() != null) {
            act.getSupportActionBar().hide();
        }

        previewView     = view.findViewById(R.id.previewView);
        tvSwipeHint     = view.findViewById(R.id.tvSwipeHint);
        imageViewResult = view.findViewById(R.id.imageViewResult);
        textViewHeader  = view.findViewById(R.id.textViewHeader);

        cameraExecutor = Executors.newSingleThreadExecutor();

        // Ẩn hint sau 3 giây
        tvSwipeHint.postDelayed(() -> tvSwipeHint.setVisibility(View.GONE), 3000);

        startCamera();
        setupEdgeSwipeToExit();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                analysis.setAnalyzer(cameraExecutor, this::analyzeFrame);

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this,
                        new CameraSelector.Builder()
                                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                                .build(),
                        preview,
                        imageCapture,
                        analysis
                );
            } catch (Exception e) {
                Log.e(TAG, "Camera start error", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void analyzeFrame(@NonNull ImageProxy imageProxy) {
        if (hasTriggered) {
            imageProxy.close();
            return;
        }
        Bitmap bmp = toBitmap(imageProxy);
        imageProxy.close();

        Mat mat = new Mat();
        Utils.bitmapToMat(bmp, mat);

        List<MatOfPoint> markers = MarkerUtils.findMarkers(
                mat, 100.0, 1500.0, 0.6, 0.99);

        if (markers.size() >= 4) {
            hasTriggered = true;
            takePhotoAndGrade();
        }
    }

    private Bitmap toBitmap(ImageProxy image) {
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        ByteBuffer y = planes[0].getBuffer();
        ByteBuffer u = planes[1].getBuffer();
        ByteBuffer v = planes[2].getBuffer();

        int w = image.getWidth(), h = image.getHeight();
        byte[] nv21 = new byte[y.remaining() + u.remaining() + v.remaining()];
        y.get(nv21, 0, y.remaining());
        v.get(nv21, y.remaining(), v.remaining());
        u.get(nv21, y.remaining() + v.remaining(), u.remaining());

        YuvImage yuv = new YuvImage(nv21, ImageFormat.NV21, w, h, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuv.compressToJpeg(new Rect(0, 0, w, h), 90, out);
        byte[] jpg = out.toByteArray();
        return BitmapFactory.decodeByteArray(jpg, 0, jpg.length);
    }

    private void takePhotoAndGrade() {
        if (imageCapture == null) return;

        imageCapture.takePicture(
                ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy proxy) {
                        Bitmap bmp = toBitmap(proxy);
                        proxy.close();

                        OmrGrader.Result result = OmrGrader.grade(
                                bmp,
                                getArguments().getInt("examId", -1),
                                requireContext()
                        );

                        requireActivity().runOnUiThread(() -> {
                            previewView.setVisibility(View.GONE);
                            tvSwipeHint.setVisibility(View.GONE);
                            textViewHeader.setVisibility(View.VISIBLE);
                            imageViewResult.setVisibility(View.VISIBLE);

                            if (result != null) {
                                textViewHeader.setText(
                                        "Mã đề: "   + result.maDe +
                                                "\nSBD: "   + result.sbd +
                                                "\nĐúng: "  + result.correctCount +
                                                "/20   Điểm: " + String.format("%.1f", result.score)
                                );
                                imageViewResult.setImageBitmap(result.annotatedBitmap);
                            } else {
                                Toast.makeText(requireContext(),
                                        "Chấm bài thất bại", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Photo capture failed", exception);
                    }
                }
        );
    }

    private void setupEdgeSwipeToExit() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        final int edgeSizePx = (int)(20 * dm.density);
        final int swipeSlop = ViewConfiguration.get(requireContext()).getScaledTouchSlop();

        GestureDetector.SimpleOnGestureListener listener =
                new GestureDetector.SimpleOnGestureListener() {
                    private float startX, startY;
                    @Override
                    public boolean onDown(MotionEvent e) {
                        startX = e.getX();
                        startY = e.getY();
                        return true;
                    }
                    @Override
                    public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                            float dx, float dy) {
                        float deltaX = e2.getX() - startX;
                        float deltaY = Math.abs(e2.getY() - startY);
                        if (startX <= edgeSizePx && deltaX > swipeSlop && deltaY < deltaX/2) {
                            tvSwipeHint.setVisibility(View.GONE);
                            requireActivity().onBackPressed();
                            return true;
                        }
                        return false;
                    }
                };

        GestureDetector detector = new GestureDetector(requireContext(), listener);
        previewView.setOnTouchListener((v, event) -> detector.onTouchEvent(event));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Khi rời ChamBaiFragment, hiện lại toolbar của Activity
        AppCompatActivity act = (AppCompatActivity) getActivity();
        if (act != null && act.getSupportActionBar() != null) {
            act.getSupportActionBar().show();
        }
        cameraExecutor.shutdown();
    }
}
