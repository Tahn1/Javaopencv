package com.example.javaopencv.omr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.javaopencv.R;

public class OmrDemoActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView imagePreview;
    private Bitmap capturedBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_omr_demo);

        imagePreview = findViewById(R.id.image_preview);
        Button btnCapture = findViewById(R.id.btn_capture);
        Button btnProcessOmr = findViewById(R.id.btn_process_omr);

        btnCapture.setOnClickListener(v -> openCamera());
        btnProcessOmr.setOnClickListener(v -> processOmr());
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            capturedBitmap = (Bitmap) extras.get("data");
            imagePreview.setImageBitmap(capturedBitmap);
        }
    }

    private void processOmr() {
        if (capturedBitmap != null) {
            OMRProcessor.OMRResult result = OMRProcessor.process(capturedBitmap, this);
            if (result != null) {
                // Hiển thị kết quả
                String sbd = result.sbd;
                String maDe = result.maDe;
                String answers = result.answers.toString();
                showResultDialog(sbd, maDe, answers);
            }
        }
    }

    private void showResultDialog(String sbd, String maDe, String answers) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Kết quả OMR")
                .setMessage("SBD: " + sbd + "\nMã đề: " + maDe + "\nĐáp án: " + answers)
                .setPositiveButton("OK", null)
                .show();
    }
}
