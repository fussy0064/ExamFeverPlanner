package com.fussy.examfeverplanner.ui;

import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fussy.examfeverplanner.R;
import com.fussy.examfeverplanner.data.entity.StudyMaterial;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Expandable grouped adapter for Study Materials.
 *
 * Display items are either:
 * TYPE_GROUP — a collapsible header (groupName + file count)
 * TYPE_FILE — an individual file child row (visible only when group is
 * expanded)
 */
public class MaterialAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // ── View types ────────────────────────────────────────────────────────────
    private static final int TYPE_GROUP = 0;
    private static final int TYPE_FILE = 1;

    // ── Listener interfaces ───────────────────────────────────────────────────
    public interface OnGroupDeleteListener {
        void onDelete(String groupName);
    }

    public interface OnGroupEditListener {
        void onEdit(String groupName, List<StudyMaterial> files);
    }

    public interface OnFileDeleteListener {
        void onDelete(StudyMaterial material);
    }

    public interface OnFileEditListener {
        void onEdit(StudyMaterial material);
    }

    public interface OnFileClickListener {
        void onClick(StudyMaterial material);
    }

    private OnGroupDeleteListener groupDeleteListener;
    private OnGroupEditListener groupEditListener;
    private OnFileDeleteListener fileDeleteListener;
    private OnFileEditListener fileEditListener;
    private OnFileClickListener fileClickListener;

    public void setGroupDeleteListener(OnGroupDeleteListener l) {
        groupDeleteListener = l;
    }

    public void setGroupEditListener(OnGroupEditListener l) {
        groupEditListener = l;
    }

    public void setFileDeleteListener(OnFileDeleteListener l) {
        fileDeleteListener = l;
    }

    public void setFileEditListener(OnFileEditListener l) {
        fileEditListener = l;
    }

    public void setFileClickListener(OnFileClickListener l) {
        fileClickListener = l;
    }

    // ── Internal display list item ────────────────────────────────────────────
    private static class DisplayItem {
        int type;
        // For GROUP
        String groupName;
        List<StudyMaterial> groupFiles;
        boolean expanded;
        // For FILE
        StudyMaterial material;

        static DisplayItem group(String name, List<StudyMaterial> files) {
            DisplayItem d = new DisplayItem();
            d.type = TYPE_GROUP;
            d.groupName = name;
            d.groupFiles = new ArrayList<>(files);
            d.expanded = false;
            return d;
        }

        static DisplayItem file(StudyMaterial m) {
            DisplayItem d = new DisplayItem();
            d.type = TYPE_FILE;
            d.material = m;
            return d;
        }
    }

    // ── Data ─────────────────────────────────────────────────────────────────
    /** Master list of all materials from DB (sorted by groupName, id) */
    private List<StudyMaterial> allMaterials = new ArrayList<>();

    /**
     * Flat, ordered display list that the RecyclerView consumes.
     * Re-built from allMaterials any time data changes.
     */
    private final List<DisplayItem> displayList = new ArrayList<>();

    /** Tracks which groups the user has opened */
    private final java.util.Set<String> expandedGroups = new java.util.HashSet<>();

    // ── Public API ────────────────────────────────────────────────────────────

    public void setMaterials(List<StudyMaterial> materials) {
        allMaterials = materials != null ? materials : new ArrayList<>();
        rebuildDisplayList();
        notifyDataSetChanged();
    }

    // ── RecyclerView.Adapter overrides ────────────────────────────────────────

    @Override
    public int getItemViewType(int position) {
        return displayList.get(position).type;
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_GROUP) {
            View v = inf.inflate(R.layout.item_material_group, parent, false);
            return new GroupViewHolder(v);
        } else {
            View v = inf.inflate(R.layout.item_material_file, parent, false);
            return new FileViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {
        DisplayItem item = displayList.get(pos);
        if (item.type == TYPE_GROUP) {
            bindGroup((GroupViewHolder) holder, item, pos);
        } else {
            bindFile((FileViewHolder) holder, item);
        }
    }

    // ── Bind helpers ──────────────────────────────────────────────────────────

    private void bindGroup(GroupViewHolder h, DisplayItem item, int pos) {
        h.tvGroupName.setText(item.groupName);
        int count = item.groupFiles.size();
        h.tvFileCount.setText(h.itemView.getResources()
                .getQuantityString(R.plurals.file_count, count, count));

        // Animate chevron
        float rotation = item.expanded ? 180f : 0f;
        h.ivChevron.setRotation(rotation);

        // Toggle expand/collapse on header click
        h.itemView.setOnClickListener(v -> {
            int adPos = h.getAdapterPosition();
            if (adPos == RecyclerView.NO_POSITION)
                return;
            DisplayItem di = displayList.get(adPos);
            boolean wasExpanded = di.expanded;
            if (wasExpanded) {
                collapseGroup(adPos, di);
            } else {
                expandGroup(adPos, di);
            }
            // Animate chevron from its old state to its new state
            ObjectAnimator.ofFloat(h.ivChevron, "rotation",
                    wasExpanded ? 180f : 0f, wasExpanded ? 0f : 180f)
                    .setDuration(200).start();
        });

        h.btnEditGroup.setOnClickListener(v -> {
            if (groupEditListener != null)
                groupEditListener.onEdit(item.groupName, item.groupFiles);
        });

        h.btnDeleteGroup.setOnClickListener(v -> {
            if (groupDeleteListener != null)
                groupDeleteListener.onDelete(item.groupName);
        });
    }

    private void bindFile(FileViewHolder h, DisplayItem item) {
        StudyMaterial m = item.material;
        String display = m.getTitle() != null && !m.getTitle().isEmpty()
                ? m.getTitle()
                : m.getFilePathOrUrl();
        h.tvFileName.setText(display);

        // Different icon for links vs files
        h.ivFileIcon.setImageResource(m.isLink()
                ? R.drawable.ic_link_24
                : R.drawable.ic_file_24);

        h.btnDeleteFile.setOnClickListener(v -> {
            if (fileDeleteListener != null)
                fileDeleteListener.onDelete(m);
        });

        h.btnEditFile.setOnClickListener(v -> {
            if (fileEditListener != null)
                fileEditListener.onEdit(m);
        });

        h.itemView.setOnClickListener(v -> {
            if (fileClickListener != null)
                fileClickListener.onClick(m);
        });
    }

    // ── Expand / Collapse ─────────────────────────────────────────────────────

    private void expandGroup(int groupPos, DisplayItem groupItem) {
        groupItem.expanded = true;
        expandedGroups.add(groupItem.groupName);
        int insertAt = groupPos + 1;
        for (StudyMaterial m : groupItem.groupFiles) {
            displayList.add(insertAt, DisplayItem.file(m));
            notifyItemInserted(insertAt);
            insertAt++;
        }
        notifyItemChanged(groupPos); // refresh chevron
    }

    private void collapseGroup(int groupPos, DisplayItem groupItem) {
        groupItem.expanded = false;
        expandedGroups.remove(groupItem.groupName);
        int toRemove = groupItem.groupFiles.size();
        for (int i = 0; i < toRemove; i++) {
            displayList.remove(groupPos + 1);
            notifyItemRemoved(groupPos + 1);
        }
        notifyItemChanged(groupPos); // refresh chevron
    }

    // ── Display-list builder ──────────────────────────────────────────────────

    private void rebuildDisplayList() {
        displayList.clear();

        // Group materials by groupName (preserve insertion order)
        LinkedHashMap<String, List<StudyMaterial>> grouped = new LinkedHashMap<>();
        for (StudyMaterial m : allMaterials) {
            String key = m.getGroupName() != null ? m.getGroupName() : m.getTitle();
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(m);
        }

        for (Map.Entry<String, List<StudyMaterial>> entry : grouped.entrySet()) {
            DisplayItem header = DisplayItem.group(entry.getKey(), entry.getValue());
            header.expanded = expandedGroups.contains(entry.getKey());
            displayList.add(header);
            if (header.expanded) {
                for (StudyMaterial m : entry.getValue()) {
                    displayList.add(DisplayItem.file(m));
                }
            }
        }
    }

    // ── ViewHolders ───────────────────────────────────────────────────────────

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView tvGroupName, tvFileCount;
        ImageView ivChevron;
        ImageButton btnEditGroup, btnDeleteGroup;

        GroupViewHolder(View v) {
            super(v);
            tvGroupName = v.findViewById(R.id.tv_group_name);
            tvFileCount = v.findViewById(R.id.tv_file_count);
            ivChevron = v.findViewById(R.id.iv_chevron);
            btnEditGroup = v.findViewById(R.id.btn_edit_group);
            btnDeleteGroup = v.findViewById(R.id.btn_delete_group);
        }
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        TextView tvFileName;
        ImageView ivFileIcon;
        ImageButton btnDeleteFile, btnEditFile;

        FileViewHolder(View v) {
            super(v);
            tvFileName = v.findViewById(R.id.tv_file_name);
            ivFileIcon = v.findViewById(R.id.iv_file_icon);
            btnDeleteFile = v.findViewById(R.id.btn_delete_file);
            btnEditFile = v.findViewById(R.id.btn_edit_file);
        }
    }
}
