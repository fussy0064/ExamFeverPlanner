package com.fussy.examfeverplanner.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.fussy.examfeverplanner.data.entity.Task;

import java.util.List;

@Dao
public interface TaskDao {
    @Insert
    void insert(Task task);

    @Insert
    long insertReturnId(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    /** Room auto-updates this LiveData whenever the tasks table changes */
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, dueDate ASC")
    LiveData<List<Task>> getAllTasksLive();

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY dueDate ASC")
    LiveData<List<Task>> getPendingTasksLive();

    // Non-LiveData queries kept for legacy / one-shot use
    @Query("SELECT * FROM tasks")
    List<Task> getAllTasks();

    @Query("SELECT * FROM tasks WHERE subjectId = :subjectId")
    List<Task> getTasksBySubject(int subjectId);

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY dueDate ASC")
    List<Task> getPendingTasks();
}
