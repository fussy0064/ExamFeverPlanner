package com.fussy.examfeverplanner.data.dao;

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

    @Query("SELECT * FROM exams ORDER BY date ASC")
    List<Exam> getAllExams();

    @Query("SELECT * FROM exams WHERE subjectId = :subjectId")
    List<Exam> getExamsBySubject(int subjectId);
}
