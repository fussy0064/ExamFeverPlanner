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
import androidx.recyclerview.widget.DiffUtil;
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
    private OnEditListener editListener;

    public interface OnItemClickListener {
        void onCheckboxClick(Task task, boolean isChecked);
    }

    public interface OnDeleteListener {
        void onDelete(Task task);
    }

    public interface OnEditListener {
        void onEdit(Task task);
    }

    public TaskAdapter(OnItemClickListener listener) {
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
            holder.tvTaskDate.setText(sdf.format(currentTask.getReminderTime()));
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
                .setTitle(R.string.delete_task_title)
                .setMessage(v.getContext().getString(R.string.delete_task_message, currentTask.getTitle()))
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    NotificationHelper.cancelAlarm(v.getContext(), currentTask.getId());
                    int pos = holder.getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        tasks.remove(pos);
                        notifyItemRemoved(pos);
                    }
                    if (deleteListener != null)
                        deleteListener.onDelete(currentTask);
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show());

        holder.btnEdit.setOnClickListener(v -> {
            if (editListener != null)
                editListener.onEdit(currentTask);
        });
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

    public void setTasks(List<Task> newTasks) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return tasks.size();
            }

            @Override
            public int getNewListSize() {
                return newTasks.size();
            }

            @Override
            public boolean areItemsTheSame(int o, int n) {
                return tasks.get(o).getId() == newTasks.get(n).getId();
            }

            @Override
            public boolean areContentsTheSame(int o, int n) {
                Task a = tasks.get(o), b = newTasks.get(n);
                return a.getTitle().equals(b.getTitle())
                        && a.getReminderTime() == b.getReminderTime()
                        && a.isCompleted() == b.isCompleted()
                        && (a.getDescription() == null ? b.getDescription() == null
                                : a.getDescription().equals(b.getDescription()));
            }
        });
        tasks = new ArrayList<>(newTasks);
        result.dispatchUpdatesTo(this);
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
        private final ImageButton btnEdit;

        public TaskHolder(View itemView) {
            super(itemView);
            tvTaskName = itemView.findViewById(R.id.tv_task_name);
            tvTaskDesc = itemView.findViewById(R.id.tv_task_desc);
            tvTaskDate = itemView.findViewById(R.id.tv_task_date);
            checkboxTask = itemView.findViewById(R.id.checkbox_task);
            btnDelete = itemView.findViewById(R.id.btn_delete_task);
            btnEdit = itemView.findViewById(R.id.btn_edit_task);
        }
    }
}
