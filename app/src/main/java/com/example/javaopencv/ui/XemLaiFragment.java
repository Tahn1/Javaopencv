package com.example.javaopencv.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.example.javaopencv.data.AppDatabase;
import com.example.javaopencv.data.dao.AnswerDao;
import com.example.javaopencv.data.entity.Answer;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class XemLaiFragment extends Fragment {
    private XemLaiViewModel    vm;
    private GradeResultAdapter adapter;
    private RecyclerView       rv;
    private SwipeRefreshLayout swipeRefresh;

    private int    examId;
    private Integer classId;

    private final Map<String, Student> studentMap = new HashMap<>();
    private final List<GradeResult>    fullResults = new ArrayList<>();

    private int sortMode = 0; // 0=default,1=SBD,2=MaDe

    // Dùng để filter mã đề; load 1 lần khi viewCreated
    private final Set<String> validCodes = new HashSet<>();

    // Executor + Handler để gọi Room off main thread
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler  mainHandler = new Handler(Looper.getMainLooper());

    public XemLaiFragment() {
        super(R.layout.fragment_xem_lai);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            examId = getArguments().getInt("examId", -1);
        }
        requireActivity().getOnBackPressedDispatcher()
                .addCallback(this, new OnBackPressedCallback(true) {
                    @Override public void handleOnBackPressed() {
                        NavHostFragment.findNavController(XemLaiFragment.this).navigateUp();
                    }
                });
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- 1) Menu trên toolbar ---
        MenuHost host = requireActivity();
        host.addMenuProvider(new MenuProvider() {
            @Override public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
                inflater.inflate(R.menu.menu_xem_lai, menu);
            }
            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == android.R.id.home) {
                    navigateUp();
                    return true;
                } else if (id == R.id.action_delete_all) {
                    confirmDeleteAll();
                    return true;
                } else if (id == R.id.action_sort) {
                    showSortDialog();
                    return true;
                } else if (id == R.id.action_export) {
                    exportCsvAndShare();
                    return true;
                } else if (id == R.id.action_export_pdf) {
                    exportSinglePdfAndShare();
                    return true;
                } else if (id == R.id.action_export_answers) {
                    exportAnswerKeyCsvAndShare();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        // --- 2) Swipe to refresh ---
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        swipeRefresh.setOnRefreshListener(this::refreshData);

        // --- 3) RecyclerView + Adapter ---
        rv = view.findViewById(R.id.rvGradeResults);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new GradeResultAdapter();
        rv.setAdapter(adapter);

        // --- 4) ViewModel ---
        vm = new ViewModelProvider(
                this,
                new XemLaiViewModel.Factory(requireActivity().getApplication(), examId)
        ).get(XemLaiViewModel.class);

        // --- 5) Load danh sách mã đề (validCodes) off main thread ---
        executor.execute(() -> {
            AnswerDao dao = AppDatabase.getInstance(requireContext()).answerDao();
            List<String> list = dao.getDistinctCodesSync(examId);
            mainHandler.post(() -> {
                validCodes.clear();
                validCodes.addAll(list);
                applySort();
            });
        });

        // --- 6) Observe kết quả chấm ---
        vm.getResultsForExam().observe(getViewLifecycleOwner(), results -> {
            swipeRefresh.setRefreshing(false);
            fullResults.clear();
            if (results != null) fullResults.addAll(results);
            applySort();
        });

        // --- 7) Observe classId rồi students ---
        vm.getClassIdForExam().observe(getViewLifecycleOwner(), cid -> {
            classId = cid;
            if (cid != null) {
                vm.getStudentsForClass(cid).observe(getViewLifecycleOwner(), list -> {
                    studentMap.clear();
                    if (list != null) {
                        for (Student s : list) {
                            String no = s.getStudentNumber();
                            if (no != null) studentMap.put(no.trim(), s);
                        }
                    }
                    adapter.setStudentMap(studentMap);
                    applySort();
                });
            }
        });

        // --- 8) Item click / long-click ---
        adapter.setOnItemClickListener(item -> {
            Bundle args = new Bundle();
            args.putLong("gradeId", item.id);
            if (classId != null) args.putInt("classId", classId);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_xemLaiFragment_to_gradeDetailFragment, args);
        });
        adapter.setOnItemLongClickListener(this::confirmDelete);

        // --- 9) Kick off first load ---
        swipeRefresh.setRefreshing(true);
        refreshData();
    }

    @Override
    public void onResume() {
        super.onResume();
        swipeRefresh.setRefreshing(true);
        refreshData();
    }

    /** Reload dữ liệu sync và cập nhật fullResults */
    private void refreshData() {
        new Thread(() -> {
            List<GradeResult> list = vm.getResultsListSync();
            mainHandler.post(() -> {
                swipeRefresh.setRefreshing(false);
                fullResults.clear();
                if (list != null) fullResults.addAll(list);
                applySort();
            });
        }).start();
    }

    /** Lọc theo validCodes + studentMap, rồi sắp xếp và submitList */
    private void applySort() {
        List<GradeResult> filtered = new ArrayList<>();
        boolean hasClass = (classId != null && classId >= 0);
        for (GradeResult gr : fullResults) {
            if (hasClass) {
                String sbd  = gr.sbd  != null ? gr.sbd.trim()  : null;
                String code = gr.maDe != null ? gr.maDe.trim() : null;
                boolean okSbd  = sbd  != null && sbd.length() == 6 && studentMap.containsKey(sbd);
                boolean okCode = code != null && code.length() == 3 && validCodes.contains(code);
                if (okSbd && okCode) {
                    filtered.add(gr);
                }
            } else {
                // không có classId → không lọc
                filtered.add(gr);
            }
        }
        // sắp xếp
        if (sortMode == 1) {
            filtered.sort(Comparator.comparing(r -> r.sbd != null ? r.sbd.trim() : ""));
        } else if (sortMode == 2) {
            filtered.sort(Comparator.comparing(r -> r.maDe != null ? r.maDe.trim() : ""));
        }
        adapter.submitList(filtered);
    }

    private void confirmDelete(GradeResult item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa kết quả chấm")
                .setMessage("Bạn có chắc muốn xóa kết quả này không?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (d,w) -> vm.deleteResult(item))
                .show();
    }

    private void confirmDeleteAll() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa tất cả bài thi")
                .setMessage("Bạn có chắc muốn xóa toàn bộ kết quả?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (d,w) -> vm.deleteAllResultsForExam())
                .show();
    }

    private void showSortDialog() {
        String[] opts  = {"Sắp theo SBD ↑","Sắp theo Mã đề ↑"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Chọn cách sắp xếp")
                .setSingleChoiceItems(opts, sortMode-1, (dlg,i)-> sortMode = i+1)
                .setPositiveButton("OK",(d,w)-> applySort())
                .setNegativeButton("Hủy",null)
                .show();
    }

    private void exportCsvAndShare() {
        new Thread(() -> {
            // *** Bỏ examId khi gọi getExamSync() ***
            Exam exam = vm.getExamSync();
            String test    = exam != null ? exam.getTitle()       : "";
            String subject = exam != null ? exam.getSubjectName() : "";
            String date    = exam != null ? exam.getDate()        : "";
            String safeT   = test.trim().replaceAll("\\s+","_");
            String safeS   = subject.trim().replaceAll("\\s+","_");
            String safeD   = date.replace("/","-");
            String meta    = "BaiCham_"+safeT+"_"+safeS+"_"+safeD;

            File dir = requireContext().getExternalFilesDir("exports");
            if (dir!=null && !dir.exists()) dir.mkdirs();
            File csv = new File(dir, meta + ".csv");

            try (FileOutputStream fos = new FileOutputStream(csv);
                 OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                 BufferedWriter bw = new BufferedWriter(osw)) {
                fos.write(0xEF); fos.write(0xBB); fos.write(0xBF);
                bw.write(meta + "\n");
                bw.write("Tên bài,Họ và tên,Số báo danh,Mã đề,Điểm\n");
                for (GradeResult r : vm.getResultsListSync()) {
                    Student s = studentMap.get(r.sbd != null ? r.sbd.trim() : "");
                    bw.write(String.format(
                            "=\"%s\",=\"%s\",=\"%s\",=\"%s\",=\"%.2f\"\n",
                            test,
                            s != null ? s.getName() : "",
                            r.sbd != null ? r.sbd : "",
                            r.maDe != null ? r.maDe : "",
                            r.score
                    ));
                }
                bw.flush();
            } catch (IOException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Lỗi xuất CSV", Toast.LENGTH_SHORT).show()
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

    /**
     * Xuất PDF chi tiết bài chấm với metadata header trên tiêu đề trang đầu
     */
    private void exportSinglePdfAndShare(){
        Toast.makeText(requireContext(),"Đang tạo PDF…",Toast.LENGTH_SHORT).show();
        new Thread(()->{
            try{
                List<GradeResult> list = new ArrayList<>(vm.getResultsListSync());
                if (list.isEmpty()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Không có dữ liệu", Toast.LENGTH_SHORT).show());
                    return;
                }
                list.sort(Comparator.comparing(r->r.sbd!=null?r.sbd.trim():""));
                Exam exam=vm.getExamSync();
                String test=exam!=null?exam.getTitle():"";
                String subj=exam!=null?exam.getSubjectName():"";
                String dt=exam!=null?exam.getDate().replace("/","-"):"";
                String meta="BaiCham_"+test.replaceAll("\\s+","_")+"_"+subj.replaceAll("\\s+","_")+"_"+dt;

                PdfDocument pdf=new PdfDocument();
                Paint titleP=new Paint();titleP.setTextSize(18);titleP.setFakeBoldText(true);
                Paint infoP =new Paint();infoP.setTextSize(14);
                Paint sepP  =new Paint();sepP.setStrokeWidth(1);
                PdfDocument.PageInfo pi=new PdfDocument.PageInfo.Builder(595,842,1).create();
                PdfDocument.Page page=pdf.startPage(pi);
                Canvas c=page.getCanvas();float y=40;
                // draw metadata title
                c.drawText(meta,40,y,titleP);y+=30;
                for(GradeResult r:list){
                    if(y>pi.getPageHeight()-200){pdf.finishPage(page);page=pdf.startPage(pi);c=page.getCanvas();y=40;}
                    String name= r.sbd!=null && studentMap.containsKey(r.sbd.trim())?studentMap.get(r.sbd.trim()).getName():"";
                    c.drawText("SBD: "+r.sbd+(name.isEmpty()?"":" - "+name),40,y,infoP);y+=20;
                    c.drawText("Mã đề: "+r.maDe+String.format(" - %.2f",r.score),40,y,titleP);y+=24;
                    if(r.imagePath!=null){Bitmap bm=BitmapFactory.decodeFile(r.imagePath);
                        if(bm!=null){float maxW=pi.getPageWidth()-80;float scale=maxW/bm.getWidth();
                            c.drawBitmap(Bitmap.createScaledBitmap(bm,(int)(bm.getWidth()*scale),(int)(bm.getHeight()*scale),false),40,y,null);
                            y+=bm.getHeight()*scale+20;bm.recycle();}}
                    c.drawLine(40,y,pi.getPageWidth()-40,y,sepP);y+=24;
                }
                pdf.finishPage(page);
                File dir=requireContext().getExternalFilesDir("exports");if(dir!=null&&!dir.exists())dir.mkdirs();
                String fname=meta+".pdf";
                File out=new File(dir,fname);
                try(FileOutputStream fos=new FileOutputStream(out)){pdf.writeTo(fos);}pdf.close();
                Uri uri=FileProvider.getUriForFile(requireContext(),requireContext().getPackageName()+".fileprovider",out);
                Intent share=new Intent(Intent.ACTION_SEND).setType("application/pdf").putExtra(Intent.EXTRA_STREAM,uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                requireActivity().runOnUiThread(()->startActivity(Intent.createChooser(share,"Chia sẻ PDF")));
            }catch(Exception e){e.printStackTrace();requireActivity().runOnUiThread(()->
                    Toast.makeText(requireContext(),"Lỗi PDF: "+e.getMessage(),Toast.LENGTH_LONG).show());}
        }).start();
    }

    /**
     * Xuất đáp án (answer key) CSV với tên file DapAn_<Test>_<Subject>_<dd-MM-yyyy>
     */
    private void exportAnswerKeyCsvAndShare() {
        new Thread(() -> {
            AnswerDao dao = AppDatabase.getInstance(requireContext()).answerDao();
            List<Answer> list = dao.getAnswersByExamSync(examId);
            if (list == null || list.isEmpty()) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Không có đáp án để xuất", Toast.LENGTH_SHORT).show());
                return;
            }
            Exam exam = vm.getExamSync();
            String test = exam != null ? exam.getTitle() : "";
            String subj = exam != null ? exam.getSubjectName() : "";
            String dt = exam != null ? exam.getDate().replace("/","-") : "";
            String safeT = test.trim().replaceAll("\\s+","_");
            String safeS = subj.trim().replaceAll("\\s+","_");
            String meta = "DapAn_"+safeT+"_"+safeS+"_"+dt;
            StringBuilder sb = new StringBuilder();
            sb.append(meta).append('\n');
            sb.append("Mã đề,Câu số,Đáp án\n");
            for (Answer a : list) {
                sb.append("=\"").append(a.code).append("\"")
                        .append(',').append(a.cauSo).append(',').append(a.dapAn).append('\n');
            }
            String fileName = meta + ".csv";
            try {
                File dir = requireContext().getExternalFilesDir("exports"); if(dir!=null&&!dir.exists())dir.mkdirs();
                File csv = new File(dir, fileName);
                try (FileOutputStream fos = new FileOutputStream(csv);
                     OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                     BufferedWriter bw = new BufferedWriter(osw)) {
                    fos.write(0xEF);fos.write(0xBB);fos.write(0xBF);
                    bw.write(sb.toString());bw.flush();
                }
                Uri uri = FileProvider.getUriForFile(requireContext(),requireContext().getPackageName()+".fileprovider",csv);
                Intent share = new Intent(Intent.ACTION_SEND).setType("text/csv").putExtra(Intent.EXTRA_STREAM,uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                requireActivity().runOnUiThread(()->startActivity(Intent.createChooser(share,"Chia sẻ đáp án")));
            } catch (IOException e) {
                e.printStackTrace(); requireActivity().runOnUiThread(()->
                        Toast.makeText(requireContext(),"Lỗi xuất đáp án",Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void navigateUp() {
        NavHostFragment.findNavController(this).navigateUp();
    }

    private void navigateToDetail(Bundle args) {
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_xemLaiFragment_to_gradeDetailFragment, args);
    }
}
