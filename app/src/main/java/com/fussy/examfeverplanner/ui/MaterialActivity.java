package com.fussy.examfeverplanner.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fussy.examfeverplanner.R;
import com.fussy.examfeverplanner.data.entity.StudyMaterial;
import com.fussy.examfeverplanner.viewmodel.StudyMaterialViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

public class MaterialActivity extends AppCompatActivity {

    private StudyMaterialViewModel materialViewModel;
    private TextView tvSelectedFile;
    private String selectedFilePath = null; // null = use URL field

    // File picker launcher
    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        // Take persistable permission so the path survives restarts
                        getContentResolver().takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        selectedFilePath = uri.toString();
                        if (tvSelectedFile != null) {
                            tvSelectedFile.setText("📄 " + getFileNameFromUri(uri));
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material);

        Toolbar toolbar = findViewById(R.id.toolbar_materials);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView tvEmpty = findViewById(R.id.tv_empty_materials);
        RecyclerView recyclerView = findViewById(R.id.recycler_view_materials);
        FloatingActionButton fabAdd = findViewById(R.id.fab_add_material);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final MaterialAdapter adapter = new MaterialAdapter();
        adapter.setDeleteListener(material -> materialViewModel.delete(material));
        recyclerView.setAdapter(adapter);

        materialViewModel = new ViewModelProvider(this).get(StudyMaterialViewModel.class);
        materialViewModel.getMaterialsBySubject(-1).observe(this, materials -> {
            if (materials != null && !materials.isEmpty()) {
                tvEmpty.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.setMaterials(materials);
            } else {
                tvEmpty.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });

        fabAdd.setOnClickListener(v -> showAddMaterialDialog());
    }

    private void showAddMaterialDialog() {
        selectedFilePath = null; // reset per-dialog

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_material, null);
        builder.setView(dialogView);

        TextInputEditText editTextName = dialogView.findViewById(R.id.edit_text_material_name);
        TextInputEditText editTextUrl = dialogView.findViewById(R.id.edit_text_material_url);
        MaterialButton btnPickFile = dialogView.findViewById(R.id.btn_pick_file);
        tvSelectedFile = dialogView.findViewById(R.id.tv_selected_file);

        btnPickFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            filePickerLauncher.launch(intent);
        });

        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = editTextName.getText().toString().trim();
            String url = editTextUrl.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedFilePath != null) {
                // User picked a file
                StudyMaterial newMaterial = new StudyMaterial(title, selectedFilePath, -1, false);
                materialViewModel.insert(newMaterial);
            } else if (!url.isEmpty()) {
                // User entered a URL
                StudyMaterial newMaterial = new StudyMaterial(title, url, -1, true);
                materialViewModel.insert(newMaterial);
            } else {
                Toast.makeText(this, "Please enter a URL or pick a file", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private String getFileNameFromUri(Uri uri) {
        String path = uri.getLastPathSegment();
        return path != null ? path : uri.toString();
    }
}
