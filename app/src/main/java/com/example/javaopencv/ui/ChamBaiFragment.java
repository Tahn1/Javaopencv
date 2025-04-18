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
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChamBaiFragment extends Fragment {
    private static final String TAG = "ChamBaiFragment";
    static {
        if (!OpenCVLoader.initDebug()) {
            System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
        }
    }

    private PreviewView previewView;
    private OverlayView overlayView;
    private TextView tvSwipeHint, textViewHeader;
    private ImageView imageViewResult;
    private ImageButton btnCapture;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private int questionCount = 20;
    private float originalBrightness;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cham_bai, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1) Fullscreen + max brightness
        AppCompatActivity act = (AppCompatActivity) getActivity();
        if (act!=null && act.getSupportActionBar()!=null) act.getSupportActionBar().hide();
        WindowManager.LayoutParams wl = requireActivity().getWindow().getAttributes();
        originalBrightness = wl.screenBrightness;
        wl.screenBrightness = 1f;
        requireActivity().getWindow().setAttributes(wl);
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // 2) Đọc questionCount nếu có
        if (getArguments()!=null) {
            questionCount = getArguments().getInt("questionCount", questionCount);
        }

        // 3) Ánh xạ view
        previewView     = view.findViewById(R.id.previewView);
        overlayView     = view.findViewById(R.id.overlayView);
        tvSwipeHint     = view.findViewById(R.id.tvSwipeHint);
        imageViewResult = view.findViewById(R.id.imageViewResult);
        textViewHeader  = view.findViewById(R.id.textViewHeader);
        btnCapture      = view.findViewById(R.id.btn_capture);

        cameraExecutor = Executors.newSingleThreadExecutor();
        tvSwipeHint.postDelayed(() -> tvSwipeHint.setVisibility(View.GONE), 3000);

        startCamera();
        setupEdgeSwipeToExit();

        // 4) Bấm nút chụp thì gọi takePhotoAndGrade()
        btnCapture.setOnClickListener(v -> takePhotoAndGrade());
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> f = ProcessCameraProvider.getInstance(requireContext());
        f.addListener(() -> {
            try {
                ProcessCameraProvider cp = f.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                cp.unbindAll();
                cp.bindToLifecycle(this,
                        new CameraSelector.Builder()
                                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                                .build(),
                        preview, imageCapture);

            } catch (Exception e) {
                Log.e(TAG, "startCamera failed", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void takePhotoAndGrade() {
        if (imageCapture==null) return;
        imageCapture.takePicture(
                ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy proxy) {
                        Bitmap bmp = toBitmap(proxy);
                        proxy.close();
                        Mat mat = new Mat();
                        Utils.bitmapToMat(bmp, mat);

                        // Gọi OMR
                        OmrGrader.Result r = OmrGrader.grade(
                                bmp,
                                getArguments().getInt("examId", -1),
                                requireContext()
                        );

                        requireActivity().runOnUiThread(() -> {
                            previewView.setVisibility(View.GONE);
                            overlayView.setVisibility(View.GONE);
                            btnCapture.setVisibility(View.GONE);
                            textViewHeader.setVisibility(View.VISIBLE);
                            imageViewResult.setVisibility(View.VISIBLE);

                            if (r != null) {
                                textViewHeader.setText(
                                        "Mã đề: " + r.maDe +
                                                "\nSBD: "  + r.sbd +
                                                "\nĐúng: " + r.correctCount +
                                                "/" + questionCount +
                                                "   Điểm: " + String.format("%.1f", r.score)
                                );
                                imageViewResult.setImageBitmap(r.annotatedBitmap);
                            } else {
                                Toast.makeText(requireContext(),
                                        "Chấm bài thất bại", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    @Override
                    public void onError(@NonNull ImageCaptureException e) {
                        Log.e(TAG, "capture failed", e);
                    }
                }
        );
    }

    private Bitmap toBitmap(@NonNull ImageProxy image) {
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        if (planes.length==1 && image.getFormat()==ImageFormat.JPEG) {
            ByteBuffer buf = planes[0].getBuffer();
            byte[] data = new byte[buf.remaining()];
            buf.get(data);
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        // YUV -> JPEG
        ByteBuffer y = planes[0].getBuffer(), u = planes[1].getBuffer(), v = planes[2].getBuffer();
        int w = image.getWidth(), h = image.getHeight();
        byte[] nv21 = new byte[y.remaining()+u.remaining()+v.remaining()];
        y.get(nv21,0,y.remaining());
        v.get(nv21,y.remaining(),v.remaining());
        u.get(nv21,y.remaining()+v.remaining(),u.remaining());

        YuvImage yi = new YuvImage(nv21, ImageFormat.NV21, w, h, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yi.compressToJpeg(new Rect(0,0,w,h), 90, out);
        byte[] bytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void setupEdgeSwipeToExit() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        final int edgePx = (int)(20 * dm.density);
        final int slop   = ViewConfiguration.get(requireContext()).getScaledTouchSlop();
        GestureDetector.SimpleOnGestureListener listener =
                new GestureDetector.SimpleOnGestureListener(){
                    float startX, startY;
                    @Override public boolean onDown(MotionEvent e){
                        startX=e.getX(); startY=e.getY();
                        return true;
                    }
                    @Override public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                                      float dx, float dy){
                        float dX=e2.getX()-startX, dY=Math.abs(e2.getY()-startY);
                        if (startX<=edgePx && dX>slop && dY<dX/2) {
                            requireActivity().onBackPressed();
                            return true;
                        }
                        return false;
                    }
                };
        GestureDetector gd = new GestureDetector(requireContext(), listener);
        previewView.setOnTouchListener((v,e)->gd.onTouchEvent(e));
    }

    @Override public void onDestroyView(){
        super.onDestroyView();
        AppCompatActivity act = (AppCompatActivity)getActivity();
        if (act!=null && act.getSupportActionBar()!=null) act.getSupportActionBar().show();
        WindowManager.LayoutParams wl = requireActivity().getWindow().getAttributes();
        wl.screenBrightness = originalBrightness;
        requireActivity().getWindow().setAttributes(wl);
        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        cameraExecutor.shutdown();
    }
}
