package com.fussy.examfeverplanner.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
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

    public interface OnDeleteListener {
        void onDelete(Exam exam);
    }

    public void setDeleteListener(OnDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
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
                dateStr += "  ⏰ " + timeStr;
            }
            holder.tvExamDate.setText(dateStr);

            String location = currentExam.getLocation();
            if (location != null && !location.isEmpty()) {
                holder.tvExamLocation.setText("📍 " + location);
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
                    if (deleteListener != null)
                        deleteListener.onDelete(currentExam);
                })
                .setNegativeButton("Cancel", null)
                .show());
    }

    @Override
    public int getItemCount() {
        return exams.size();
    }

    public void setExams(List<Exam> exams) {
        this.exams = exams;
        notifyDataSetChanged();
    }

    class ExamHolder extends RecyclerView.ViewHolder {
        private final TextView tvExamTitle;
        private final TextView tvExamDate;
        private final TextView tvExamLocation;
        private final TextView tvDaysLeft;
        private final ImageButton btnDelete;

        public ExamHolder(View itemView) {
            super(itemView);
            tvExamTitle = itemView.findViewById(R.id.tv_exam_title);
            tvExamDate = itemView.findViewById(R.id.tv_exam_date);
            tvExamLocation = itemView.findViewById(R.id.tv_exam_location);
            tvDaysLeft = itemView.findViewById(R.id.tv_days_left);
            btnDelete = itemView.findViewById(R.id.btn_delete_exam);
        }
    }
}
