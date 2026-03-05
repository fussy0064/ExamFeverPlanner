package com.fussy.examfeverplanner.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.fussy.examfeverplanner.data.entity.Subject;

import java.util.List;

@Dao
public interface SubjectDao {
    @Insert
    void insert(Subject subject);

    @Update
    void update(Subject subject);

    @Delete
    void delete(Subject subject);

    /** Room reactively updates observers on insert/update/delete */
    @Query("SELECT * FROM subjects ORDER BY name ASC")
    LiveData<List<Subject>> getAllSubjectsLive();

    @Query("SELECT * FROM subjects WHERE id = :id")
    Subject getSubjectById(int id);
}
