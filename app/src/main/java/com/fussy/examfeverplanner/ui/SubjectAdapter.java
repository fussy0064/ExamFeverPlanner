package com.fussy.examfeverplanner.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.fussy.examfeverplanner.R;
import com.fussy.examfeverplanner.data.entity.Subject;

import java.util.ArrayList;
import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectHolder> {

    private List<Subject> subjects = new ArrayList<>();
    private final OnItemClickListener listener;
    private OnDeleteListener deleteListener;
    private OnEditListener editListener;

    public interface OnItemClickListener {
        void onItemClick(Subject subject);
    }

    public interface OnDeleteListener {
        void onDelete(Subject subject);
    }

    public interface OnEditListener {
        void onEdit(Subject subject);
    }

    public SubjectAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setDeleteListener(OnDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void setEditListener(OnEditListener editListener) {
        this.editListener = editListener;
    }

    @NonNull
    @Override
    public SubjectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subject, parent, false);
        return new SubjectHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectHolder holder, int position) {
        Subject currentSubject = subjects.get(position);
        holder.tvSubjectName.setText(currentSubject.getName());
        holder.tvTopicCount.setText(R.string.topics_available);

        try {
            if (currentSubject.getColorHex() != null && !currentSubject.getColorHex().isEmpty()) {
                holder.llSubjectBg.setBackgroundColor(Color.parseColor(currentSubject.getColorHex()));
            }
        } catch (IllegalArgumentException e) {
            holder.llSubjectBg.setBackgroundColor(Color.DKGRAY);
        }

        holder.llSubjectBg.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (listener != null && pos != RecyclerView.NO_POSITION) {
                listener.onItemClick(subjects.get(pos));
            }
        });

        holder.btnDelete.setOnClickListener(v -> new AlertDialog.Builder(v.getContext())
                .setTitle(R.string.delete_subject_title)
                .setMessage(v.getContext().getString(R.string.delete_subject_message, currentSubject.getName()))
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    int pos = holder.getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        subjects.remove(pos);
                        notifyItemRemoved(pos);
                    }
                    if (deleteListener != null)
                        deleteListener.onDelete(currentSubject);
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show());

        holder.btnEdit.setOnClickListener(v -> {
            if (editListener != null)
                editListener.onEdit(currentSubject);
        });
    }

    @Override
    public int getItemCount() {
        return subjects.size();
    }

    public void setSubjects(List<Subject> newSubjects) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return subjects.size();
            }

            @Override
            public int getNewListSize() {
                return newSubjects.size();
            }

            @Override
            public boolean areItemsTheSame(int o, int n) {
                return subjects.get(o).getId() == newSubjects.get(n).getId();
            }

            @Override
            public boolean areContentsTheSame(int o, int n) {
                Subject a = subjects.get(o), b = newSubjects.get(n);
                return a.getName().equals(b.getName())
                        && (a.getColorHex() == null ? b.getColorHex() == null
                                : a.getColorHex().equals(b.getColorHex()));
            }
        });
        subjects = new ArrayList<>(newSubjects);
        result.dispatchUpdatesTo(this);
    }

    public Subject getSubjectAt(int position) {
        return subjects.get(position);
    }

    class SubjectHolder extends RecyclerView.ViewHolder {
        private TextView tvSubjectName;
        private TextView tvTopicCount;
        private LinearLayout llSubjectBg;
        private ImageButton btnDelete;
        private ImageButton btnEdit;

        public SubjectHolder(View itemView) {
            super(itemView);
            tvSubjectName = itemView.findViewById(R.id.tv_subject_name);
            tvTopicCount = itemView.findViewById(R.id.tv_topic_count);
            llSubjectBg = itemView.findViewById(R.id.ll_subject_bg);
            btnDelete = itemView.findViewById(R.id.btn_delete_subject);
            btnEdit = itemView.findViewById(R.id.btn_edit_subject);
        }
    }
}
