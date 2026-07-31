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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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
import com.fussy.examfeverplanner.data.entity.Subject;
import com.fussy.examfeverplanner.data.entity.Topic;
import com.fussy.examfeverplanner.viewmodel.StudyMaterialViewModel;
import com.fussy.examfeverplanner.viewmodel.SubjectViewModel;
import com.fussy.examfeverplanner.viewmodel.TopicViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class MaterialActivity extends BaseActivity {

    // ─── ViewModels ─────────────────────────────────────────────────────────
    private StudyMaterialViewModel materialViewModel;
    private SubjectViewModel subjectViewModel;
    private TopicViewModel topicViewModel;
    private MaterialAdapter adapter;

    private List<Subject> allSubjects = new ArrayList<>();

    // ─── State for the Add dialog ─────────────────────────────────────────────
    private TextView tvAddSelectedFiles;
    private final List<Uri> pendingAddUris = new ArrayList<>();
    private Spinner spinnerAddSubject, spinnerAddTopic;

    // ─── State for Group-Edit dialog ──────────────────────────────────────────
    private String editingGroupName;
    private List<StudyMaterial> editingGroupFiles; // existing files in group
    private TextView tvEditSelectedFiles;
    private final List<Uri> pendingEditUris = new ArrayList<>(); // newly picked files
    private Spinner spinnerEditSubject, spinnerEditTopic;

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
            Toast.makeText(this, R.string.file_removed, Toast.LENGTH_SHORT).show();
        });

        adapter.setFileEditListener(material -> showEditSingleFileDialog(material));

        adapter.setFileClickListener(material -> openMaterial(material));

        recyclerView.setAdapter(adapter);

        materialViewModel = new ViewModelProvider(this).get(StudyMaterialViewModel.class);
        subjectViewModel = new ViewModelProvider(this).get(SubjectViewModel.class);
        topicViewModel = new ViewModelProvider(this).get(TopicViewModel.class);

        subjectViewModel.getAllSubjects().observe(this, subjects -> allSubjects = subjects != null
                ? subjects
                : new ArrayList<>());

        // Study Materials is a global screen — show materials across every subject
        materialViewModel.getAllMaterials().observe(this, materials -> {
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

    // ─── Subject / Topic spinner wiring ────────────────────────────────────────

    /** Fills the subject spinner and wires it to reload the topic spinner. */
    private void setupSubjectTopicSpinners(Spinner subjectSpinner, Spinner topicSpinner,
            Integer preselectSubjectId, Integer preselectTopicId) {

        List<String> subjectNames = new ArrayList<>();
        for (Subject s : allSubjects)
            subjectNames.add(s.getName());

        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, subjectNames);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectSpinner.setAdapter(subjectAdapter);

        int startIndexTmp = 0;
        if (preselectSubjectId != null) {
            for (int i = 0; i < allSubjects.size(); i++) {
                if (allSubjects.get(i).getId() == preselectSubjectId) {
                    startIndexTmp = i;
                    break;
                }
            }
        }
        final int startIndex = startIndexTmp;
        if (!allSubjects.isEmpty())
            subjectSpinner.setSelection(startIndex);

        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= allSubjects.size())
                    return;
                int subjectId = allSubjects.get(position).getId();
                loadTopicsIntoSpinner(topicSpinner, subjectId,
                        position == startIndex ? preselectTopicId : null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        if (!allSubjects.isEmpty()) {
            loadTopicsIntoSpinner(topicSpinner, allSubjects.get(startIndex).getId(), preselectTopicId);
        }
    }

    private void loadTopicsIntoSpinner(Spinner topicSpinner, int subjectId, Integer preselectTopicId) {
        topicViewModel.getTopicsBySubject(subjectId).observe(this, topics -> {
            List<String> names = new ArrayList<>();
            List<Integer> ids = new ArrayList<>();
            names.add(getString(R.string.general_no_topic));
            ids.add(-1);
            if (topics != null) {
                for (Topic t : topics) {
                    names.add(t.getName());
                    ids.add(t.getId());
                }
            }
            ArrayAdapter<String> topicAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, names);
            topicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            topicSpinner.setAdapter(topicAdapter);
            topicSpinner.setTag(ids); // stash the parallel id list for lookup on save

            int sel = 0;
            if (preselectTopicId != null) {
                int idx = ids.indexOf(preselectTopicId);
                if (idx >= 0)
                    sel = idx;
            }
            topicSpinner.setSelection(sel);
        });
    }

    @SuppressWarnings("unchecked")
    private int selectedTopicId(Spinner topicSpinner) {
        Object tag = topicSpinner.getTag();
        int position = topicSpinner.getSelectedItemPosition();
        if (tag instanceof List && position >= 0 && position < ((List<Integer>) tag).size()) {
            return ((List<Integer>) tag).get(position);
        }
        return -1;
    }

    private Subject selectedSubject(Spinner subjectSpinner) {
        int position = subjectSpinner.getSelectedItemPosition();
        if (position >= 0 && position < allSubjects.size())
            return allSubjects.get(position);
        return null;
    }

    /** Builds the folder/group label shown in the list: "Subject • Topic • Title" */
    private String buildGroupName(String subjectName, String topicName, String title) {
        StringBuilder sb = new StringBuilder(subjectName);
        if (topicName != null)
            sb.append(" • ").append(topicName);
        if (title != null && !title.isEmpty())
            sb.append(" • ").append(title);
        return sb.toString();
    }

    // ─── ADD GROUP dialog ─────────────────────────────────────────────────────

    private void showAddGroupDialog() {
        if (allSubjects.isEmpty()) {
            Toast.makeText(this, R.string.subject_required, Toast.LENGTH_SHORT).show();
            return;
        }
        pendingAddUris.clear();

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_material, null);
        TextInputEditText etTitle = view.findViewById(R.id.edit_text_material_name);
        TextInputEditText etUrl = view.findViewById(R.id.edit_text_material_url);
        MaterialButton btnBrowse = view.findViewById(R.id.btn_pick_file);
        tvAddSelectedFiles = view.findViewById(R.id.tv_selected_file);
        spinnerAddSubject = view.findViewById(R.id.spinner_material_subject);
        spinnerAddTopic = view.findViewById(R.id.spinner_material_topic);

        setupSubjectTopicSpinners(spinnerAddSubject, spinnerAddTopic, null, null);

        tvAddSelectedFiles.setText(R.string.no_files_selected_browse);

        btnBrowse.setOnClickListener(v -> addFilePicker.launch(multiFilePicker()));

        new AlertDialog.Builder(this)
                .setTitle(R.string.add_study_materials)
                .setView(view)
                .setPositiveButton(R.string.action_add, (d, w) -> {
                    String title = etTitle.getText().toString().trim();
                    String url = etUrl.getText().toString().trim();

                    if (pendingAddUris.isEmpty() && url.isEmpty()) {
                        Toast.makeText(this, R.string.add_file_or_url_required, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Subject subject = selectedSubject(spinnerAddSubject);
                    if (subject == null) {
                        Toast.makeText(this, R.string.subject_required, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int topicId = selectedTopicId(spinnerAddTopic);
                    String topicName = topicId == -1 ? null
                            : ((ArrayAdapter<String>) spinnerAddTopic.getAdapter())
                                    .getItem(spinnerAddTopic.getSelectedItemPosition());
                    String groupName = buildGroupName(subject.getName(), topicName, title);

                    int count = 0;
                    // Insert each selected file — all share the same groupName
                    for (int i = 0; i < pendingAddUris.size(); i++) {
                        String fileTitle = pendingAddUris.size() == 1 && !title.isEmpty()
                                ? title
                                : fileNameOf(pendingAddUris.get(i));
                        StudyMaterial m = new StudyMaterial(
                                fileTitle, pendingAddUris.get(i).toString(),
                                subject.getId(), false, groupName);
                        m.setTopicId(topicId);
                        materialViewModel.insert(m);
                        count++;
                    }
                    // Insert URL if provided
                    if (!url.isEmpty()) {
                        StudyMaterial m = new StudyMaterial(
                                title.isEmpty() ? url : title, url,
                                subject.getId(), true, groupName);
                        m.setTopicId(topicId);
                        materialViewModel.insert(m);
                        count++;
                    }
                    Toast.makeText(this, getString(R.string.items_added_under, count, groupName),
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.action_cancel, null)
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
        spinnerEditSubject = view.findViewById(R.id.spinner_material_subject);
        spinnerEditTopic = view.findViewById(R.id.spinner_material_topic);

        StudyMaterial first = existingFiles.isEmpty() ? null : existingFiles.get(0);
        setupSubjectTopicSpinners(spinnerEditSubject, spinnerEditTopic,
                first != null ? first.getSubjectId() : null,
                first != null ? first.getTopicId() : null);

        tvEditSelectedFiles.setText(getString(R.string.existing_files_browse_more, existingFiles.size()));

        btnBrowse.setOnClickListener(v -> editGroupFilePicker.launch(multiFilePicker()));

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.edit_group_title, groupName))
                .setView(view)
                .setPositiveButton(R.string.action_save, (d, w) -> {
                    String newTitle = etTitle.getText().toString().trim();
                    String url = etUrl.getText().toString().trim();

                    Subject subject = selectedSubject(spinnerEditSubject);
                    if (subject == null) {
                        Toast.makeText(this, R.string.subject_required, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int topicId = selectedTopicId(spinnerEditTopic);
                    String topicName = topicId == -1 ? null
                            : ((ArrayAdapter<String>) spinnerEditTopic.getAdapter())
                                    .getItem(spinnerEditTopic.getSelectedItemPosition());
                    String newGroupName = buildGroupName(subject.getName(), topicName,
                            newTitle.isEmpty() ? null : newTitle);

                    // Update subject/topic/group on all existing files in the group
                    for (StudyMaterial m : editingGroupFiles) {
                        m.setGroupName(newGroupName);
                        m.setSubjectId(subject.getId());
                        m.setTopicId(topicId);
                        materialViewModel.update(m);
                    }

                    // Insert newly picked files under the (new) group
                    for (Uri uri : pendingEditUris) {
                        StudyMaterial m = new StudyMaterial(
                                fileNameOf(uri), uri.toString(),
                                subject.getId(), false, newGroupName);
                        m.setTopicId(topicId);
                        materialViewModel.insert(m);
                    }

                    // Insert new URL if provided
                    if (!url.isEmpty()) {
                        StudyMaterial m = new StudyMaterial(url, url, subject.getId(), true, newGroupName);
                        m.setTopicId(topicId);
                        materialViewModel.insert(m);
                    }

                    int added = pendingEditUris.size() + (url.isEmpty() ? 0 : 1);
                    Toast.makeText(this,
                            added > 0 ? getString(R.string.group_updated_with_added, added)
                                    : getString(R.string.group_updated),
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.action_cancel, null)
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

        // Single-file edit also lets the user re-file it under a different subject/topic
        Spinner subjectSpinner = view.findViewById(R.id.spinner_material_subject);
        Spinner topicSpinner = view.findViewById(R.id.spinner_material_topic);
        setupSubjectTopicSpinners(subjectSpinner, topicSpinner,
                material.getSubjectId(), material.getTopicId());

        etTitle.setText(material.getTitle());
        if (material.isLink())
            etUrl.setText(material.getFilePathOrUrl());
        tvSingleEditFile.setText(material.isLink()
                ? getString(R.string.currently_a_link)
                : fileNameOf(Uri.parse(material.getFilePathOrUrl())));

        btnBrowse.setOnClickListener(v -> singleFilePicker.launch(singleFilePicker()));

        new AlertDialog.Builder(this)
                .setTitle(R.string.edit_file_title)
                .setView(view)
                .setPositiveButton(R.string.action_save, (d, w) -> {
                    String title = etTitle.getText().toString().trim();
                    String url = etUrl.getText().toString().trim();
                    if (title.isEmpty()) {
                        Toast.makeText(this, R.string.title_cannot_be_empty, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Subject subject = selectedSubject(subjectSpinner);
                    if (subject != null) {
                        material.setSubjectId(subject.getId());
                        material.setTopicId(selectedTopicId(topicSpinner));
                    }
                    material.setTitle(title);
                    if (!url.isEmpty()) {
                        material.setFilePathOrUrl(url);
                        material.setLink(true);
                    }
                    materialViewModel.update(material);
                    Toast.makeText(this, R.string.file_updated, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    // ─── Delete group confirmation ────────────────────────────────────────────

    private void confirmDeleteGroup(String groupName) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_group_title)
                .setMessage(getString(R.string.delete_group_message, groupName))
                .setPositiveButton(R.string.action_delete_all, (d, w) -> {
                    materialViewModel.deleteByGroup(groupName, -1);
                    Toast.makeText(this, R.string.group_deleted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.action_cancel, null)
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
            Toast.makeText(this, R.string.could_not_open_file, Toast.LENGTH_SHORT).show();
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
            tvAddSelectedFiles.setText(R.string.no_files_selected);
        } else if (pendingAddUris.size() == 1) {
            tvAddSelectedFiles.setText(fileNameOf(pendingAddUris.get(0)));
        } else {
            tvAddSelectedFiles.setText(getString(R.string.files_selected_count, pendingAddUris.size()));
        }
    }

    private void updateEditGroupFileLabel() {
        if (tvEditSelectedFiles == null)
            return;
        if (pendingEditUris.isEmpty()) {
            tvEditSelectedFiles.setText(getString(R.string.existing_files_label, editingGroupFiles.size()));
        } else {
            tvEditSelectedFiles.setText(getString(R.string.existing_plus_new_files,
                    editingGroupFiles.size(), pendingEditUris.size()));
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
