package com.fussy.examfeverplanner.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fussy.examfeverplanner.R;
import com.fussy.examfeverplanner.data.entity.Exam;
import com.fussy.examfeverplanner.utils.NotificationHelper;
import com.fussy.examfeverplanner.viewmodel.ExamViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

public class ExamActivity extends BaseActivity {

    private ExamViewModel examViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam);

        Toolbar toolbar = findViewById(R.id.toolbar_exams);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView tvEmpty = findViewById(R.id.tv_empty_exams);
        RecyclerView recyclerView = findViewById(R.id.recycler_view_exams);
        FloatingActionButton fabAdd = findViewById(R.id.fab_add_exam);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final ExamAdapter adapter = new ExamAdapter();
        adapter.setDeleteListener(exam -> examViewModel.delete(exam));
        adapter.setEditListener(exam -> showEditExamDialog(exam));
        recyclerView.setAdapter(adapter);

        examViewModel = new ViewModelProvider(this).get(ExamViewModel.class);
        examViewModel.getAllExams().observe(this, exams -> {
            if (exams != null && !exams.isEmpty()) {
                tvEmpty.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.setExams(exams);
            } else {
                tvEmpty.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });

        fabAdd.setOnClickListener(v -> showAddExamDialog());
    }

    private void showAddExamDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_exam, null);
        builder.setView(dialogView);

        TextInputEditText editTextExamName = dialogView.findViewById(R.id.edit_text_exam_name);
        TextInputEditText editTextExamLocation = dialogView.findViewById(R.id.edit_text_exam_location);
        DatePicker datePicker = dialogView.findViewById(R.id.date_picker_exam);
        TimePicker timePicker = dialogView.findViewById(R.id.time_picker_exam);
        CheckBox checkBoxReminder = dialogView.findViewById(R.id.checkbox_exam_reminder);

        timePicker.setIs24HourView(false);

        builder.setPositiveButton(R.string.action_add_exam, (dialog, which) -> {
            String title = editTextExamName.getText().toString().trim();
            String location = editTextExamLocation.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, R.string.exam_name_required, Toast.LENGTH_SHORT).show();
                return;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                    timePicker.getHour(), timePicker.getMinute(), 0);
            calendar.set(Calendar.MILLISECOND, 0);

            String timeStr = String.format(Locale.getDefault(), "%02d:%02d",
                    timePicker.getHour(), timePicker.getMinute());

            boolean setReminder = checkBoxReminder.isChecked();
            Exam newExam = new Exam(title, calendar.getTimeInMillis(), timeStr, location, -1, setReminder);
            examViewModel.insert(newExam);

            // ── Always fire an instant confirmation notification ──────────────
            String locationSuffix = location.isEmpty() ? "" : getString(R.string.location_suffix_at, location);
            NotificationHelper.sendNow(this,
                    ("confirm_" + title + System.currentTimeMillis()).hashCode(),
                    getString(R.string.exam_saved_title, title),
                    getString(R.string.exam_saved_msg, timeStr, locationSuffix),
                    NotificationHelper.CHANNEL_ID_EXAMS);

            // ── Schedule multi-stage reminders if checkbox is ticked ──────────
            if (setReminder) {
                scheduleExamReminders(title, location, timeStr, calendar.getTimeInMillis());
            }
        });

        builder.setNegativeButton(R.string.action_cancel, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    /** Edit dialog — pre-fills all fields from an existing Exam */
    private void showEditExamDialog(Exam exam) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_exam, null);
        builder.setView(dialogView);

        // Change dialog title label
        TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_exam_title);
        if (tvTitle != null)
            tvTitle.setText(R.string.edit_exam);

        TextInputEditText editTextExamName = dialogView.findViewById(R.id.edit_text_exam_name);
        TextInputEditText editTextExamLocation = dialogView.findViewById(R.id.edit_text_exam_location);
        DatePicker datePicker = dialogView.findViewById(R.id.date_picker_exam);
        TimePicker timePicker = dialogView.findViewById(R.id.time_picker_exam);
        CheckBox checkBoxReminder = dialogView.findViewById(R.id.checkbox_exam_reminder);

        // Pre-fill existing values
        editTextExamName.setText(exam.getTitle());
        editTextExamLocation.setText(exam.getLocation() != null ? exam.getLocation() : "");
        checkBoxReminder.setChecked(exam.isReminderSet());

        timePicker.setIs24HourView(false);

        // Set current date/time from stored value
        Calendar existing = Calendar.getInstance();
        if (exam.getDate() > 0)
            existing.setTimeInMillis(exam.getDate());
        datePicker.updateDate(existing.get(Calendar.YEAR), existing.get(Calendar.MONTH),
                existing.get(Calendar.DAY_OF_MONTH));
        timePicker.setHour(existing.get(Calendar.HOUR_OF_DAY));
        timePicker.setMinute(existing.get(Calendar.MINUTE));

        builder.setPositiveButton(R.string.action_save, (dialog, which) -> {
            String title = editTextExamName.getText().toString().trim();
            String location = editTextExamLocation.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, R.string.exam_name_required, Toast.LENGTH_SHORT).show();
                return;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                    timePicker.getHour(), timePicker.getMinute(), 0);
            calendar.set(Calendar.MILLISECOND, 0);

            String timeStr = String.format(Locale.getDefault(), "%02d:%02d",
                    timePicker.getHour(), timePicker.getMinute());

            // Cancel old alarms first (use old title hash)
            int oldHash = exam.getTitle().hashCode();
            NotificationHelper.cancelAlarm(this, oldHash);
            NotificationHelper.cancelAlarm(this, oldHash + 1);
            NotificationHelper.cancelAlarm(this, oldHash + 2);

            exam.setTitle(title);
            exam.setLocation(location);
            exam.setDate(calendar.getTimeInMillis());
            exam.setExamTime(timeStr);
            exam.setReminderSet(checkBoxReminder.isChecked());
            examViewModel.update(exam);

            // Instant confirmation
            String locationSuffix = location.isEmpty() ? "" : getString(R.string.location_suffix_at, location);
            NotificationHelper.sendNow(this,
                    ("edit_" + title + System.currentTimeMillis()).hashCode(),
                    getString(R.string.exam_updated_title, title),
                    getString(R.string.exam_updated_msg, timeStr, locationSuffix),
                    NotificationHelper.CHANNEL_ID_EXAMS);

            if (checkBoxReminder.isChecked()) {
                scheduleExamReminders(title, location, timeStr, calendar.getTimeInMillis());
            }

            Toast.makeText(this, R.string.exam_updated_toast, Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton(R.string.action_cancel, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    /**
     * Schedule up to 3 alarms for an exam:
     * 1. 1 day (24 h) before
     * 2. 1 hour before
     * 3. Exactly at exam start time
     *
     * If any alarm time is already in the past, scheduleAlarm() fires it
     * immediately.
     */
    private void scheduleExamReminders(String title, String location,
            String timeStr, long examMs) {
        String loc = location.isEmpty() ? "" : getString(R.string.location_suffix_in, location);
        int baseCode = title.hashCode();

        // 1 – 24 h before
        long oneDayBefore = examMs - (24 * 60 * 60 * 1000L);
        NotificationHelper.scheduleAlarm(this, oneDayBefore,
                baseCode,
                getString(R.string.exam_tomorrow_title, title),
                getString(R.string.exam_tomorrow_msg, timeStr, loc),
                NotificationHelper.CHANNEL_ID_EXAMS);

        // 2 – 1 hour before
        long oneHourBefore = examMs - (60 * 60 * 1000L);
        NotificationHelper.scheduleAlarm(this, oneHourBefore,
                baseCode + 1,
                getString(R.string.exam_1hr_title, title),
                getString(R.string.exam_1hr_msg, timeStr, loc),
                NotificationHelper.CHANNEL_ID_EXAMS);

        // 3 – Exactly at exam time
        NotificationHelper.scheduleAlarm(this, examMs,
                baseCode + 2,
                getString(R.string.exam_now_title, title),
                getString(R.string.exam_now_msg, timeStr, loc),
                NotificationHelper.CHANNEL_ID_EXAMS);

        Toast.makeText(this,
                R.string.reminders_set_msg,
                Toast.LENGTH_LONG).show();
    }
}
