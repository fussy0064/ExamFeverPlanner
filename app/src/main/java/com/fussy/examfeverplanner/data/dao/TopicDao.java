package com.fussy.examfeverplanner.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.fussy.examfeverplanner.data.entity.Topic;

import java.util.List;

@Dao
public interface TopicDao {
    @Insert
    void insert(Topic topic);

    @Update
    void update(Topic topic);

    @Delete
    void delete(Topic topic);

    /** Room reactively pushes changes when topics are added/edited/deleted */
    @Query("SELECT * FROM topics WHERE subjectId = :subjectId ORDER BY isCompleted ASC, id ASC")
    LiveData<List<Topic>> getTopicsBySubjectLive(int subjectId);

    @Query("SELECT COUNT(*) FROM topics WHERE subjectId = :subjectId")
    int getTotalTopics(int subjectId);

    @Query("SELECT COUNT(*) FROM topics WHERE subjectId = :subjectId AND isCompleted = 1")
    int getCompletedTopics(int subjectId);
}
