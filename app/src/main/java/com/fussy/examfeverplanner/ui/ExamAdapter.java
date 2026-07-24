package com.fussy.examfeverplanner.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.fussy.examfeverplanner.R;
import com.fussy.examfeverplanner.data.entity.Exam;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExamAdapter extends RecyclerView.Adapter<ExamAdapter.ExamHolder> {

    private List<Exam> exams = new ArrayList<>();
    private OnDeleteListener deleteListener;
    private OnEditListener editListener;

    public interface OnDeleteListener {
        void onDelete(Exam exam);
    }

    public interface OnEditListener {
        void onEdit(Exam exam);
    }

    public void setDeleteListener(OnDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void setEditListener(OnEditListener editListener) {
        this.editListener = editListener;
    }

    @NonNull
    @Override
    public ExamHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exam, parent, false);
        return new ExamHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamHolder holder, int position) {
        Exam currentExam = exams.get(position);
        holder.tvExamTitle.setText(currentExam.getTitle());

        long dateMillis = currentExam.getDate();
        if (dateMillis > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd yyyy", Locale.getDefault());
            String dateStr = sdf.format(new Date(dateMillis));
            String timeStr = currentExam.getExamTime();
            if (timeStr != null && !timeStr.isEmpty()) {
                dateStr += "  " + timeStr;
            }
            holder.tvExamDate.setText(dateStr);

            String location = currentExam.getLocation();
            if (location != null && !location.isEmpty()) {
                holder.tvExamLocation.setText(location);
                holder.tvExamLocation.setVisibility(View.VISIBLE);
            } else {
                holder.tvExamLocation.setVisibility(View.GONE);
            }

            long diffInMillis = dateMillis - System.currentTimeMillis();
            if (diffInMillis < 0) {
                holder.tvDaysLeft.setText("Done!");
            } else {
                long days = diffInMillis / (1000 * 60 * 60 * 24);
                holder.tvDaysLeft.setText(days + "\nDays\nLeft");
            }
        } else {
            holder.tvExamDate.setText("Date: Not set");
            holder.tvExamLocation.setVisibility(View.GONE);
            holder.tvDaysLeft.setText("?");
        }

        holder.btnDelete.setOnClickListener(v -> new AlertDialog.Builder(v.getContext())
                .setTitle("Delete Exam")
                .setMessage("Delete \"" + currentExam.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (deleteListener != null) {
                        // Instant remove from local list
                        int pos = holder.getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            exams.remove(pos);
                            notifyItemRemoved(pos);
                        }
                        deleteListener.onDelete(currentExam);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show());

        holder.btnEdit.setOnClickListener(v -> {
            if (editListener != null)
                editListener.onEdit(currentExam);
        });
    }

    @Override
    public int getItemCount() {
        return exams.size();
    }

    public void setExams(List<Exam> newExams) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return exams.size();
            }

            @Override
            public int getNewListSize() {
                return newExams.size();
            }

            @Override
            public boolean areItemsTheSame(int o, int n) {
                return exams.get(o).getId() == newExams.get(n).getId();
            }

            @Override
            public boolean areContentsTheSame(int o, int n) {
                Exam a = exams.get(o), b = newExams.get(n);
                return a.getTitle().equals(b.getTitle())
                        && a.getDate() == b.getDate()
                        && (a.getExamTime() == null ? b.getExamTime() == null
                                : a.getExamTime().equals(b.getExamTime()))
                        && (a.getLocation() == null ? b.getLocation() == null
                                : a.getLocation().equals(b.getLocation()));
            }
        });
        exams = new ArrayList<>(newExams);
        result.dispatchUpdatesTo(this);
    }

    class ExamHolder extends RecyclerView.ViewHolder {
        private final TextView tvExamTitle;
        private final TextView tvExamDate;
        private final TextView tvExamLocation;
        private final TextView tvDaysLeft;
        private final ImageButton btnDelete;
        private final ImageButton btnEdit;

        public ExamHolder(View itemView) {
            super(itemView);
            tvExamTitle = itemView.findViewById(R.id.tv_exam_title);
            tvExamDate = itemView.findViewById(R.id.tv_exam_date);
            tvExamLocation = itemView.findViewById(R.id.tv_exam_location);
            tvDaysLeft = itemView.findViewById(R.id.tv_days_left);
            btnDelete = itemView.findViewById(R.id.btn_delete_exam);
            btnEdit = itemView.findViewById(R.id.btn_edit_exam);
        }
    }
}
