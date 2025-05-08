package com.example.javaopencv.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.ClassWithCount;
import com.example.javaopencv.data.entity.SchoolClass;
import com.example.javaopencv.databinding.ItemClassBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ClassAdapter
        extends ListAdapter<ClassWithCount, ClassAdapter.VH> {

    public interface OnItemClickListener { void onClick(SchoolClass sc); }
    public interface OnItemLongClickListener { void onLongClick(ClassWithCount cc); }

    private final OnItemClickListener clickListener;
    private final OnItemLongClickListener longClickListener;

    private static final DiffUtil.ItemCallback<ClassWithCount> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull ClassWithCount oldItem,
                                               @NonNull ClassWithCount newItem) {
                    return oldItem.getKlass().getId() == newItem.getKlass().getId();
                }
                @Override
                public boolean areContentsTheSame(@NonNull ClassWithCount oldItem,
                                                  @NonNull ClassWithCount newItem) {
                    SchoolClass a = oldItem.getKlass();
                    SchoolClass b = newItem.getKlass();
                    return a.getName().equals(b.getName())
                            && a.getDateCreated().equals(b.getDateCreated())
                            && oldItem.getStudentCount() == newItem.getStudentCount();
                }
            };

    public ClassAdapter(OnItemClickListener click,
                        OnItemLongClickListener longClick) {
        super(DIFF_CALLBACK);
        this.clickListener = click;
        this.longClickListener = longClick;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemClassBinding b = ItemClassBinding.inflate(inflater, parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(getItem(position), clickListener, longClickListener);
    }

    @Override
    public int getItemCount() {
        return getCurrentList().size();
    }

    /** ViewHolder must be public since it's used in the adapter's signature */
    public static class VH extends RecyclerView.ViewHolder {
        private final ItemClassBinding b;

        public VH(@NonNull ItemClassBinding binding) {
            super(binding.getRoot());
            b = binding;
        }

        void bind(@NonNull ClassWithCount cc,
                  @NonNull OnItemClickListener click,
                  @NonNull OnItemLongClickListener longClick) {
            SchoolClass sc = cc.getKlass();
            Context ctx = b.getRoot().getContext();

            // 1) Tên lớp
            b.tvClassName.setText(sc.getName());

            // 2) Số học sinh
            int count = cc.getStudentCount();
            String studentCountStr = ctx.getResources()
                    .getQuantityString(R.plurals.student_count, count, count);
            b.tvStudentCount.setText(studentCountStr);

            // 3) Ngày tạo: parse rồi format, với null-check
            try {
                SimpleDateFormat inFmt = new SimpleDateFormat("d/M/yyyy", new Locale("vi"));
                Date date = inFmt.parse(sc.getDateCreated());
                if (date != null) {
                    SimpleDateFormat outFmt = new SimpleDateFormat("d-'Thg' M-yyyy", new Locale("vi"));
                    b.tvDateValue.setText(outFmt.format(date));
                } else {
                    b.tvDateValue.setText(sc.getDateCreated());
                }
            } catch (Exception e) {
                b.tvDateValue.setText(sc.getDateCreated());
            }

            // 4) Sự kiện click / long click
            b.getRoot().setOnClickListener(v -> click.onClick(sc));
            b.getRoot().setOnLongClickListener(v -> {
                longClick.onLongClick(cc);
                return true;
            });
        }
    }
}
