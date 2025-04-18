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
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.javaopencv.R;
import com.example.javaopencv.omr.OmrGrader;
import com.google.common.util.concurrent.ListenableFuture;

import org.opencv.android.OpenCVLoader;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChamBaiFragment extends Fragment {
    private static final String TAG = "ChamBaiFragment";
    static {
        if (!OpenCVLoader.initDebug()) {
            // load lib if cần thiết
            System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
        }
    }

    private PreviewView previewView;
    private OverlayView overlayView;
    private ImageButton btnCapture;
    private ImageView imageViewResult;
    private TextView  textViewHeader;
    private ImageCapture   imageCapture;
    private ExecutorService cameraExecutor;
    private int    questionCount = 20;
    private float  originalBrightness;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cham_bai, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Giữ full‐screen và tăng độ sáng
        AppCompatActivity act = (AppCompatActivity) getActivity();
        if (act != null && act.getSupportActionBar() != null) {
            act.getSupportActionBar().hide();
        }
        WindowManager.LayoutParams lp = requireActivity().getWindow().getAttributes();
        originalBrightness = lp.screenBrightness;
        lp.screenBrightness = 1f;
        requireActivity().getWindow().setAttributes(lp);
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // đọc questionCount nếu có
        if (getArguments() != null) {
            questionCount = getArguments().getInt("questionCount", questionCount);
        }

        // Ánh xạ view
        previewView     = view.findViewById(R.id.previewView);
        overlayView     = view.findViewById(R.id.overlayView);
        btnCapture      = view.findViewById(R.id.btn_capture);
        imageViewResult = view.findViewById(R.id.imageViewResult);
        textViewHeader  = view.findViewById(R.id.textViewHeader);

        // ban đầu ẩn kết quả
        imageViewResult.setVisibility(View.GONE);
        textViewHeader.setVisibility(View.GONE);

        cameraExecutor = Executors.newSingleThreadExecutor();

        startCamera();
        setupEdgeSwipeToExit();

        btnCapture.setOnClickListener(v -> takePhotoAndGrade());
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(requireContext());
        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                provider.unbindAll();
                provider.bindToLifecycle(this,
                        new CameraSelector.Builder()
                                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                                .build(),
                        preview,
                        imageCapture);

            } catch (Exception e) {
                Log.e(TAG, "startCamera failed", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void takePhotoAndGrade() {
        if (imageCapture == null) return;
        btnCapture.setEnabled(false);

        imageCapture.takePicture(
                ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override public void onCaptureSuccess(@NonNull ImageProxy proxy) {
                        Bitmap bmp = toBitmap(proxy);
                        proxy.close();

                        // Gọi OMR trong try–catch để bắt lỗi không đủ marker lớn
                        OmrGrader.Result result;
                        try {
                            result = OmrGrader.grade(
                                    bmp,
                                    getArguments() != null ? getArguments().getInt("examId", -1) : -1,
                                    requireContext()
                            );
                        } catch (Exception e) {
                            Log.e(TAG, "OMR failed: not enough markers", e);
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(),
                                        "Không tìm đủ 4 marker để căn chỉnh, vui lòng chụp lại gần hơn",
                                        Toast.LENGTH_LONG).show();
                                btnCapture.setEnabled(true);
                            });
                            return;
                        }

                        requireActivity().runOnUiThread(() -> {
                            // Ẩn camera + overlay + nút chụp
                            previewView.setVisibility(View.GONE);
                            overlayView.setVisibility(View.GONE);
                            btnCapture.setVisibility(View.GONE);

                            // Hiện kết quả
                            textViewHeader.setVisibility(View.VISIBLE);
                            imageViewResult.setVisibility(View.VISIBLE);

                            if (result != null) {
                                textViewHeader.setText(
                                        "Mã đề: " + result.maDe +
                                                "\nSBD: "   + result.sbd +
                                                "\nĐúng: "  + result.correctCount +
                                                "/" + questionCount +
                                                "   Điểm: " + String.format("%.1f", result.score)
                                );
                                imageViewResult.setImageBitmap(result.annotatedBitmap);
                            } else {
                                Toast.makeText(requireContext(),
                                        "Chấm bài thất bại", Toast.LENGTH_SHORT).show();
                                btnCapture.setEnabled(true);
                            }
                        });
                    }

                    @Override public void onError(@NonNull ImageCaptureException e) {
                        Log.e(TAG, "capture failed", e);
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(),
                                    "Chụp ảnh thất bại, thử lại", Toast.LENGTH_SHORT).show();
                            btnCapture.setEnabled(true);
                        });
                    }
                }
        );
    }

    /** Chuyển ImageProxy (JPEG hoặc YUV) → Bitmap */
    private Bitmap toBitmap(@NonNull ImageProxy image) {
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        if (planes.length == 1 && image.getFormat() == ImageFormat.JPEG) {
            ByteBuffer buf = planes[0].getBuffer();
            byte[] data = new byte[buf.remaining()];
            buf.get(data);
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        // YUV_420_888 → NV21 → JPEG → Bitmap
        ByteBuffer y = planes[0].getBuffer(),
                u = planes[1].getBuffer(),
                v = planes[2].getBuffer();
        int w = image.getWidth(), h = image.getHeight();
        byte[] nv21 = new byte[y.remaining() + u.remaining() + v.remaining()];
        y.get(nv21, 0, y.remaining());
        v.get(nv21, y.remaining(), v.remaining());
        u.get(nv21, y.remaining()+v.remaining(), u.remaining());

        YuvImage yi = new YuvImage(nv21, ImageFormat.NV21, w, h, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yi.compressToJpeg(new Rect(0,0,w,h), 90, out);
        byte[] jpeg = out.toByteArray();
        return BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
    }

    /** Vuốt từ mép trái để thoát */
    private void setupEdgeSwipeToExit() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        final int edgePx = (int)(20 * dm.density);
        final int slop   = ViewConfiguration.get(requireContext()).getScaledTouchSlop();
        GestureDetector.SimpleOnGestureListener listener =
                new GestureDetector.SimpleOnGestureListener(){
                    float startX, startY;
                    @Override public boolean onDown(MotionEvent e){
                        startX = e.getX(); startY = e.getY(); return true;
                    }
                    @Override public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                                      float dx, float dy){
                        float dX = e2.getX() - startX, dY = Math.abs(e2.getY() - startY);
                        if (startX <= edgePx && dX > slop && dY < dX/2) {
                            requireActivity().onBackPressed();
                            return true;
                        }
                        return false;
                    }
                };
        GestureDetector gd = new GestureDetector(requireContext(), listener);
        previewView.setOnTouchListener((v,e)-> gd.onTouchEvent(e));
    }

    @Override public void onDestroyView(){
        super.onDestroyView();
        // khôi phục brightness và full‐screen
        AppCompatActivity act = (AppCompatActivity)getActivity();
        if (act!=null && act.getSupportActionBar()!=null) act.getSupportActionBar().show();
        WindowManager.LayoutParams lp = requireActivity().getWindow().getAttributes();
        lp.screenBrightness = originalBrightness;
        requireActivity().getWindow().setAttributes(lp);
        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        cameraExecutor.shutdown();
    }
}
