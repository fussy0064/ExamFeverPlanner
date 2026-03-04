package com.fussy.examfeverplanner.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fussy.examfeverplanner.R;
import com.fussy.examfeverplanner.data.entity.Topic;
import com.fussy.examfeverplanner.viewmodel.TopicViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

public class TopicsActivity extends AppCompatActivity {

    public static final String EXTRA_SUBJECT_ID = "com.fussy.examfeverplanner.EXTRA_SUBJECT_ID";
    public static final String EXTRA_SUBJECT_NAME = "com.fussy.examfeverplanner.EXTRA_SUBJECT_NAME";

    private TopicViewModel topicViewModel;
    private int subjectId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topics);

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_SUBJECT_ID)) {
            subjectId = intent.getIntExtra(EXTRA_SUBJECT_ID, -1);
        }
        String subjectName = intent.getStringExtra(EXTRA_SUBJECT_NAME);
        if (subjectName == null)
            subjectName = "Topics";

        // Toolbar with Back Button
        Toolbar toolbar = findViewById(R.id.toolbar_topics);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(subjectName + " Topics");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish()); // Handle back press

        // UI Elements
        TextView tvEmpty = findViewById(R.id.tv_empty_topics);
        RecyclerView recyclerView = findViewById(R.id.recycler_view_topics);
        FloatingActionButton fabAdd = findViewById(R.id.fab_add_topic);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final TopicAdapter adapter = new TopicAdapter((topic, isChecked) -> {
            topicViewModel.update(topic);
        });
        adapter.setDeleteListener(topic -> topicViewModel.delete(topic));
        recyclerView.setAdapter(adapter);

        // ViewModel Setup
        topicViewModel = new ViewModelProvider(this).get(TopicViewModel.class);
        if (subjectId != -1) {
            topicViewModel.getTopicsBySubject(subjectId).observe(this, topics -> {
                if (topics != null && !topics.isEmpty()) {
                    tvEmpty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.setTopics(topics);
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            });
        }

        fabAdd.setOnClickListener(v -> showAddTopicDialog());
    }

    private void showAddTopicDialog() {
        if (subjectId == -1)
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_topic, null);
        builder.setView(dialogView);

        TextInputEditText editTextTopicName = dialogView.findViewById(R.id.edit_text_topic_name);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String topicName = editTextTopicName.getText().toString().trim();
            if (!topicName.isEmpty()) {
                Topic newTopic = new Topic(topicName, subjectId);
                topicViewModel.insert(newTopic);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
}
