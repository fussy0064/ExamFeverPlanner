package com.fussy.examfeverplanner.ui;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fussy.examfeverplanner.R;
import com.fussy.examfeverplanner.data.entity.Task;
import com.fussy.examfeverplanner.utils.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskHolder> {

    private List<Task> tasks = new ArrayList<>();
    private final OnItemClickListener listener;
    private OnDeleteListener deleteListener;

    public interface OnItemClickListener {
        void onCheckboxClick(Task task, boolean isChecked);
    }

    public interface OnDeleteListener {
        void onDelete(Task task);
    }

    public TaskAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setDeleteListener(OnDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public TaskHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskHolder holder, int position) {
        Task currentTask = tasks.get(position);
        holder.tvTaskName.setText(currentTask.getTitle());
        holder.tvTaskDesc.setText(currentTask.getDescription());

        if (currentTask.getReminderTime() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d, hh:mm a", Locale.getDefault());
            holder.tvTaskDate.setText("⏰ " + sdf.format(currentTask.getReminderTime()));
            holder.tvTaskDate.setVisibility(View.VISIBLE);
        } else {
            holder.tvTaskDate.setVisibility(View.GONE);
        }

        holder.checkboxTask.setOnCheckedChangeListener(null);
        holder.checkboxTask.setChecked(currentTask.isCompleted());
        updateStrikeThrough(holder.tvTaskName, currentTask.isCompleted());

        holder.checkboxTask.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                currentTask.setCompleted(isChecked);
                updateStrikeThrough(holder.tvTaskName, isChecked);
                listener.onCheckboxClick(currentTask, isChecked);
            }
        });

        holder.btnDelete.setOnClickListener(v -> new AlertDialog.Builder(v.getContext())
                .setTitle("Delete Task")
                .setMessage("Delete \"" + currentTask.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    NotificationHelper.cancelAlarm(v.getContext(), currentTask.getId());
                    if (deleteListener != null)
                        deleteListener.onDelete(currentTask);
                })
                .setNegativeButton("Cancel", null)
                .show());
    }

    private void updateStrikeThrough(TextView tv, boolean isCompleted) {
        if (isCompleted) {
            tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            tv.setPaintFlags(tv.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    public Task getTaskAt(int position) {
        return tasks.get(position);
    }

    class TaskHolder extends RecyclerView.ViewHolder {
        private final TextView tvTaskName;
        private final TextView tvTaskDesc;
        private final TextView tvTaskDate;
        private final CheckBox checkboxTask;
        private final ImageButton btnDelete;

        public TaskHolder(View itemView) {
            super(itemView);
            tvTaskName = itemView.findViewById(R.id.tv_task_name);
            tvTaskDesc = itemView.findViewById(R.id.tv_task_desc);
            tvTaskDate = itemView.findViewById(R.id.tv_task_date);
            checkboxTask = itemView.findViewById(R.id.checkbox_task);
            btnDelete = itemView.findViewById(R.id.btn_delete_task);
        }
    }
}
