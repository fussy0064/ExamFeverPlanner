package com.fussy.examfeverplanner.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.fussy.examfeverplanner.data.dao.ExamDao;
import com.fussy.examfeverplanner.data.dao.StudyMaterialDao;
import com.fussy.examfeverplanner.data.dao.SubjectDao;
import com.fussy.examfeverplanner.data.dao.TaskDao;
import com.fussy.examfeverplanner.data.dao.TopicDao;
import com.fussy.examfeverplanner.data.entity.Exam;
import com.fussy.examfeverplanner.data.entity.StudyMaterial;
import com.fussy.examfeverplanner.data.entity.Subject;
import com.fussy.examfeverplanner.data.entity.Task;
import com.fussy.examfeverplanner.data.entity.Topic;

@Database(entities = { Subject.class, Task.class, Exam.class, Topic.class,
        StudyMaterial.class }, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract SubjectDao subjectDao();

    public abstract TaskDao taskDao();

    public abstract ExamDao examDao();

    public abstract TopicDao topicDao();

    public abstract StudyMaterialDao studyMaterialDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "exam_fever_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
