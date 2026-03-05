package com.fussy.examfeverplanner.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.fussy.examfeverplanner.data.dao.StudyMaterialDao;
import com.fussy.examfeverplanner.data.database.AppDatabase;
import com.fussy.examfeverplanner.data.entity.StudyMaterial;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StudyMaterialRepository {
    private StudyMaterialDao materialDao;

    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public StudyMaterialRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        materialDao = db.studyMaterialDao();
    }

    public void insert(StudyMaterial material) {
        databaseWriteExecutor.execute(() -> materialDao.insert(material));
    }

    public void update(StudyMaterial material) {
        databaseWriteExecutor.execute(() -> materialDao.update(material));
    }

    public void delete(StudyMaterial material) {
        databaseWriteExecutor.execute(() -> materialDao.delete(material));
    }

    public void deleteByGroup(String groupName, int subjectId) {
        databaseWriteExecutor.execute(() -> materialDao.deleteByGroup(groupName, subjectId));
    }

    public LiveData<List<StudyMaterial>> getMaterialsBySubject(int subjectId) {
        return materialDao.getMaterialsBySubjectLive(subjectId);
    }
}
