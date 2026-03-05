package com.fussy.examfeverplanner.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fussy.examfeverplanner.R;
import com.fussy.examfeverplanner.data.entity.Task;
import com.fussy.examfeverplanner.utils.NotificationHelper;
import com.fussy.examfeverplanner.viewmodel.TaskViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TaskActivity extends BaseActivity {

    private TaskViewModel taskViewModel;
    private final Calendar selectedDateTime = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        Toolbar toolbar = findViewById(R.id.toolbar_tasks);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView tvEmpty = findViewById(R.id.tv_empty_tasks);
        RecyclerView recyclerView = findViewById(R.id.recycler_view_tasks);
        FloatingActionButton fabAdd = findViewById(R.id.fab_add_task);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        final TaskAdapter adapter = new TaskAdapter((task, isChecked) -> {
            task.setCompleted(isChecked);
            taskViewModel.update(task);
        });
        adapter.setDeleteListener(task -> taskViewModel.delete(task));
        adapter.setEditListener(task -> showEditTaskDialog(task));
        recyclerView.setAdapter(adapter);

        // Swipe left to delete
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder vh, RecyclerView.ViewHolder t) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder vh, int dir) {
                Task task = adapter.getTaskAt(vh.getAdapterPosition());
                NotificationHelper.cancelAlarm(TaskActivity.this, task.getId());
                taskViewModel.delete(task);
                Toast.makeText(TaskActivity.this, "Task deleted", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(recyclerView);

        taskViewModel.getAllTasks().observe(this, tasks -> {
            if (tasks != null && !tasks.isEmpty()) {
                tvEmpty.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.setTasks(tasks);
            } else {
                tvEmpty.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });

        fabAdd.setOnClickListener(v -> showAddTaskDialog());
    }

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        TextInputEditText editTextTaskName = dialogView.findViewById(R.id.edit_text_task_name);
        TextInputEditText editTextTaskDesc = dialogView.findViewById(R.id.edit_text_task_desc);
        TextView tvPickedDateTime = dialogView.findViewById(R.id.tv_picked_date_time);
        TextView btnPickDate = dialogView.findViewById(R.id.btn_pick_date);
        TextView btnPickTime = dialogView.findViewById(R.id.btn_pick_time);

        selectedDateTime.setTimeInMillis(System.currentTimeMillis());

        btnPickDate.setOnClickListener(v -> {
            DatePickerDialog dpd = new DatePickerDialog(this,
                    (view, year, month, day) -> {
                        selectedDateTime.set(Calendar.YEAR, year);
                        selectedDateTime.set(Calendar.MONTH, month);
                        selectedDateTime.set(Calendar.DAY_OF_MONTH, day);
                        refreshDateTimeLabel(tvPickedDateTime);
                    },
                    selectedDateTime.get(Calendar.YEAR),
                    selectedDateTime.get(Calendar.MONTH),
                    selectedDateTime.get(Calendar.DAY_OF_MONTH));
            dpd.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            dpd.show();
        });

        btnPickTime.setOnClickListener(v -> new TimePickerDialog(this,
                (view, hour, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hour);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    selectedDateTime.set(Calendar.SECOND, 0);
                    refreshDateTimeLabel(tvPickedDateTime);
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE), false).show());

        builder.setPositiveButton("Add Task", (dialog, which) -> {
            String title = editTextTaskName.getText().toString().trim();
            String desc = editTextTaskDesc.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter a task title", Toast.LENGTH_SHORT).show();
                return;
            }
            long reminderMs = selectedDateTime.getTimeInMillis();
            Task newTask = new Task(title, desc, reminderMs, -1);
            newTask.setReminderTime(reminderMs);

            taskViewModel.insertAndThen(newTask, insertedId -> {
                if (reminderMs > System.currentTimeMillis()) {
                    String msg = desc.isEmpty() ? "Time to tackle: " + title : desc;
                    NotificationHelper.scheduleAlarm(this, reminderMs,
                            insertedId, "📋 Task Reminder: " + title, msg,
                            NotificationHelper.CHANNEL_ID_TASKS);
                    runOnUiThread(() -> Toast.makeText(this,
                            "Reminder set for " + formatDateTime(reminderMs), Toast.LENGTH_LONG).show());
                }
            });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    /**
     * Edit dialog — pre-fills title, description and date/time from an existing
     * Task
     */
    private void showEditTaskDialog(Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        // Update dialog heading if present
        TextView tvHeading = dialogView.findViewById(R.id.tv_dialog_task_title);
        if (tvHeading != null)
            tvHeading.setText("Edit Task");

        TextInputEditText editTextTaskName = dialogView.findViewById(R.id.edit_text_task_name);
        TextInputEditText editTextTaskDesc = dialogView.findViewById(R.id.edit_text_task_desc);
        TextView tvPickedDateTime = dialogView.findViewById(R.id.tv_picked_date_time);
        TextView btnPickDate = dialogView.findViewById(R.id.btn_pick_date);
        TextView btnPickTime = dialogView.findViewById(R.id.btn_pick_time);

        // Pre-fill existing values
        editTextTaskName.setText(task.getTitle());
        editTextTaskDesc.setText(task.getDescription() != null ? task.getDescription() : "");

        // Use existing reminder time if set, else now
        long existingMs = task.getReminderTime() > 0 ? task.getReminderTime() : System.currentTimeMillis();
        selectedDateTime.setTimeInMillis(existingMs);
        tvPickedDateTime.setText("⏰ " + formatDateTime(existingMs));

        btnPickDate.setOnClickListener(v -> {
            DatePickerDialog dpd = new DatePickerDialog(this,
                    (view, year, month, day) -> {
                        selectedDateTime.set(Calendar.YEAR, year);
                        selectedDateTime.set(Calendar.MONTH, month);
                        selectedDateTime.set(Calendar.DAY_OF_MONTH, day);
                        refreshDateTimeLabel(tvPickedDateTime);
                    },
                    selectedDateTime.get(Calendar.YEAR),
                    selectedDateTime.get(Calendar.MONTH),
                    selectedDateTime.get(Calendar.DAY_OF_MONTH));
            dpd.show();
        });

        btnPickTime.setOnClickListener(v -> new TimePickerDialog(this,
                (view, hour, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hour);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    selectedDateTime.set(Calendar.SECOND, 0);
                    refreshDateTimeLabel(tvPickedDateTime);
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE), false).show());

        builder.setPositiveButton("Save", (dialog, which) -> {
            String title = editTextTaskName.getText().toString().trim();
            String desc = editTextTaskDesc.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter a task title", Toast.LENGTH_SHORT).show();
                return;
            }
            long newReminderMs = selectedDateTime.getTimeInMillis();

            // Cancel the old alarm if any
            NotificationHelper.cancelAlarm(this, task.getId());

            task.setTitle(title);
            task.setDescription(desc);
            task.setDueDate(newReminderMs);
            task.setReminderTime(newReminderMs);
            taskViewModel.update(task);

            // Re-schedule if in the future
            if (newReminderMs > System.currentTimeMillis()) {
                String msg = desc.isEmpty() ? "Time to tackle: " + title : desc;
                NotificationHelper.scheduleAlarm(this, newReminderMs, task.getId(),
                        "📋 Task Reminder: " + title, msg, NotificationHelper.CHANNEL_ID_TASKS);
            }
            Toast.makeText(this, "Task updated ✓", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void refreshDateTimeLabel(TextView label) {
        label.setText("⏰ " + formatDateTime(selectedDateTime.getTimeInMillis()));
    }

    private String formatDateTime(long millis) {
        return new SimpleDateFormat("EEE, MMM d yyyy  hh:mm a", Locale.getDefault()).format(millis);
    }
}
