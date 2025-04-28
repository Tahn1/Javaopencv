package com.example.javaopencv.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.javaopencv.R;
import com.example.javaopencv.data.entity.Student;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Adapter danh sách học sinh, hỗ trợ:
 *  - DiffUtil/ListAdapter
 *  - search/filter by name
 *  - toggle sort order A→Z / Z→A
 */
public class StudentAdapter
        extends ListAdapter<Student, StudentAdapter.StudentViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Student student);
    }
    public boolean isAscending() {
        return ascending;
    }
    public interface OnItemLongClickListener {
        void onItemLongClick(Student student);
    }

    private final OnItemClickListener clickListener;
    private final OnItemLongClickListener longClickListener;

    // Dữ liệu gốc full list và trạng thái search/sort
    private final List<Student> fullList = new ArrayList<>();
    private String currentQuery = "";
    private boolean ascending = true;  // true = A→Z, false = Z→A

    public StudentAdapter(OnItemClickListener clickListener,
                          OnItemLongClickListener longClickListener) {
        super(new DiffUtil.ItemCallback<Student>() {
            @Override
            public boolean areItemsTheSame(@NonNull Student o1,
                                           @NonNull Student o2) {
                return o1.getId() == o2.getId();
            }
            @Override
            public boolean areContentsTheSame(@NonNull Student o1,
                                              @NonNull Student o2) {
                return o1.getName().equals(o2.getName())
                        && o1.getStudentNumber().equals(o2.getStudentNumber());
            }
        });
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder,
                                 int position) {
        holder.bind(getItem(position), clickListener, longClickListener);
    }

    /** Override để lưu fullList, rồi áp dụng sort+filter trước khi hiển thị */
    @Override
    public void submitList(List<Student> list) {
        fullList.clear();
        if (list != null) fullList.addAll(list);
        applySortFilter();
    }

    /** Public API: toggle chiều sắp xếp */
    public void toggleSortOrder() {
        ascending = !ascending;
        applySortFilter();
    }
    public void setSortOrder(boolean asc) {
        this.ascending = asc;
        applySortFilter();
    }

    /** Public API: lọc theo tên */
    public void filter(String query) {
        currentQuery = (query == null ? "" : query.trim().toLowerCase());
        applySortFilter();
    }

    /** Sắp xếp fullList, lọc theo currentQuery, rồi submit cho ListAdapter */
    private void applySortFilter() {
        // 1) sort
        Collections.sort(fullList, (s1, s2) -> {
            int cmp = s1.getName()
                    .compareToIgnoreCase(s2.getName());
            return ascending ? cmp : -cmp;
        });
        // 2) filter
        List<Student> display = new ArrayList<>();
        if (currentQuery.isEmpty()) {
            display.addAll(fullList);
        } else {
            for (Student s : fullList) {
                if (s.getName().toLowerCase().contains(currentQuery)) {
                    display.add(s);
                }
            }
        }
        // 3) submit cho ListAdapter (nội bộ sẽ diff và animate)
        super.submitList(display);
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName, tvSbd;
        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvStudentName);
            tvSbd  = itemView.findViewById(R.id.tvStudentSbd);
        }
        public void bind(Student s,
                         OnItemClickListener clickListener,
                         OnItemLongClickListener longClickListener) {
            tvName.setText(s.getName());
            tvSbd.setText(s.getStudentNumber());
            itemView.setOnClickListener(v -> clickListener.onItemClick(s));
            itemView.setOnLongClickListener(v -> {
                longClickListener.onItemLongClick(s);
                return true;
            });
        }
    }
}
