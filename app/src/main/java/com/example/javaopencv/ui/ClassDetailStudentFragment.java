package com.example.javaopencv.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.Student;
import com.example.javaopencv.ui.adapter.StudentAdapter;
import com.example.javaopencv.viewmodel.StudentViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class ClassDetailStudentFragment extends Fragment {
    private static final String ARG_CLASS_ID = "classId";

    private int classId;
    private StudentViewModel vm;
    private StudentAdapter adapter;
    private ProgressDialog progressDialog;

    private final androidx.activity.result.ActivityResultLauncher<Intent> importLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    this::onImportResult
            );

    public static ClassDetailStudentFragment newInstance(int classId) {
        Bundle args = new Bundle();
        args.putInt(ARG_CLASS_ID, classId);
        ClassDetailStudentFragment fragment = new ClassDetailStudentFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            classId = getArguments().getInt(ARG_CLASS_ID, -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_class_detail_student, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        vm = new ViewModelProvider(this).get(StudentViewModel.class);

        // Initialize ProgressDialog
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Đang import...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);

        RecyclerView rv = view.findViewById(R.id.rvStudents);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new StudentAdapter(
                student -> { /* click nếu cần */ },
                this::showEditDeleteDialog
        );
        rv.setAdapter(adapter);

        vm.getStudentsForClass(classId)
                .observe(getViewLifecycleOwner(), adapter::submitList);

        FloatingActionButton fab = view.findViewById(R.id.fab_add_student);
        fab.setOnClickListener(v -> showAddOrEditDialog(null));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu,
                                    @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_class_detail_student, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView sv = (SearchView) searchItem.getActionView();
        sv.setQueryHint("Tìm kiếm học sinh...");
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String q) {
                adapter.filter(q);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String t) {
                adapter.filter(t);
                return true;
            }
        });
        sv.setOnCloseListener(() -> {
            adapter.filter("");
            return false;
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_filter) {
            CharSequence[] opts = {"Theo A→Z", "Theo Z→A"};
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Chọn chiều sắp xếp")
                    .setItems(opts, (dialog, which) -> {
                        boolean asc = (which == 0);
                        adapter.setSortOrder(asc);
                        Toast.makeText(
                                requireContext(),
                                asc ? "Sắp xếp A→Z" : "Sắp xếp Z→A",
                                Toast.LENGTH_SHORT
                        ).show();
                    })
                    .show();
            return true;
        }
        else if (id == R.id.action_more) {
            View anchor = requireActivity().findViewById(R.id.action_more);
            PopupMenu popup = new PopupMenu(requireContext(), anchor);
            popup.getMenuInflater()
                    .inflate(R.menu.menu_class_detail_student_more, popup.getMenu());
            popup.setOnMenuItemClickListener(sub -> {
                int subId = sub.getItemId();
                if (subId == R.id.subaction_import) {
                    Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    pick.addCategory(Intent.CATEGORY_OPENABLE);
                    pick.setType("*/*");
                    pick.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                            "application/vnd.ms-excel",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    });
                    pick.addFlags(
                            Intent.FLAG_GRANT_READ_URI_PERMISSION |
                                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                    );
                    progressDialog.show();
                    importLauncher.launch(
                            Intent.createChooser(pick, "Chọn file Excel")
                    );
                    return true;
                }
                else if (subId == R.id.subaction_delete_all) {
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Xóa tất cả học sinh")
                            .setMessage("Bạn có chắc muốn xóa toàn bộ học sinh của lớp này không?")
                            .setNegativeButton("Hủy", null)
                            .setPositiveButton("Xóa", (d, w) -> {
                                vm.deleteAllForClass(classId);
                            })
                            .show();
                    return true;
                }
                return false;
            });
            popup.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onImportResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK &&
                result.getData() != null &&
                result.getData().getData() != null) {

            Uri uri = result.getData().getData();
            requireContext().getContentResolver()
                    .takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
            new Thread(() -> importFromExcel(uri)).start();
        } else {
            if (progressDialog.isShowing()) progressDialog.dismiss();
        }
    }

    private void importFromExcel(Uri uri) {
        ContentResolver resolver = requireContext().getContentResolver();
        DocumentFile doc = DocumentFile.fromSingleUri(requireContext(), uri);
        String name = (doc != null && doc.getName() != null)
                ? doc.getName()
                : "temp_excel";
        File cacheFile = new File(requireContext().getCacheDir(), name);
        try (InputStream is = resolver.openInputStream(uri);
             FileOutputStream fos = new FileOutputStream(cacheFile)) {
            byte[] buf = new byte[8 * 1024];
            int len;
            while ((len = is.read(buf)) > 0) {
                fos.write(buf, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Không copy được file: " + e.getMessage());
            return;
        }

        final AtomicInteger validCount = new AtomicInteger(0);
        final AtomicInteger invalidCount = new AtomicInteger(0);

        try (Workbook wb = name.toLowerCase().endsWith(".xls")
                ? new HSSFWorkbook(new FileInputStream(cacheFile))
                : WorkbookFactory.create(cacheFile)) {

            Sheet sheet = wb.getSheetAt(0);
            List<Student> batch = new ArrayList<>();
            String today = new SimpleDateFormat("d/M/yyyy", Locale.getDefault())
                    .format(new Date());

            int firstRow = sheet.getFirstRowNum() + 1;
            int lastRow = sheet.getLastRowNum();
            for (int i = firstRow; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) { invalidCount.incrementAndGet(); continue; }

                Cell nameCell = row.getCell(0, MissingCellPolicy.RETURN_BLANK_AS_NULL);
                Cell sbdCell  = row.getCell(1, MissingCellPolicy.RETURN_BLANK_AS_NULL);
                if (nameCell == null || sbdCell == null) {
                    invalidCount.incrementAndGet();
                    continue;
                }

                String studentName = nameCell.getCellType() == CellType.STRING
                        ? nameCell.getStringCellValue().trim()
                        : nameCell.toString().trim();
                if (studentName.isEmpty()) {
                    invalidCount.incrementAndGet();
                    continue;
                }

                String sbd;
                if (sbdCell.getCellType() == CellType.NUMERIC) {
                    sbd = String.format(Locale.getDefault(), "%06d", (int) sbdCell.getNumericCellValue());
                } else {
                    String raw = sbdCell.getStringCellValue().trim();
                    if (!raw.matches("\\d+")) {
                        invalidCount.incrementAndGet();
                        continue;
                    }
                    sbd = String.format(Locale.getDefault(), "%06d", Integer.parseInt(raw));
                }

                if (!sbd.matches("\\d{6}")) {
                    invalidCount.incrementAndGet();
                    continue;
                }

                batch.add(new Student(studentName, sbd, classId == -1 ? null : classId, today));
                validCount.incrementAndGet();
            }

            requireActivity().runOnUiThread(() -> {
                if (progressDialog.isShowing()) progressDialog.dismiss();
                for (Student st : batch) vm.insertStudent(st);
                String msg = String.format(
                        Locale.getDefault(),
                        "Import thành công: %d bản ghi\nBỏ qua: %d dòng không hợp lệ",
                        validCount.get(), invalidCount.get()
                );
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
            });

        } catch (Exception e) {
            e.printStackTrace();
            showError("Import lỗi: " + e.getMessage());
        }
    }



    private void showError(String msg) {
        requireActivity().runOnUiThread(() -> {
            if (progressDialog.isShowing()) progressDialog.dismiss();
            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
        });
    }

    private void showAddOrEditDialog(@Nullable Student student) {
        boolean isEdit = student != null;
        View dlg = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_student, null);
        TextInputEditText etName = dlg.findViewById(R.id.etStudentName);
        TextInputEditText etSbd = dlg.findViewById(R.id.etStudentSbd);
        if (isEdit) {
            etName.setText(student.getName());
            etSbd.setText(student.getStudentNumber());
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(isEdit ? "Chỉnh sửa học sinh" : "Thêm học sinh")
                .setView(dlg)
                .setNegativeButton("Hủy", null)
                .setPositiveButton(isEdit ? "Lưu" : "Thêm", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String sbd = etSbd.getText().toString().trim();
                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(sbd)) {
                        Toast.makeText(requireContext(),
                                "Nhập đầy đủ tên và SBD", Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }
                    if (sbd.length() != 6 || !TextUtils.isDigitsOnly(sbd)) {
                        Toast.makeText(requireContext(),
                                "SBD phải gồm 6 chữ số", Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }
                    if (isEdit) {
                        vm.updateStudent(new Student(student.getId(), name, sbd, student.getClassId(), student.getDateCreated()));
                    } else {
                        String now = new SimpleDateFormat("d/M/yyyy", new Locale("vi")).format(new Date());
                        vm.insertStudent(new Student(name, sbd, classId == -1 ? null : classId, now));
                    }
                })
                .show();
    }

    private void showEditDeleteDialog(Student student) {
        String[] opts = {"Chỉnh sửa", "Xóa"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Chọn hành động")
                .setItems(opts, (d, which) -> {
                    if (which == 0) {
                        showAddOrEditDialog(student);
                    } else {
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Xóa học sinh")
                                .setMessage("Bạn có chắc muốn xóa học sinh này không?")
                                .setNegativeButton("Hủy", null)
                                .setPositiveButton("Xóa", (d2, w2) -> {
                                    vm.deleteStudent(student);
                                    Toast.makeText(requireContext(),
                                            "Đã xóa", Toast.LENGTH_SHORT
                                    ).show();
                                })
                                .show();
                    }
                })
                .show();
    }
}