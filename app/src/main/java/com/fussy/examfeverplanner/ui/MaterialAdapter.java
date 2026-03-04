package com.fussy.examfeverplanner.ui;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fussy.examfeverplanner.R;
import com.fussy.examfeverplanner.data.entity.StudyMaterial;

import java.util.ArrayList;
import java.util.List;

public class MaterialAdapter extends RecyclerView.Adapter<MaterialAdapter.MaterialHolder> {

    private List<StudyMaterial> materials = new ArrayList<>();
    private OnDeleteListener deleteListener;

    public interface OnDeleteListener {
        void onDelete(StudyMaterial material);
    }

    public void setDeleteListener(OnDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public MaterialHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_material, parent, false);
        return new MaterialHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MaterialHolder holder, int position) {
        StudyMaterial currentMaterial = materials.get(position);
        holder.tvMaterialTitle.setText(currentMaterial.getTitle());

        String pathOrUrl = currentMaterial.getFilePathOrUrl();
        boolean isLocalFile = !currentMaterial.isLink();

        if (isLocalFile) {
            holder.tvMaterialDesc.setText("📄 Local File");
            holder.ivMaterialType.setImageResource(android.R.drawable.ic_menu_save);
        } else {
            holder.tvMaterialDesc.setText(pathOrUrl);
            holder.ivMaterialType.setImageResource(android.R.drawable.ic_menu_agenda);
        }

        holder.itemView.setOnClickListener(v -> {
            if (pathOrUrl == null || pathOrUrl.isEmpty())
                return;
            Intent openIntent;
            if (isLocalFile) {
                Uri uri = Uri.parse(pathOrUrl);
                openIntent = new Intent(Intent.ACTION_VIEW);
                openIntent.setDataAndType(uri, "*/*");
                openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                String url = pathOrUrl;
                if (!url.startsWith("http://") && !url.startsWith("https://"))
                    url = "https://" + url;
                openIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            }
            try {
                v.getContext().startActivity(openIntent);
            } catch (Exception ignored) {
            }
        });

        holder.btnDelete.setOnClickListener(v -> new AlertDialog.Builder(v.getContext())
                .setTitle("Delete Material")
                .setMessage("Delete \"" + currentMaterial.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (deleteListener != null)
                        deleteListener.onDelete(currentMaterial);
                })
                .setNegativeButton("Cancel", null)
                .show());
    }

    @Override
    public int getItemCount() {
        return materials.size();
    }

    public void setMaterials(List<StudyMaterial> materials) {
        this.materials = materials;
        notifyDataSetChanged();
    }

    class MaterialHolder extends RecyclerView.ViewHolder {
        private final TextView tvMaterialTitle;
        private final TextView tvMaterialDesc;
        private final ImageView ivMaterialType;
        private final ImageButton btnDelete;

        public MaterialHolder(View itemView) {
            super(itemView);
            tvMaterialTitle = itemView.findViewById(R.id.tv_material_title);
            tvMaterialDesc = itemView.findViewById(R.id.tv_material_desc);
            ivMaterialType = itemView.findViewById(R.id.iv_material_type);
            btnDelete = itemView.findViewById(R.id.btn_delete_material);
        }
    }
}
