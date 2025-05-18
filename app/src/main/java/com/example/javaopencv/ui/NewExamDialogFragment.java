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
import android.widget.TextView;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NewExamDialogFragment extends DialogFragment {
    private static final String ARG_CLASS_ID  = "classId";
    private static final String ARG_EDIT      = "isEdit";
    private static final String ARG_EXAM_ID   = "examId";
    private static final String ARG_TITLE     = "title";
    private static final String ARG_SO_CAU    = "soCau";
    private static final String ARG_DATE      = "date";
    private static final String ARG_SUBJECT   = "subjectName";

    private boolean isEdit;
    private int classIdArg;
    private int editExamId;
    private String editTitle;
    private int editSoCau;
    private String editDate;
    private String editSubject;

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
        args.putString(ARG_SUBJECT, exam.getSubjectName() == null ? "" : exam.getSubjectName());
        NewExamDialogFragment f = new NewExamDialogFragment();
        f.setArguments(args);
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // 1. Lấy arguments
        Bundle args = getArguments();
        if (args != null) {
            classIdArg = args.getInt(ARG_CLASS_ID, 0);
            isEdit     = args.getBoolean(ARG_EDIT, false);
            if (isEdit) {
                editExamId  = args.getInt(ARG_EXAM_ID);
                editTitle   = args.getString(ARG_TITLE);
                editSoCau   = args.getInt(ARG_SO_CAU);
                editDate    = args.getString(ARG_DATE);
                editSubject = args.getString(ARG_SUBJECT);
            }
        }

        // 2. Inflate layout và set tiêu đề nội tại
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_exam, null);
        TextView tvInnerTitle = view.findViewById(R.id.tvDialogTitle);
        tvInnerTitle.setText(
                isEdit
                        ? getString(R.string.dialog_title_edit_exam)
                        : getString(R.string.dialog_title_create_exam)
        );

        // 3. Tham chiếu view
        EditText etSubject  = view.findViewById(R.id.etSubject);
        EditText etTitle    = view.findViewById(R.id.etExamTitle);
        EditText etSoCau    = view.findViewById(R.id.etSoCau);
        TextView tvPhieu    = view.findViewById(R.id.tvPhieuValue);
        Spinner spClass     = view.findViewById(R.id.spClass);
        Button  btnCancel   = view.findViewById(R.id.btnCancel);
        Button  btnCreate   = view.findViewById(R.id.btnCreate);

        // 4. Phiếu chấm cố định 20 và phóng to cho bằng cỡ edittext
        tvPhieu.setText("20");
        tvPhieu.setTextSize(18);

        // 5. Spinner Lớp
        ArrayAdapter<String> classNames = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<>()
        );
        classNames.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        List<Integer> classIds = new ArrayList<>();
        classIds.add(0);
        classNames.add(getString(R.string.label_none));
        spClass.setAdapter(classNames);

        new ViewModelProvider(
                requireActivity(),
                new ClassViewModel.Factory(requireActivity().getApplication())
        ).get(ClassViewModel.class)
                .getAllClasses()
                .observe(this, list -> {
                    classNames.clear();
                    classIds.clear();
                    classIds.add(0);
                    classNames.add(getString(R.string.label_none));
                    for (SchoolClass c : list) {
                        classIds.add(c.getId());
                        classNames.add(c.getName());
                    }
                    classNames.notifyDataSetChanged();
                    int idx = classIds.indexOf(classIdArg);
                    spClass.setSelection(idx >= 0 ? idx : 0);
                    if (isEdit && classIdArg != 0) spClass.setEnabled(false);
                });

        // 6. Giới hạn số câu 1–20
        etSoCau.setFilters(new InputFilter[]{ new InputFilterMinMax(1, 20) });

        // 7. Prefill khi edit
        if (isEdit) {
            etSubject.setText(editSubject);
            etTitle.setText(editTitle);
            etSoCau.setText(String.valueOf(editSoCau));
            etSoCau.setEnabled(false);
            spClass.setEnabled(false);
            tvPhieu.setEnabled(false);
        }

        // 8. ViewModel Exam
        ExamViewModel examVm = new ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())
        ).get(ExamViewModel.class);

        // 9. Build AlertDialog và show
        AlertDialog dlg = new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();
        dlg.show();

        // 10. Gán listener cho 2 nút custom
        btnCancel.setOnClickListener(v -> dlg.dismiss());
        btnCreate.setOnClickListener(v -> {
            String subject = etSubject.getText().toString().trim();
            String title   = etTitle.getText().toString().trim();

            if (TextUtils.isEmpty(title)) {
                Toast.makeText(requireContext(),
                        "Vui lòng nhập tiêu đề",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (isEdit) {
                // Chỉ update Title và Subject
                Exam exam = new Exam(
                        editExamId,
                        classIdArg == 0 ? null : classIdArg,
                        title,
                        tvPhieu.getText().toString(),
                        editSoCau,
                        editDate,
                        subject
                );
                examVm.updateExam(exam);

            } else {
                // Tạo mới full dữ liệu
                String soStr = etSoCau.getText().toString().trim();
                if (TextUtils.isEmpty(soStr)) {
                    Toast.makeText(requireContext(),
                            "Vui lòng nhập số câu",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                int soCau;
                try { soCau = Integer.parseInt(soStr); }
                catch (NumberFormatException ex) {
                    Toast.makeText(requireContext(),
                            "Số câu không hợp lệ",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                String phieu = tvPhieu.getText().toString();
                String date  = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(new Date());
                int sel = spClass.getSelectedItemPosition();
                Integer cid = classIds.get(sel) == 0 ? null : classIds.get(sel);

                Exam exam = new Exam(
                        cid,
                        title,
                        phieu,
                        soCau,
                        date,
                        subject
                );
                examVm.insertExam(exam);
            }

            dlg.dismiss();
        });

        return dlg;
    }

    /** InputFilter để bắt buộc số trong khoảng [min,max] */
    public static class InputFilterMinMax implements InputFilter {
        private final int min, max;
        public InputFilterMinMax(int min, int max) {
            this.min = min; this.max = max;
        }
        @Override
        public CharSequence filter(CharSequence src, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            try {
                String result = dest.toString().substring(0, dstart)
                        + src
                        + dest.toString().substring(dend);
                int v = Integer.parseInt(result);
                if (v >= min && v <= max) return null;
            } catch (NumberFormatException ignored) {}
            return "";
        }
    }
}
