package com.example.javaopencv.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.Exam;
import com.example.javaopencv.data.entity.SchoolClass;
import com.example.javaopencv.viewmodel.ClassViewModel;
import com.example.javaopencv.viewmodel.ExamViewModel;

import java.util.ArrayList;
import java.util.List;

public class NewExamDialogFragment extends DialogFragment {
    private static final String ARG_CLASS_ID = "classId";
    private static final String ARG_EDIT     = "isEdit";
    private static final String ARG_EXAM_ID  = "examId";
    private static final String ARG_TITLE    = "title";
    private static final String ARG_PHIEU    = "phieu";
    private static final String ARG_SO_CAU   = "soCau";
    private static final String ARG_DATE     = "date";

    /** Tạo dialog để thêm mới, classId = 0 cho phép chọn lớp, >0 khóa lớp đó */
    public static NewExamDialogFragment newInstanceForCreate(int classId) {
        Bundle args = new Bundle();
        args.putInt(ARG_CLASS_ID, classId);
        args.putBoolean(ARG_EDIT, false);
        NewExamDialogFragment f = new NewExamDialogFragment();
        f.setArguments(args);
        return f;
    }

    /** Tạo dialog để chỉnh sửa/xóa */
    public static NewExamDialogFragment newInstanceForEdit(Exam exam) {
        Bundle args = new Bundle();
        args.putInt(ARG_CLASS_ID, exam.getClassId() == null ? 0 : exam.getClassId());
        args.putBoolean(ARG_EDIT, true);
        args.putInt(ARG_EXAM_ID, exam.getId());
        args.putString(ARG_TITLE, exam.getTitle());
        args.putString(ARG_PHIEU, exam.getPhieu());
        args.putInt(ARG_SO_CAU, exam.getSoCau());
        args.putString(ARG_DATE, exam.getDate());
        NewExamDialogFragment f = new NewExamDialogFragment();
        f.setArguments(args);
        return f;
    }

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        boolean isEdit     = getArguments().getBoolean(ARG_EDIT, false);
        int     classIdArg = getArguments().getInt(ARG_CLASS_ID, 0);

        View dlgView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_create_exam, null);

        Spinner spClass  = dlgView.findViewById(R.id.spinner_exam_class);
        EditText etTitle = dlgView.findViewById(R.id.et_exam_title);
        EditText etSoCau = dlgView.findViewById(R.id.et_exam_socau);
        Spinner spPhieu  = dlgView.findViewById(R.id.spinner_exam_phieu);

        // --- 1) Load danh sách lớp vào Spinner ---
        ArrayAdapter<String> classNames = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new ArrayList<>()
        );
        List<Integer> classIds = new ArrayList<>();
        classIds.add(0);
        classNames.add("None");
        spClass.setAdapter(classNames);

        ClassViewModel classVm = new ViewModelProvider(
                requireActivity(),
                new ClassViewModel.Factory(requireActivity().getApplication(), 0)
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
            // Chọn lớp mặc định (edit thì lớp cũ, create thì classIdArg)
            int target = isEdit
                    ? getArguments().getInt(ARG_CLASS_ID)
                    : classIdArg;
            int idx = classIds.indexOf(target);
            spClass.setSelection(idx >= 0 ? idx : 0);
            // Nếu tạo bài từ ClassDetail (classIdArg!=0) thì khóa spinner
            if (!isEdit && classIdArg != 0) {
                spClass.setEnabled(false);
            }
        });

        // --- 2) Spinner Phiếu & giới hạn số câu ---
        ArrayAdapter<CharSequence> phieuAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.exam_phieu_array,
                android.R.layout.simple_spinner_dropdown_item
        );
        spPhieu.setAdapter(phieuAdapter);
        spPhieu.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) {
                int max = spPhieu.getSelectedItem().toString().equals("Phiếu 20") ? 20 : 60;
                etSoCau.setFilters(new InputFilter[]{ new InputFilterMinMax(1, max) });
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });

        // --- 3) Prefill & khóa field khi edit ---
        if (isEdit) {
            etTitle.setText(getArguments().getString(ARG_TITLE));
            String ph = getArguments().getString(ARG_PHIEU);
            spPhieu.setSelection(phieuAdapter.getPosition(ph));
            etSoCau.setText(String.valueOf(getArguments().getInt(ARG_SO_CAU)));
            spClass.setEnabled(false);
            spPhieu.setEnabled(false);
            etSoCau.setEnabled(false);
        }

        // --- 4) ViewModel bài kiểm tra ---
        ExamViewModel examVm = new ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())
        ).get(ExamViewModel.class);

        // --- 5) Xây AlertDialog ---
        AlertDialog.Builder b = new AlertDialog.Builder(requireContext())
                .setTitle(isEdit ? "Chỉnh sửa bài kiểm tra" : "Tạo bài kiểm tra")
                .setView(dlgView)
                .setPositiveButton(isEdit ? "LƯU" : "TẠO", null)
                .setNegativeButton("HỦY", null);
        if (isEdit) {
            b.setNeutralButton("XÓA", null);
        }

        AlertDialog dlg = b.create();
        dlg.setOnShowListener(dialog -> {
            // Nút TẠO / LƯU
            Button btnPos = dlg.getButton(AlertDialog.BUTTON_POSITIVE);
            btnPos.setOnClickListener(v -> {
                String title = etTitle.getText().toString().trim();
                String soStr = etSoCau.getText().toString().trim();
                if (TextUtils.isEmpty(title) || TextUtils.isEmpty(soStr)) {
                    Toast.makeText(requireContext(),
                            "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }
                int soCau    = Integer.parseInt(soStr);
                String phieu = spPhieu.getSelectedItem().toString();
                String date  = isEdit
                        ? getArguments().getString(ARG_DATE)
                        : android.text.format.DateFormat
                        .format("dd/MM/yyyy", System.currentTimeMillis())
                        .toString();
                int sel      = spClass.getSelectedItemPosition();
                Integer cid  = classIds.get(sel);

                if (isEdit) {
                    Exam exam = new Exam(
                            getArguments().getInt(ARG_EXAM_ID),
                            cid == 0 ? null : cid,
                            title, phieu, soCau, date
                    );
                    examVm.updateExam(exam);
                } else {
                    Exam exam = new Exam(
                            cid == 0 ? null : cid,
                            title, phieu, soCau, date
                    );
                    examVm.insertExam(exam);
                }
                dlg.dismiss();
            });

            // Nút XÓA (chỉ khi edit)
            if (isEdit) {
                Button btnDel = dlg.getButton(AlertDialog.BUTTON_NEUTRAL);
                btnDel.setOnClickListener(v2 -> {
                    Exam toDel = new Exam(
                            getArguments().getInt(ARG_EXAM_ID),
                            null, "", "", 0, ""
                    );
                    examVm.deleteExam(toDel);
                    dlg.dismiss();
                });
            }
        });

        return dlg;
    }

    /** Giới hạn giá trị nhập số câu giữa min…max */
    public static class InputFilterMinMax implements InputFilter {
        private final int min, max;
        public InputFilterMinMax(int min, int max) { this.min = min; this.max = max; }
        @Override public CharSequence filter(CharSequence src, int start, int end,
                                             Spanned dest, int dstart, int dend) {
            try {
                String result = dest.toString().substring(0, dstart)
                        + src + dest.toString().substring(dend);
                int val = Integer.parseInt(result);
                if (val >= min && val <= max) return null;
            } catch (NumberFormatException ignored){}
            return "";
        }
    }
}
