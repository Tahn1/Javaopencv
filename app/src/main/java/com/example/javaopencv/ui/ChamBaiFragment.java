package com.example.javaopencv.ui;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;
import com.example.javaopencv.data.AppDatabase;
import com.example.javaopencv.data.entity.GradeResult;
import com.example.javaopencv.omr.OmrGrader;
import com.example.javaopencv.omr.OmrGrader.Result;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChamBaiFragment extends Fragment {
    private static final int REQ_READ_EXTERNAL = 200;

    private Button btnPick;
    private ProgressBar progressBar;
    private ImageView imageViewDebug;
    private RecyclerView rvBatch;
    private BatchResultAdapter batchAdapter;

    private int examId;
    private int questionCount;

    private ActivityResultLauncher<Intent> multiImageLauncher;
    private ActivityResultLauncher<Intent> folderLauncher;

    public ChamBaiFragment() {
        super(R.layout.fragment_cham_bai);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Lấy examId & questionCount từ args nếu có
        Bundle args = getArguments();
        if (args != null) {
            examId        = args.getInt("examId", -1);
            questionCount = args.getInt("questionCount", 20);
        }

        btnPick         = view.findViewById(R.id.btnPickImage);
        progressBar     = view.findViewById(R.id.progressBar);
        imageViewDebug  = view.findViewById(R.id.imageViewDebug);
        rvBatch         = view.findViewById(R.id.rvBatchResults);

        // Setup RecyclerView
        batchAdapter = new BatchResultAdapter();
        rvBatch.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvBatch.setAdapter(batchAdapter);

        // Launcher chọn nhiều ảnh
        multiImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        List<Uri> uris = new ArrayList<>();
                        ClipData clip = data.getClipData();
                        if (clip != null) {
                            for (int i = 0; i < clip.getItemCount(); i++) {
                                uris.add(clip.getItemAt(i).getUri());
                            }
                        } else if (data.getData() != null) {
                            uris.add(data.getData());
                        }
                        if (!uris.isEmpty()) startBatch(uris);
                    }
                }
        );

        // Launcher chọn folder
        folderLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri treeUri = result.getData().getData();
                        if (treeUri != null) {
                            // Cấp quyền đọc lâu dài
                            requireContext().getContentResolver()
                                    .takePersistableUriPermission(
                                            treeUri,
                                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    );
                            scanFolderAndStart(treeUri);
                        }
                    }
                }
        );

        // Hiển thị AlertDialog 2 lựa chọn
        btnPick.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Chọn nguồn ảnh")
                    .setItems(new String[]{"Chọn nhiều ảnh", "Chọn thư mục"},
                            (dialog, which) -> {
                                if (which == 0) pickMultipleImages();
                                else pickFolder();
                            })
                    .show();
        });
    }

    private void pickMultipleImages() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE },
                    REQ_READ_EXTERNAL
            );
        } else {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            multiImageLauncher.launch(i);
        }
    }

    private void pickFolder() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE },
                    REQ_READ_EXTERNAL
            );
        } else {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            i.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION |
                            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            );
            folderLauncher.launch(i);
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
            btnPick.performClick();
        } else {
            Toast.makeText(requireContext(),
                    "Cần quyền đọc bộ nhớ", Toast.LENGTH_SHORT).show();
        }
    }

    private void scanFolderAndStart(Uri treeUri) {
        DocumentFile folder = DocumentFile.fromTreeUri(requireContext(), treeUri);
        if (folder == null || !folder.isDirectory()) {
            Toast.makeText(requireContext(),
                    "Thư mục không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        List<Uri> uris = new ArrayList<>();
        for (DocumentFile doc : folder.listFiles()) {
            if (doc.isFile() && doc.getType() != null && doc.getType().startsWith("image/")) {
                uris.add(doc.getUri());
            }
        }
        if (uris.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Không tìm thấy ảnh", Toast.LENGTH_SHORT).show();
        } else {
            startBatch(uris);
        }
    }

    /** Hiển thị loading, chạy batch, rồi ẩn loading */
    private void startBatch(List<Uri> uris) {
        // show loading
        progressBar.setVisibility(View.VISIBLE);
        btnPick.setEnabled(false);
        imageViewDebug.setVisibility(View.GONE);

        new Thread(() -> {
            List<OMRResult> results = new ArrayList<>();
            for (Uri uri : uris) {
                try {
                    Bitmap bmp = MediaStore.Images.Media.getBitmap(
                            requireContext().getContentResolver(), uri);
                    OpenCVLoader.initDebug();
                    Result r = OmrGrader.grade(bmp, examId, requireContext());
                    if (r == null) continue;

                    double score = ((double) r.correctCount / questionCount) * 10.0;
                    results.add(new OMRResult(r.annotatedBitmap, score, r.maDe, r.sbd));
                    saveResultToDb(r, score);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // Quay về UI
            new Handler(Looper.getMainLooper()).post(() -> {
                progressBar.setVisibility(View.GONE);
                btnPick.setEnabled(true);
                batchAdapter.submitList(results);
            });
        }).start();
    }

    private void saveResultToDb(Result r, double score) {
        String csv = r.answers != null ? TextUtils.join(",", r.answers) : "";
        String path = saveAnnotatedBitmap(r.annotatedBitmap);
        GradeResult gr = new GradeResult(
                examId, r.maDe, r.sbd,
                csv, r.correctCount, questionCount,
                score, path, 0.5f, 0.5f
        );
        AppDatabase.getInstance(requireContext())
                .gradeResultDao().insert(gr);
    }

    private String saveAnnotatedBitmap(Bitmap bmp) {
        try {
            File dir = new File(requireContext()
                    .getExternalFilesDir(null), "debug_results");
            if (!dir.exists()) dir.mkdirs();
            File out = new File(dir, System.currentTimeMillis() + "_omr.jpg");
            try (FileOutputStream fos = new FileOutputStream(out)) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            }
            return out.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ===== Inner Model & Adapter =====

    private static class OMRResult {
        final Bitmap annotated;
        final double score;
        final String maDe, sbd;
        OMRResult(Bitmap annotated, double score, String maDe, String sbd) {
            this.annotated = annotated;
            this.score     = score;
            this.maDe      = maDe;
            this.sbd       = sbd;
        }
    }

    private class BatchResultAdapter
            extends RecyclerView.Adapter<BatchResultAdapter.VH> {

        private final List<OMRResult> list = new ArrayList<>();

        class VH extends RecyclerView.ViewHolder {
            ImageView ivResult;
            TextView  tvScore;
            VH(View itemView) {
                super(itemView);
                ivResult = itemView.findViewById(R.id.ivResult);
                tvScore  = itemView.findViewById(R.id.tvScore);
            }
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_batch_result, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            OMRResult r = list.get(pos);
            h.ivResult.setImageBitmap(r.annotated);
            h.tvScore.setText(String.format(
                    "Mã đề %s – SBD %s – Điểm: %.2f",
                    r.maDe, r.sbd, r.score
            ));
        }

        @Override public int getItemCount() { return list.size(); }

        void submitList(List<OMRResult> data) {
            list.clear();
            list.addAll(data);
            notifyDataSetChanged();
        }
    }
}
