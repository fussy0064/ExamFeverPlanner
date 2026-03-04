package com.fussy.examfeverplanner.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "exams")
public class Exam {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private long date; // Date stored as millis
    private String examTime; // Time as "HH:mm" string e.g. "09:30"
    private String location; // Venue / room (optional)
    private int subjectId;
    private boolean reminderSet;

    public Exam(String title, long date, String examTime, String location, int subjectId, boolean reminderSet) {
        this.title = title;
        this.date = date;
        this.examTime = examTime;
        this.location = location;
        this.subjectId = subjectId;
        this.reminderSet = reminderSet;
    }

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

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getExamTime() {
        return examTime;
    }

    public void setExamTime(String examTime) {
        this.examTime = examTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public boolean isReminderSet() {
        return reminderSet;
    }

    public void setReminderSet(boolean reminderSet) {
        this.reminderSet = reminderSet;
    }
}
