package com.fussy.examfeverplanner.data.dao;

import androidx.lifecycle.LiveData;
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

    /**
     * Returns all materials for a subject ordered by groupName then id.
     * Room auto-notifies observers on any insert/update/delete.
     */
    @Query("SELECT * FROM materials WHERE subjectId = :subjectId ORDER BY groupName ASC, id ASC")
    LiveData<List<StudyMaterial>> getMaterialsBySubjectLive(int subjectId);

    /** All materials across every subject, ordered by subject then topic then group. */
    @Query("SELECT * FROM materials ORDER BY subjectId ASC, topicId ASC, groupName ASC, id ASC")
    LiveData<List<StudyMaterial>> getAllMaterialsLive();

    /** Delete all materials belonging to a group */
    @Query("DELETE FROM materials WHERE groupName = :groupName AND subjectId = :subjectId")
    void deleteByGroup(String groupName, int subjectId);

    /** Non-live fallback */
    @Query("SELECT * FROM materials WHERE subjectId = :subjectId")
    List<StudyMaterial> getMaterialsBySubject(int subjectId);
}
