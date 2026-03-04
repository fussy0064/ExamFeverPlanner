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

public class ExamActivity extends AppCompatActivity {

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

        builder.setPositiveButton("Add Exam", (dialog, which) -> {
            String title = editTextExamName.getText().toString().trim();
            String location = editTextExamLocation.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter an exam name", Toast.LENGTH_SHORT).show();
                return;
            }

            // Build date+time
            Calendar calendar = Calendar.getInstance();
            calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                    timePicker.getHour(), timePicker.getMinute(), 0);
            calendar.set(Calendar.MILLISECOND, 0);

            String timeStr = String.format(Locale.getDefault(), "%02d:%02d",
                    timePicker.getHour(), timePicker.getMinute());

            boolean setReminder = checkBoxReminder.isChecked();
            Exam newExam = new Exam(title, calendar.getTimeInMillis(), timeStr, location, -1, setReminder);
            examViewModel.insert(newExam);

            // Schedule notification 1 day before
            if (setReminder) {
                long reminderMs = calendar.getTimeInMillis() - (24 * 60 * 60 * 1000L);
                if (reminderMs > System.currentTimeMillis()) {
                    String msg = "Your exam \"" + title + "\" is tomorrow at " + timeStr
                            + (location.isEmpty() ? "" : " in " + location);
                    // Use title hashCode as ID (exams don't yet have DB id at this point)
                    NotificationHelper.scheduleAlarm(this, reminderMs,
                            title.hashCode(), "📚 Exam Tomorrow: " + title, msg,
                            NotificationHelper.CHANNEL_ID_EXAMS);
                    Toast.makeText(this, "Reminder set 1 day before exam", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
}
