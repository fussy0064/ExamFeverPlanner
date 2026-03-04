package com.fussy.examfeverplanner.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "topics")
public class Topic {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String name;
    private boolean isCompleted;
    private int subjectId;

    public Topic(String name, int subjectId) {
        this.name = name;
        this.subjectId = subjectId;
        this.isCompleted = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }
}
