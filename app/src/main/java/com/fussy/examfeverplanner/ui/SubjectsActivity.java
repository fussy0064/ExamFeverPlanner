package com.fussy.examfeverplanner.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fussy.examfeverplanner.R;
import com.fussy.examfeverplanner.data.entity.Subject;
import com.fussy.examfeverplanner.viewmodel.SubjectViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AlertDialog;
import java.util.Random;

public class SubjectsActivity extends AppCompatActivity {

    private SubjectViewModel subjectViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subjects);

        // Toolbar with Back Button
        Toolbar toolbar = findViewById(R.id.toolbar_subjects);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish()); // Handle back press

        // UI Elements
        TextView tvEmpty = findViewById(R.id.tv_empty_subjects);
        RecyclerView recyclerView = findViewById(R.id.recycler_view_subjects);
        FloatingActionButton fabAdd = findViewById(R.id.fab_add_subject);

        // Setup RecyclerView & Adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final SubjectAdapter adapter = new SubjectAdapter(subject -> {
            Intent intent = new Intent(SubjectsActivity.this, TopicsActivity.class);
            intent.putExtra(TopicsActivity.EXTRA_SUBJECT_ID, subject.getId());
            intent.putExtra(TopicsActivity.EXTRA_SUBJECT_NAME, subject.getName());
            startActivity(intent);
        });
        adapter.setDeleteListener(subject -> subjectViewModel.delete(subject));
        recyclerView.setAdapter(adapter);

        // ViewModel Setup
        subjectViewModel = new ViewModelProvider(this).get(SubjectViewModel.class);
        subjectViewModel.getAllSubjects().observe(this, subjects -> {
            if (subjects != null && !subjects.isEmpty()) {
                tvEmpty.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.setSubjects(subjects);
            } else {
                tvEmpty.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });

        // Setup Floating Action Button
        fabAdd.setOnClickListener(v -> showAddSubjectDialog());
    }

    private void showAddSubjectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_subject, null);
        builder.setView(dialogView);

        TextInputEditText editTextSubjectName = dialogView.findViewById(R.id.edit_text_subject_name);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String subjectName = editTextSubjectName.getText().toString().trim();
            if (!subjectName.isEmpty()) {
                String[] colors = { "#EF5350", "#EC407A", "#AB47BC", "#7E57C2",
                        "#5C6BC0", "#42A5F5", "#29B6F6", "#26C6DA",
                        "#26A69A", "#66BB6A", "#9CCC65", "#FFA726" };
                String randomColor = colors[new Random().nextInt(colors.length)];

                Subject newSubject = new Subject(subjectName, randomColor);
                subjectViewModel.insert(newSubject);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
}
