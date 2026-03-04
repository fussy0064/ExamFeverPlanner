package com.fussy.examfeverplanner.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "subjects")
public class Subject {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String name;
    private String colorHex;
    
    public Subject(String name, String colorHex) {
        this.name = name;
        this.colorHex = colorHex;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }
}
