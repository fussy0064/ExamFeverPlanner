package com.fussy.examfeverplanner.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.fussy.examfeverplanner.data.entity.StudyMaterial;

import java.util.List;

@Dao
public interface StudyMaterialDao {
    @Insert
    void insert(StudyMaterial material);

    @Update
    void update(StudyMaterial material);

    @Delete
    void delete(StudyMaterial material);

    @Query("SELECT * FROM materials WHERE subjectId = :subjectId")
    List<StudyMaterial> getMaterialsBySubject(int subjectId);
}
