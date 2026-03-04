package com.fussy.examfeverplanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.fussy.examfeverplanner.utils.NotificationHelper;
import com.fussy.examfeverplanner.viewmodel.TaskViewModel;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_NOTIFICATION_PERMISSION = 101;
    private TaskViewModel taskViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create notification channels (safe to call repeatedly)
        NotificationHelper.createNotificationChannels(this);

        // Request POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.POST_NOTIFICATIONS },
                        REQUEST_NOTIFICATION_PERMISSION);
            }
        }

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Bind ViewModel Data to UI
        TextView summaryText = findViewById(R.id.tv_tasks_summary);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        taskViewModel.getPendingTasks().observe(this, tasks -> {
            if (tasks != null && !tasks.isEmpty()) {
                summaryText.setText("You have " + tasks.size() + " pending tasks.");
            } else {
                summaryText.setText("All caught up! No pending tasks.");
            }
        });

        MaterialButton btnSubjects = findViewById(R.id.btn_subjects);
        btnSubjects.setOnClickListener(v -> startActivity(
                new Intent(MainActivity.this, com.fussy.examfeverplanner.ui.SubjectsActivity.class)));

        MaterialButton btnTodo = findViewById(R.id.btn_todo);
        btnTodo.setOnClickListener(
                v -> startActivity(new Intent(MainActivity.this, com.fussy.examfeverplanner.ui.TaskActivity.class)));

        MaterialButton btnExams = findViewById(R.id.btn_exams);
        btnExams.setOnClickListener(
                v -> startActivity(new Intent(MainActivity.this, com.fussy.examfeverplanner.ui.ExamActivity.class)));

        MaterialButton btnMaterials = findViewById(R.id.btn_materials);
        btnMaterials.setOnClickListener(v -> startActivity(
                new Intent(MainActivity.this, com.fussy.examfeverplanner.ui.MaterialActivity.class)));
    }
}
