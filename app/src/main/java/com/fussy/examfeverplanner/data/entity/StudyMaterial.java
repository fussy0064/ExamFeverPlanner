package com.fussy.examfeverplanner.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "materials")
public class StudyMaterial {

    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String title;
    private String filePathOrUrl;
    private int subjectId;
    private boolean isLink;

    public StudyMaterial(String title, String filePathOrUrl, int subjectId, boolean isLink) {
        this.title = title;
        this.filePathOrUrl = filePathOrUrl;
        this.subjectId = subjectId;
        this.isLink = isLink;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getFilePathOrUrl() { return filePathOrUrl; }
    public void setFilePathOrUrl(String filePathOrUrl) { this.filePathOrUrl = filePathOrUrl; }
    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }
    public boolean isLink() { return isLink; }
    public void setLink(boolean link) { isLink = link; }
}
