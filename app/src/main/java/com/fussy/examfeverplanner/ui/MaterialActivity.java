package com.fussy.examfeverplanner.ui;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
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

import java.util.ArrayList;
import java.util.List;

public class MaterialActivity extends BaseActivity {

    // ─── ViewModel ────────────────────────────────────────────────────────────
    private StudyMaterialViewModel materialViewModel;
    private MaterialAdapter adapter;

    // ─── State for the Add dialog ─────────────────────────────────────────────
    private TextView tvAddSelectedFiles;
    private final List<Uri> pendingAddUris = new ArrayList<>();

    // ─── State for Group-Edit dialog ──────────────────────────────────────────
    private String editingGroupName;
    private List<StudyMaterial> editingGroupFiles; // existing files in group
    private TextView tvEditSelectedFiles;
    private final List<Uri> pendingEditUris = new ArrayList<>(); // newly picked files

    // ─── State for single-File edit ───────────────────────────────────────────
    private StudyMaterial editingFile;
    private TextView tvSingleEditFile;

    // ── File pickers ──────────────────────────────────────────────────────────

    /** Multi-select picker for ADD dialog */
    private final ActivityResultLauncher<Intent> addFilePicker = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    pendingAddUris.clear();
                    collectUrisFromResult(result.getData(), pendingAddUris);
                    updateAddFileLabel();
                }
            });

    /** Multi-select picker for GROUP EDIT dialog (add more files to group) */
    private final ActivityResultLauncher<Intent> editGroupFilePicker = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    pendingEditUris.clear();
                    collectUrisFromResult(result.getData(), pendingEditUris);
                    updateEditGroupFileLabel();
                }
            });

    /** Single-select picker for individual FILE edit */
    private final ActivityResultLauncher<Intent> singleFilePicker = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK
                        && result.getData() != null && editingFile != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        persistUri(uri);
                        editingFile.setFilePathOrUrl(uri.toString());
                        editingFile.setLink(false);
                        if (tvSingleEditFile != null)
                            tvSingleEditFile.setText(fileNameOf(uri));
                    }
                }
            });

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material);

        Toolbar toolbar = findViewById(R.id.toolbar_materials);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView tvEmpty = findViewById(R.id.tv_empty_materials);
        RecyclerView recyclerView = findViewById(R.id.recycler_view_materials);
        FloatingActionButton fabAdd = findViewById(R.id.fab_add_material);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MaterialAdapter();

        // Group-level listeners
        adapter.setGroupDeleteListener(groupName -> confirmDeleteGroup(groupName));

        adapter.setGroupEditListener((groupName, files) -> showEditGroupDialog(groupName, files));

        // File-level listeners
        adapter.setFileDeleteListener(material -> {
            materialViewModel.delete(material);
            Toast.makeText(this, "File removed", Toast.LENGTH_SHORT).show();
        });

        adapter.setFileEditListener(material -> showEditSingleFileDialog(material));

        adapter.setFileClickListener(material -> openMaterial(material));

        recyclerView.setAdapter(adapter);

        materialViewModel = new ViewModelProvider(this).get(StudyMaterialViewModel.class);
        // subjectId = -1 is the global "all materials" bucket
        materialViewModel.getMaterialsBySubject(-1).observe(this, materials -> {
            if (materials != null && !materials.isEmpty()) {
                tvEmpty.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.setMaterials(materials);
            } else {
                tvEmpty.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                adapter.setMaterials(new ArrayList<>());
            }
        });

        fabAdd.setOnClickListener(v -> showAddGroupDialog());
    }

    // ─── ADD GROUP dialog ─────────────────────────────────────────────────────

    private void showAddGroupDialog() {
        pendingAddUris.clear();

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_material, null);
        TextInputEditText etTitle = view.findViewById(R.id.edit_text_material_name);
        TextInputEditText etUrl = view.findViewById(R.id.edit_text_material_url);
        MaterialButton btnBrowse = view.findViewById(R.id.btn_pick_file);
        tvAddSelectedFiles = view.findViewById(R.id.tv_selected_file);

        tvAddSelectedFiles.setText("No files selected — Browse to pick one or more");

        btnBrowse.setOnClickListener(v -> addFilePicker.launch(multiFilePicker()));

        new AlertDialog.Builder(this)
                .setTitle("Add Study Materials")
                .setView(view)
                .setPositiveButton("Add", (d, w) -> {
                    String title = etTitle.getText().toString().trim();
                    String url = etUrl.getText().toString().trim();

                    if (title.isEmpty()) {
                        Toast.makeText(this, "Please enter a group title", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (pendingAddUris.isEmpty() && url.isEmpty()) {
                        Toast.makeText(this, "Add at least one file or URL", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int count = 0;
                    // Insert each selected file — all share the same groupName = title
                    for (int i = 0; i < pendingAddUris.size(); i++) {
                        String fileTitle = pendingAddUris.size() == 1
                                ? title
                                : fileNameOf(pendingAddUris.get(i));
                        materialViewModel.insert(new StudyMaterial(
                                fileTitle, pendingAddUris.get(i).toString(),
                                -1, false, title));
                        count++;
                    }
                    // Insert URL if provided
                    if (!url.isEmpty()) {
                        materialViewModel.insert(new StudyMaterial(
                                url, url, -1, true, title));
                        count++;
                    }
                    Toast.makeText(this, "\u2705 " + count + " item(s) added under '" + title + "'",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ─── EDIT GROUP dialog (rename + add more files) ──────────────────────────

    private void showEditGroupDialog(String groupName, List<StudyMaterial> existingFiles) {
        editingGroupName = groupName;
        editingGroupFiles = new ArrayList<>(existingFiles);
        pendingEditUris.clear();

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_material, null);
        TextInputEditText etTitle = view.findViewById(R.id.edit_text_material_name);
        TextInputEditText etUrl = view.findViewById(R.id.edit_text_material_url);
        MaterialButton btnBrowse = view.findViewById(R.id.btn_pick_file);
        tvEditSelectedFiles = view.findViewById(R.id.tv_selected_file);

        etTitle.setText(groupName);
        tvEditSelectedFiles.setText(existingFiles.size() + " existing file(s) — Browse to add more");

        btnBrowse.setOnClickListener(v -> editGroupFilePicker.launch(multiFilePicker()));

        new AlertDialog.Builder(this)
                .setTitle("Edit Group: " + groupName)
                .setView(view)
                .setPositiveButton("Save", (d, w) -> {
                    String newTitle = etTitle.getText().toString().trim();
                    String url = etUrl.getText().toString().trim();

                    if (newTitle.isEmpty()) {
                        Toast.makeText(this, "Please enter a group title", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Rename all existing files in the group
                    for (StudyMaterial m : editingGroupFiles) {
                        m.setGroupName(newTitle);
                        materialViewModel.update(m);
                    }

                    // Insert newly picked files under the (new) group name
                    for (Uri uri : pendingEditUris) {
                        materialViewModel.insert(new StudyMaterial(
                                fileNameOf(uri), uri.toString(),
                                -1, false, newTitle));
                    }

                    // Insert new URL if provided
                    if (!url.isEmpty()) {
                        materialViewModel.insert(new StudyMaterial(
                                url, url, -1, true, newTitle));
                    }

                    int added = pendingEditUris.size() + (url.isEmpty() ? 0 : 1);
                    Toast.makeText(this,
                            "Group updated" + (added > 0 ? ", " + added + " item(s) added" : ""),
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ─── EDIT single FILE dialog ──────────────────────────────────────────────

    private void showEditSingleFileDialog(StudyMaterial material) {
        editingFile = material;

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_material, null);
        TextInputEditText etTitle = view.findViewById(R.id.edit_text_material_name);
        TextInputEditText etUrl = view.findViewById(R.id.edit_text_material_url);
        MaterialButton btnBrowse = view.findViewById(R.id.btn_pick_file);
        tvSingleEditFile = view.findViewById(R.id.tv_selected_file);

        etTitle.setText(material.getTitle());
        if (material.isLink())
            etUrl.setText(material.getFilePathOrUrl());
        tvSingleEditFile.setText(material.isLink()
                ? "Currently a link"
                : fileNameOf(Uri.parse(material.getFilePathOrUrl())));

        btnBrowse.setOnClickListener(v -> singleFilePicker.launch(singleFilePicker()));

        new AlertDialog.Builder(this)
                .setTitle("Edit File")
                .setView(view)
                .setPositiveButton("Save", (d, w) -> {
                    String title = etTitle.getText().toString().trim();
                    String url = etUrl.getText().toString().trim();
                    if (title.isEmpty()) {
                        Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    material.setTitle(title);
                    if (!url.isEmpty()) {
                        material.setFilePathOrUrl(url);
                        material.setLink(true);
                    }
                    materialViewModel.update(material);
                    Toast.makeText(this, "File updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ─── Delete group confirmation ────────────────────────────────────────────

    private void confirmDeleteGroup(String groupName) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Group")
                .setMessage("Delete all files under '" + groupName + "'?")
                .setPositiveButton("Delete All", (d, w) -> {
                    materialViewModel.deleteByGroup(groupName, -1);
                    Toast.makeText(this, "Group deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ─── Helper methods ───────────────────────────────────────────────────────

    private void openMaterial(StudyMaterial material) {
        try {
            Uri uri = Uri.parse(material.getFilePathOrUrl());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (!material.isLink()) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                // Help OS figure out how to open
                intent.setDataAndType(uri, getContentResolver().getType(uri));
            }
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Could not find an app to open this file", Toast.LENGTH_SHORT).show();
        }
    }

    /** Collects all URIs from an Intent result (supports multi-select ClipData) */
    private void collectUrisFromResult(Intent data, List<Uri> out) {
        ClipData clip = data.getClipData();
        if (clip != null) {
            for (int i = 0; i < clip.getItemCount(); i++) {
                Uri uri = clip.getItemAt(i).getUri();
                persistUri(uri);
                out.add(uri);
            }
        } else if (data.getData() != null) {
            Uri uri = data.getData();
            persistUri(uri);
            out.add(uri);
        }
    }

    private void updateAddFileLabel() {
        if (tvAddSelectedFiles == null)
            return;
        if (pendingAddUris.isEmpty()) {
            tvAddSelectedFiles.setText("No files selected");
        } else if (pendingAddUris.size() == 1) {
            tvAddSelectedFiles.setText(fileNameOf(pendingAddUris.get(0)));
        } else {
            tvAddSelectedFiles.setText(pendingAddUris.size() + " files selected");
        }
    }

    private void updateEditGroupFileLabel() {
        if (tvEditSelectedFiles == null)
            return;
        if (pendingEditUris.isEmpty()) {
            tvEditSelectedFiles.setText(editingGroupFiles.size() + " existing file(s)");
        } else {
            tvEditSelectedFiles.setText(editingGroupFiles.size() + " existing + "
                    + pendingEditUris.size() + " new file(s) to add");
        }
    }

    private void persistUri(Uri uri) {
        try {
            getContentResolver().takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception ignored) {
        }
    }

    private String fileNameOf(Uri uri) {
        if (uri == null)
            return "unknown";

        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception ignored) {
            }
        }

        if (result == null) {
            result = uri.getLastPathSegment();
            if (result != null && result.contains("/")) {
                result = result.substring(result.lastIndexOf('/') + 1);
            }
        }

        return result != null && !result.isEmpty() ? result : uri.toString();
    }

    /** Returns an Intent for multi-file selection */
    private Intent multiFilePicker() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        return i;
    }

    /** Returns an Intent for single-file selection */
    private Intent singleFilePicker() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        return i;
    }
}
