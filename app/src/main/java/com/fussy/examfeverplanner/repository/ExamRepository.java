package com.fussy.examfeverplanner.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.fussy.examfeverplanner.data.dao.ExamDao;
import com.fussy.examfeverplanner.data.database.AppDatabase;
import com.fussy.examfeverplanner.data.entity.Exam;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExamRepository {
    private ExamDao examDao;

    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public ExamRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        examDao = db.examDao();
    }

    public void insert(Exam exam) {
        databaseWriteExecutor.execute(() -> examDao.insert(exam));
    }

    public void update(Exam exam) {
        databaseWriteExecutor.execute(() -> examDao.update(exam));
    }

    public void delete(Exam exam) {
        databaseWriteExecutor.execute(() -> examDao.delete(exam));
    }

    /** Room-backed LiveData — auto-refreshes on any table change */
    public LiveData<List<Exam>> getAllExams() {
        return examDao.getAllExamsLive();
    }
}
