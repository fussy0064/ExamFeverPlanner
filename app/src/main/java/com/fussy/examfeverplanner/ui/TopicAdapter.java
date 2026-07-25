package com.fussy.examfeverplanner.ui;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.fussy.examfeverplanner.R;
import com.fussy.examfeverplanner.data.entity.Topic;

import java.util.ArrayList;
import java.util.List;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.TopicHolder> {

    private List<Topic> topics = new ArrayList<>();
    private final OnItemClickListener listener;
    private OnDeleteListener deleteListener;
    private OnEditListener editListener;

    public interface OnItemClickListener {
        void onCheckboxClick(Topic topic, boolean isChecked);
    }

    public interface OnDeleteListener {
        void onDelete(Topic topic);
    }

    public interface OnEditListener {
        void onEdit(Topic topic);
    }

    public TopicAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setDeleteListener(OnDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void setEditListener(OnEditListener editListener) {
        this.editListener = editListener;
    }

    @NonNull
    @Override
    public TopicHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_topic, parent, false);
        return new TopicHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TopicHolder holder, int position) {
        Topic currentTopic = topics.get(position);
        holder.tvTopicName.setText(currentTopic.getName());

        holder.checkboxTopic.setOnCheckedChangeListener(null);
        holder.checkboxTopic.setChecked(currentTopic.isCompleted());
        updateStrikeThrough(holder.tvTopicName, currentTopic.isCompleted());

        holder.checkboxTopic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                currentTopic.setCompleted(isChecked);
                updateStrikeThrough(holder.tvTopicName, isChecked);
                listener.onCheckboxClick(currentTopic, isChecked);
            }
        });

        holder.btnDelete.setOnClickListener(v -> new AlertDialog.Builder(v.getContext())
                .setTitle(R.string.delete_topic_title)
                .setMessage(v.getContext().getString(R.string.delete_topic_message, currentTopic.getName()))
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    int pos = holder.getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        topics.remove(pos);
                        notifyItemRemoved(pos);
                    }
                    if (deleteListener != null)
                        deleteListener.onDelete(currentTopic);
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show());

        holder.btnEdit.setOnClickListener(v -> {
            if (editListener != null)
                editListener.onEdit(currentTopic);
        });
    }

    private void updateStrikeThrough(TextView tv, boolean isCompleted) {
        if (isCompleted) {
            tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            tv.setPaintFlags(tv.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    @Override
    public int getItemCount() {
        return topics.size();
    }

    public void setTopics(List<Topic> newTopics) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return topics.size();
            }

            @Override
            public int getNewListSize() {
                return newTopics.size();
            }

            @Override
            public boolean areItemsTheSame(int o, int n) {
                return topics.get(o).getId() == newTopics.get(n).getId();
            }

            @Override
            public boolean areContentsTheSame(int o, int n) {
                Topic a = topics.get(o), b = newTopics.get(n);
                return a.getName().equals(b.getName()) && a.isCompleted() == b.isCompleted();
            }
        });
        topics = new ArrayList<>(newTopics);
        result.dispatchUpdatesTo(this);
    }

    class TopicHolder extends RecyclerView.ViewHolder {
        private final TextView tvTopicName;
        private final CheckBox checkboxTopic;
        private final ImageButton btnDelete;
        private final ImageButton btnEdit;

        public TopicHolder(View itemView) {
            super(itemView);
            tvTopicName = itemView.findViewById(R.id.tv_topic_name);
            checkboxTopic = itemView.findViewById(R.id.checkbox_topic);
            btnDelete = itemView.findViewById(R.id.btn_delete_topic);
            btnEdit = itemView.findViewById(R.id.btn_edit_topic);
        }
    }
}
