package com.fussy.examfeverplanner.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.fussy.examfeverplanner.data.dao.SubjectDao;
import com.fussy.examfeverplanner.data.database.AppDatabase;
import com.fussy.examfeverplanner.data.entity.Subject;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SubjectRepository {
    private SubjectDao subjectDao;

    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public SubjectRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        subjectDao = db.subjectDao();
    }

    public void insert(Subject subject) {
        databaseWriteExecutor.execute(() -> subjectDao.insert(subject));
    }

    public void update(Subject subject) {
        databaseWriteExecutor.execute(() -> subjectDao.update(subject));
    }

    public void delete(Subject subject) {
        databaseWriteExecutor.execute(() -> subjectDao.delete(subject));
    }

    /** Room-backed LiveData — list refreshes instantly on any change */
    public LiveData<List<Subject>> getAllSubjects() {
        return subjectDao.getAllSubjectsLive();
    }
}
