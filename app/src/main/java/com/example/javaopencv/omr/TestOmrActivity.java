package com.example.javaopencv.omr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.javaopencv.R;

import org.opencv.android.OpenCVLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class TestOmrActivity extends AppCompatActivity {

    private static final String TAG = "TestOmrActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_omr);

        // Khởi tạo OpenCV
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "OpenCV failed to load!");
        } else {
            Log.d(TAG, "OpenCV loaded successfully");
        }

        ImageView imagePreview = findViewById(R.id.imagePreview);
        TextView textResult = findViewById(R.id.textResult);

        try {
            // Load ảnh test từ thư mục assets (đặt file "test_omr.jpg" vào app/src/main/assets/)
            InputStream is = getAssets().open("phieu20cau3.jpg");
            Bitmap testBitmap = BitmapFactory.decodeStream(is);
            is.close();

            // Hiển thị ảnh test lên ImageView
            imagePreview.setImageBitmap(testBitmap);

            // Gọi OMRProcessor để xử lý ảnh
            OMRProcessor.OMRResult result = OMRProcessor.process(testBitmap, getApplicationContext());

            if (result != null && result.sbd != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("Số báo danh: ").append(result.sbd).append("\n");
                sb.append("Mã đề: ").append(result.maDe).append("\n");
                sb.append("Đáp án:\n");

                List<String> answers = result.answers;
                if (answers != null && !answers.isEmpty()) {
                    for (int i = 0; i < answers.size(); i++) {
                        sb.append((i + 1)).append(". ").append(answers.get(i)).append("\n");
                    }
                } else {
                    sb.append("Không phát hiện được đáp án");
                }
                textResult.setText(sb.toString());
            } else {
                textResult.setText("Không xử lý được OMR!");
            }
        } catch (IOException e) {
            Log.e(TAG, "Lỗi khi load ảnh test", e);
            textResult.setText("Lỗi khi load ảnh test: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi xử lý OMR", e);
            textResult.setText("Lỗi khi xử lý OMR: " + e.getMessage());
        }
    }
}
