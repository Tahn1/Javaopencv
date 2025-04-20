package com.example.javaopencv.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.javaopencv.R;
import com.example.javaopencv.omr.OMRProcessor;
import com.example.javaopencv.omr.OMRProcessor.OMRResult;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

public class ChamBaiFragment extends Fragment
        implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG       = "ChamBaiFragment";
    private static final int    REQ_CAMERA = 100;

    private JavaCameraView      javaCameraView;
    private OverlayView         overlayView;
    private ImageView           imageResult;
    private TextView            textViewHeader;
    private boolean             frameCaptured = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate layout
        return inflater.inflate(R.layout.fragment_cham_bai, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // full‑screen
        requireActivity().getWindow()
                .addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // bind views (phải trùng với IDs trong XML)
        javaCameraView = view.findViewById(R.id.javaCameraView);
        overlayView    = view.findViewById(R.id.overlayView);
        imageResult    = view.findViewById(R.id.imageViewResult);
        textViewHeader = view.findViewById(R.id.textViewHeader);

        // camera setup
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);
        javaCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);
    }

    @Override
    public void onResume() {
        super.onResume();

        // 1) kiểm tra quyền
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{ Manifest.permission.CAMERA },
                    REQ_CAMERA
            );
            return;
        }

        // 2) init OpenCV
        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(requireContext(),
                    "Không khởi tạo được OpenCV", Toast.LENGTH_LONG).show();
            return;
        }

        // 3) báo đã có quyền -> enable preview
        javaCameraView.setCameraPermissionGranted();
        javaCameraView.enableView();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (javaCameraView != null) javaCameraView.disableView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (javaCameraView != null) javaCameraView.disableView();
        requireActivity().getWindow()
                .clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQ_CAMERA
                && grantResults.length>0
                && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
            // sau khi cấp quyền, khởi động camera
            if (OpenCVLoader.initDebug()) {
                javaCameraView.setCameraPermissionGranted();
                javaCameraView.enableView();
            }
        } else {
            Toast.makeText(requireContext(),
                    "Cần cấp quyền CAMERA", Toast.LENGTH_SHORT).show();
        }
    }

    // ---------------------------------------------
    // CvCameraViewListener2
    @Override public void onCameraViewStarted(int width, int height) { }
    @Override public void onCameraViewStopped() { }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame f) {
        Mat rgba = f.rgba();

        // TODO: copy đoạn code phát hiện 4 marker, cập nhật overlay và chụp frame tại đây
        // ví dụ:
        // if (!frameCaptured && detect4Markers(rgba)) {
        //     frameCaptured = true;
        //     snapshotAndRunOMR(rgba);
        // }

        return rgba;
    }

    // Ví dụ method chụp snapshot và chạy OMR:
    private void snapshotAndRunOMR(Mat rgba) {
        Mat snap = rgba.clone();
        javaCameraView.disableView();

        requireActivity().runOnUiThread(() -> {
            Bitmap bmp = Bitmap.createBitmap(
                    snap.cols(), snap.rows(),
                    Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(snap, bmp);
            OMRResult res = OMRProcessor.process(bmp, requireContext());
            if (res == null || res.alignedMat == null || res.sbd == null) {
                Toast.makeText(requireContext(),
                        "Không căn chỉnh được, thử lại", Toast.LENGTH_LONG).show();
                frameCaptured = false;
                javaCameraView.enableView();
            } else {
                // hiển thị ảnh
                Bitmap out = Bitmap.createBitmap(
                        res.alignedMat.cols(),
                        res.alignedMat.rows(),
                        Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(res.alignedMat, out);
                imageResult.setImageBitmap(out);
                imageResult.setVisibility(View.VISIBLE);

                textViewHeader.setText(String.format(
                        "Mã đề: %s\nSBD: %s\nĐáp án: %s",
                        res.maDe, res.sbd, res.answers));
                textViewHeader.setVisibility(View.VISIBLE);
            }
        });
    }
}
