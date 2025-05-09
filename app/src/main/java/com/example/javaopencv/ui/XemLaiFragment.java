package com.example.javaopencv.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.Exam;
import com.example.javaopencv.data.entity.GradeResult;
import com.example.javaopencv.data.entity.Student;
import com.example.javaopencv.ui.adapter.GradeResultAdapter;
import com.example.javaopencv.viewmodel.XemLaiViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class XemLaiFragment extends Fragment {
    private XemLaiViewModel vm;
    private GradeResultAdapter adapter;
    private int examId;
    private Integer classId;
    private final Map<String, Student> studentMap = new HashMap<>();

    private List<GradeResult> fullResults = new ArrayList<>();
    private int sortMode = 0; // 0 = default, 1 = SBD, 2 = Mã đề

    private SwipeRefreshLayout swipeRefresh;

    public XemLaiFragment() {
        super(R.layout.fragment_xem_lai);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            examId = getArguments().getInt("examId", -1);
        }
        classId = null;
        requireActivity().getOnBackPressedDispatcher()
                .addCallback(this, new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        navigateUp();
                    }
                });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Thiết lập toolbar menu
        MenuHost host = requireActivity();
        host.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
                inflater.inflate(R.menu.menu_xem_lai, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == android.R.id.home) {
                    navigateUp();
                } else if (id == R.id.action_delete_all) {
                    confirmDeleteAll();
                } else if (id == R.id.action_sort) {
                    showSortDialog();
                } else if (id == R.id.action_export) {
                    exportCsvAndShare();
                } else if (id == R.id.action_export_pdf) {
                    exportSinglePdfAndShare();
                } else {
                    return false;
                }
                return true;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        // Swipe to refresh
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        swipeRefresh.setOnRefreshListener(this::refreshData);

        // RecyclerView
        RecyclerView rv = view.findViewById(R.id.rvGradeResults);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new GradeResultAdapter();
        rv.setAdapter(adapter);

        // ViewModel
        vm = new ViewModelProvider(this).get(XemLaiViewModel.class);
        vm.getResultsForExam(examId).observe(getViewLifecycleOwner(), results -> {
            swipeRefresh.setRefreshing(false);
            fullResults = results != null ? results : new ArrayList<>();
            applySort();
        });
        vm.getClassIdForExam(examId).observe(getViewLifecycleOwner(), cid -> {
            classId = cid;
            if (cid != null) {
                vm.getStudentsForClass(cid).observe(getViewLifecycleOwner(), list -> {
                    studentMap.clear();
                    for (Student s : list) {
                        if (s.getStudentNumber() != null) {
                            studentMap.put(s.getStudentNumber().trim(), s);
                        }
                    }
                    adapter.setStudentMap(studentMap);
                });
            }
        });

        adapter.setOnItemClickListener(item -> {
            Bundle args = new Bundle();
            args.putLong("gradeId", item.id);
            if (classId != null) args.putInt("classId", classId);
            navigateToDetail(args);
        });
        adapter.setOnItemLongClickListener(this::confirmDelete);

        swipeRefresh.setRefreshing(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        swipeRefresh.setRefreshing(true);
        refreshData();
    }

    private void refreshData() {
        new Thread(() -> {
            List<GradeResult> results = vm.getResultsListSync(examId);
            requireActivity().runOnUiThread(() -> {
                swipeRefresh.setRefreshing(false);
                fullResults = results != null ? results : new ArrayList<>();
                applySort();
            });
        }).start();
    }

    private void applySort() {
        List<GradeResult> sorted = new ArrayList<>(fullResults);
        if (sortMode == 1) {
            sorted.sort(Comparator.comparing(r -> r.sbd != null ? r.sbd.trim() : ""));
        } else if (sortMode == 2) {
            sorted.sort(Comparator.comparing(r -> r.maDe != null ? r.maDe.trim() : ""));
        }
        adapter.submitList(sorted);
    }

    private void confirmDelete(GradeResult item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa kết quả chấm")
                .setMessage("Bạn có chắc muốn xóa kết quả này không?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (d, w) -> vm.deleteResult(item))
                .show();
    }

    private void confirmDeleteAll() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa tất cả bài thi")
                .setMessage("Bạn có chắc muốn xóa toàn bộ kết quả?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (d, w) -> vm.deleteAllResultsForExam(examId))
                .show();
    }

    private void showSortDialog() {
        String[] options = {"Sắp theo SBD ↑", "Sắp theo Mã Đề ↑"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Chọn cách sắp xếp")
                .setSingleChoiceItems(options, sortMode - 1, (dlg, which) -> sortMode = which + 1)
                .setPositiveButton("OK", (d, w) -> applySort())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void exportCsvAndShare() {
        new Thread(() -> {
            Exam exam = vm.getExamSync(examId);
            String testName = exam != null ? exam.getTitle() : "";
            String subject  = exam != null ? exam.getSubjectName() : "";
            String date     = exam != null ? exam.getDate() : ""; // dd/MM/yyyy
// An toàn tên file
            String safeTestName = testName.trim().replaceAll("\\s+", "_");
            String safeSubject  = subject.trim().replaceAll("\\s+", "_");
            String safeDate     = date.replace("/", "-");
            String fileName = String.format(Locale.getDefault(),
                    "Bai cham_%s_%s_%s.csv",
                    safeTestName, safeSubject, safeDate
            );

// Lưu vào external files/exports để dễ chia sẻ
            File dir = requireContext().getExternalFilesDir("exports");
            if (dir != null && !dir.exists()) dir.mkdirs();
            File csv = new File(dir, fileName);

            try (FileOutputStream fos = new FileOutputStream(csv);
                 OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                 BufferedWriter bw = new BufferedWriter(osw)) {
                // BOM + header
                fos.write(0xEF); fos.write(0xBB); fos.write(0xBF);
                bw.write("Tên bài,Họ Và Tên,Số Báo Danh,Mã Đề,Điểm\n");

                for (GradeResult r : vm.getResultsListSync(examId)) {
                    Student s = studentMap.get(r.sbd != null ? r.sbd.trim() : "");
                    bw.write(String.format(
                            "=\"%s\",=\"%s\",=\"%s\",=\"%s\",=\"%.2f\"\n",
                            testName,
                            s != null ? s.getName() : "",
                            r.sbd != null ? r.sbd : "",
                            r.maDe != null ? r.maDe : "",
                            r.score
                    ));}
                bw.flush();
            } catch (IOException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Lỗi khi xuất CSV", Toast.LENGTH_SHORT).show()
                );
                return;
            }

            Uri uri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".fileprovider", csv);
            Intent share = new Intent(Intent.ACTION_SEND)
                    .setType("text/csv")
                    .putExtra(Intent.EXTRA_STREAM, uri)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            requireActivity().runOnUiThread(() ->
                    startActivity(Intent.createChooser(share, "Chia sẻ CSV"))
            );
        }).start();
    }

    private void exportSinglePdfAndShare() {
        Toast.makeText(requireContext(), "Đang tạo PDF…", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try {
                List<GradeResult> list = new ArrayList<>(vm.getResultsListSync(examId));
                if (list == null || list.isEmpty()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Không có dữ liệu để xuất", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }
                // Sort theo SBD (id) tăng dần
                list.sort(Comparator.comparingInt(r -> {
                    try {
                        return Integer.parseInt(r.sbd != null ? r.sbd.trim() : "0");
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                }));

                PdfDocument pdf = new PdfDocument();
                Paint titlePaint = new Paint();
                titlePaint.setTextSize(18);
                titlePaint.setFakeBoldText(true);
                Paint separatorPaint = new Paint();
                separatorPaint.setStrokeWidth(1);
                Paint infoPaint = new Paint();
                infoPaint.setTextSize(14);

                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
                PdfDocument.Page page = pdf.startPage(pageInfo);
                Canvas c = page.getCanvas();
                float margin = 40;
                float y = margin;

                for (GradeResult r : list) {
                    if (y > pageInfo.getPageHeight() - 200) {
                        pdf.finishPage(page);
                        page = pdf.startPage(pageInfo);
                        c = page.getCanvas();
                        y = margin;
                    }
                    // Lấy tên học sinh nếu có
                    // Lấy tên học sinh nếu có
                    String key = r.sbd != null ? r.sbd.trim() : "";
                    String name = "";
                    if (studentMap.containsKey(key)) {
                        name = studentMap.get(key).getName();
                    }
                    // Vẽ thông tin
                    c.drawText("ID NUMBER: " + (r.sbd != null ? r.sbd : ""), margin, y, titlePaint);
                    y += 28;
                    if (!name.isEmpty()) {
                        c.drawText("NAME: " + name, margin, y, infoPaint);
                        y += 24;
                    }
                    c.drawText("KEY CODE: " + (r.maDe != null ? r.maDe : ""), margin, y, titlePaint);
                    y += 28;
                    c.drawText(String.format("SCORE: %.2f", r.score), margin, y, titlePaint);
                    y += 36;
                    // Vẽ ảnh chấm chi tiết
                    if (r.imagePath != null) {
                        Bitmap bmp = BitmapFactory.decodeFile(r.imagePath);
                        if (bmp != null) {
                            float maxW = pageInfo.getPageWidth() - 2 * margin;
                            float scale = Math.min(1f, maxW / bmp.getWidth());
                            float iw = bmp.getWidth() * scale;
                            float ih = bmp.getHeight() * scale;
                            c.drawBitmap(Bitmap.createScaledBitmap(bmp, (int) iw, (int) ih, false), margin, y, null);
                            bmp.recycle();
                            y += ih + 20;
                        }
                    }
                    // Phân cách
                    c.drawLine(margin, y, pageInfo.getPageWidth() - margin, y, separatorPaint);
                    y += 24;
                }
                pdf.finishPage(page);
                File dir = requireContext().getExternalFilesDir("exports");
                if (dir != null && !dir.exists()) dir.mkdirs();
                // Đặt tên file theo định dạng: Bai cham_<TestName>_<SubjectName>_<dd-MM-yyyy>.pdf
                Exam examInfo = vm.getExamSync(examId);
                String testName = examInfo != null ? examInfo.getTitle() : "";
                String subject = examInfo != null ? examInfo.getSubjectName() : "";
                String date = examInfo != null ? examInfo.getDate() : ""; // dd/MM/yyyy
// An toàn tên file
                String safeTestName = testName.trim().replaceAll("\\s+", "_");
                String safeSubject = subject.trim().replaceAll("\\s+", "_");
                String safeDate = date.replace("/", "-");
                String fileName = String.format(Locale.getDefault(),
                        "Bai_cham_%s_%s_%s.pdf", safeTestName, safeSubject, safeDate
                );
                File outFile = new File(dir, fileName);
                try (FileOutputStream out = new FileOutputStream(outFile)) {
                    pdf.writeTo(out);
                }
                pdf.close();
                Uri uri = FileProvider.getUriForFile(requireContext(),
                        requireContext().getPackageName() + ".fileprovider", outFile);
                Intent share = new Intent(Intent.ACTION_SEND)
                        .setType("application/pdf")
                        .putExtra(Intent.EXTRA_STREAM, uri)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                requireActivity().runOnUiThread(() ->
                        startActivity(Intent.createChooser(share, "Chia sẻ PDF"))
                );
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Lỗi tạo PDF: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    private void createPdfForResult(GradeResult r, File outFile) throws IOException {
        PdfDocument pdf = new PdfDocument();
        PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdf.startPage(info);
        Canvas c = page.getCanvas();
        Paint p = new Paint();
        p.setTextSize(14);
        c.drawText("Mã đề: " + r.maDe, 40, 50, p);
        c.drawText("SBD: " + r.sbd, 40, 70, p);
        c.drawText(String.format(Locale.getDefault(), "Điểm: %.2f", r.score), 40, 90, p);
        pdf.finishPage(page);

        try (FileOutputStream out = new FileOutputStream(outFile)) {
            pdf.writeTo(out);
        }
        pdf.close();
    }

    private void navigateUp() {
        NavHostFragment.findNavController(this).navigateUp();
    }

    private void navigateToDetail(Bundle args) {
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_xemLaiFragment_to_gradeDetailFragment, args);
    }
}
