package com.fussy.examfeverplanner.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "materials")
public class StudyMaterial {

    @PrimaryKey(autoGenerate = true)
    private int id;

    /** User-visible title for this individual file/link */
    private String title;

    private String filePathOrUrl;
    private int subjectId;
    private int topicId = -1; // -1 = no specific topic (General)
    private boolean isLink;

    /**
     * The "folder" name that groups related files together.
     * All files uploaded under the same topic share the same groupName.
     * For a lone URL the groupName equals the title.
     */
    private String groupName;

    public StudyMaterial(String title, String filePathOrUrl,
            int subjectId, boolean isLink, String groupName) {
        this.title = title;
        this.filePathOrUrl = filePathOrUrl;
        this.subjectId = subjectId;
        this.isLink = isLink;
        this.groupName = groupName;
    }

    // ─── Getters / Setters ──────────────────────────────────────────────────
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFilePathOrUrl() {
        return filePathOrUrl;
    }

    public void setFilePathOrUrl(String v) {
        this.filePathOrUrl = v;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int v) {
        this.subjectId = v;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public boolean isLink() {
        return isLink;
    }

    public void setLink(boolean v) {
        isLink = v;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
