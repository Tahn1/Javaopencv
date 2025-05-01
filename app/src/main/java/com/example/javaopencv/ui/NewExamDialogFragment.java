package com.example.javaopencv.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.Exam;
import com.example.javaopencv.data.entity.SchoolClass;
import com.example.javaopencv.viewmodel.ClassViewModel;
import com.example.javaopencv.viewmodel.ExamViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Dialog thêm/chỉnh sửa bài kiểm tra, chỉ dùng Phiếu 20, số câu từ 1-20 nhập thủ công
 * Khi sửa (isEdit=true), chỉ cho phép chỉnh tiêu đề, các thông tin khác bị khóa (disabled)
 * Nếu tạo mới từ ClassDetailExamFragment (classIdArg != 0), khóa Spinner lớp
 */
public class NewExamDialogFragment extends DialogFragment {
    private static final String ARG_CLASS_ID = "classId";
    private static final String ARG_EDIT     = "isEdit";
    private static final String ARG_EXAM_ID  = "examId";
    private static final String ARG_TITLE    = "title";
    private static final String ARG_SO_CAU   = "soCau";
    private static final String ARG_DATE     = "date";

    private boolean isEdit;
    private int classIdArg;
    private int editExamId;
    private String editTitle;
    private int editSoCau;
    private String editDate;

    public static NewExamDialogFragment newInstanceForCreate(int classId) {
        Bundle args = new Bundle();
        args.putInt(ARG_CLASS_ID, classId);
        args.putBoolean(ARG_EDIT, false);
        NewExamDialogFragment f = new NewExamDialogFragment();
        f.setArguments(args);
        return f;
    }

    public static NewExamDialogFragment newInstanceForEdit(Exam exam) {
        Bundle args = new Bundle();
        args.putInt(ARG_CLASS_ID, exam.getClassId() == null ? 0 : exam.getClassId());
        args.putBoolean(ARG_EDIT, true);
        args.putInt(ARG_EXAM_ID, exam.getId());
        args.putString(ARG_TITLE, exam.getTitle());
        args.putInt(ARG_SO_CAU, exam.getSoCau());
        args.putString(ARG_DATE, exam.getDate());
        NewExamDialogFragment f = new NewExamDialogFragment();
        f.setArguments(args);
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            classIdArg = args.getInt(ARG_CLASS_ID, 0);
            isEdit     = args.getBoolean(ARG_EDIT, false);
            if (isEdit) {
                editExamId = args.getInt(ARG_EXAM_ID);
                editTitle  = args.getString(ARG_TITLE);
                editSoCau  = args.getInt(ARG_SO_CAU);
                editDate   = args.getString(ARG_DATE);
            }
        }

        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_create_exam, null);
        Spinner spClass  = view.findViewById(R.id.spinner_exam_class);
        Spinner spPhieu  = view.findViewById(R.id.spinner_exam_phieu);
        EditText etTitle = view.findViewById(R.id.et_exam_title);
        EditText etSoCau = view.findViewById(R.id.et_exam_socau);

        // --- Spinner Lớp ---
        ArrayAdapter<String> classNames = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, new ArrayList<>()
        );
        classNames.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        List<Integer> classIds = new ArrayList<>();
        classIds.add(0);
        classNames.add("None");
        spClass.setAdapter(classNames);

        ClassViewModel classVm = new ViewModelProvider(
                requireActivity(),
                new ClassViewModel.Factory(requireActivity().getApplication())
        ).get(ClassViewModel.class);
        classVm.getAllClasses().observe(this, list -> {
            classNames.clear();
            classIds.clear();
            classIds.add(0);
            classNames.add("None");
            for (SchoolClass c : list) {
                classIds.add(c.getId());
                classNames.add(c.getName());
            }
            classNames.notifyDataSetChanged();
            int idx = classIds.indexOf(classIdArg);
            spClass.setSelection(idx >= 0 ? idx : 0);
            if (!isEdit && classIdArg != 0) spClass.setEnabled(false);
        });

        // --- Spinner Phiếu (chỉ Phiếu 20) ---
        ArrayAdapter<String> phieuAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item,
                Collections.singletonList("Phiếu 20")
        );
        phieuAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPhieu.setAdapter(phieuAdapter);
        if (isEdit) spPhieu.setEnabled(false);

        // Giới hạn số câu 1–20
        etSoCau.setFilters(new InputFilter[]{ new InputFilterMinMax(1, 20) });

        // Prefill & disable khi edit
        if (isEdit) {
            etTitle.setText(editTitle);
            etSoCau.setText(String.valueOf(editSoCau));
            spClass.setEnabled(false);
            etSoCau.setEnabled(false);
        }

        ExamViewModel examVm = new ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())
        ).get(ExamViewModel.class);

        AlertDialog dlg = new AlertDialog.Builder(requireContext())
                .setTitle(isEdit ? "Chỉnh sửa bài thi" : "Tạo bài thi mới")
                .setView(view)
                .setNegativeButton("Hủy", (d, w) -> d.dismiss())
                .setPositiveButton(isEdit ? "Lưu" : "Tạo", null)
                .create();

        dlg.setOnShowListener(dialog -> {
            Button btn = dlg.getButton(AlertDialog.BUTTON_POSITIVE);
            btn.setOnClickListener(v -> {
                String title = etTitle.getText().toString().trim();
                String soStr = etSoCau.getText().toString().trim();
                if (TextUtils.isEmpty(title) || TextUtils.isEmpty(soStr)) {
                    Toast.makeText(requireContext(),
                            "Vui lòng nhập tiêu đề và số câu (1–20)",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                int soCau;
                try { soCau = Integer.parseInt(soStr); }
                catch (NumberFormatException e) {
                    Toast.makeText(requireContext(),
                            "Số câu không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }
                String phieu = "Phiếu 20";
                String date  = isEdit ? editDate :
                        new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                .format(new Date());
                int sel = spClass.getSelectedItemPosition();
                Integer cid = classIds.get(sel) == 0 ? null : classIds.get(sel);

                if (isEdit) {
                    Exam exam = new Exam(
                            editExamId, cid, title, phieu, soCau, date, null
                    );
                    examVm.updateExam(exam);
                } else {
                    Exam exam = new Exam(
                            cid, title, phieu, soCau, date
                    );
                    examVm.insertExam(exam);
                }
                dlg.dismiss();
            });
        });

        return dlg;
    }

    /** Filter giá trị nhập phải nằm trong [min,max] */
    public static class InputFilterMinMax implements InputFilter {
        private final int min, max;
        public InputFilterMinMax(int min, int max) { this.min = min; this.max = max; }
        @Override public CharSequence filter(CharSequence src, int start, int end,
                                             Spanned dest, int dstart, int dend) {
            try {
                String result = dest.toString().substring(0, dstart)
                        + src + dest.toString().substring(dend);
                int v = Integer.parseInt(result);
                if (v >= min && v <= max) return null;
            } catch (NumberFormatException ignored) {}
            return "";
        }
    }
}
