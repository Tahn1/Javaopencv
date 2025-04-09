package com.example.javaopencv.omr;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageDebugUtils {

    private static final String TAG = "ImageDebugUtils";

    /**
     * Lưu debug ảnh từ Mat thành file JPEG trong thư mục debug của ứng dụng.
     *
     * @param mat      Ảnh đầu vào dạng Mat
     * @param filename Tên file muốn lưu (ví dụ: "aligned_debug.jpg")
     * @param context  Context của Activity hoặc Application
     */
    public static void saveDebugImage(Mat mat, String filename, Context context) {
        Log.d(TAG, "Bắt đầu lưu ảnh debug: " + filename);
        // Chuyển Mat sang Bitmap
        Bitmap bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bmp);

        // Lấy thư mục "debug" trong external files của app.
        // Đây là thư mục riêng của ứng dụng mà không cần quyền WRITE_EXTERNAL_STORAGE trên API 29+
        File debugDir = new File(context.getExternalFilesDir(null), "debug");
        if (!debugDir.exists()) {
            boolean created = debugDir.mkdirs();
            Log.d(TAG, "Tạo debug directory: " + created);
        }

        // Tạo file debug với tên đã chỉ định
        File file = new File(debugDir, filename);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            // Nén Bitmap thành JPEG với chất lượng 90%
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            Log.d(TAG, "Debug image saved at: " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Error saving debug image", e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    // Không cần xử lý thêm
                }
            }
        }
    }
}
