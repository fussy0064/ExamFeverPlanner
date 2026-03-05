package com.fussy.examfeverplanner.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.fussy.examfeverplanner.data.entity.Exam;

import java.util.List;

@Dao
public interface ExamDao {
    @Insert
    void insert(Exam exam);

    @Update
    void update(Exam exam);

    @Delete
    void delete(Exam exam);

    /** Room auto-pushes changes to observers on every insert/update/delete */
    @Query("SELECT * FROM exams ORDER BY date ASC")
    LiveData<List<Exam>> getAllExamsLive();

    @Query("SELECT * FROM exams WHERE subjectId = :subjectId")
    List<Exam> getExamsBySubject(int subjectId);
}
