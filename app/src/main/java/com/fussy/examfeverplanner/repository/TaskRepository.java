package com.fussy.examfeverplanner.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.fussy.examfeverplanner.data.dao.TaskDao;
import com.fussy.examfeverplanner.data.database.AppDatabase;
import com.fussy.examfeverplanner.data.entity.Task;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskRepository {

    private TaskDao taskDao;

    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public TaskRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        taskDao = db.taskDao();
    }

    public interface InsertCallback {
        void onInserted(int id);
    }

    public void insert(Task task) {
        databaseWriteExecutor.execute(() -> taskDao.insert(task));
    }

    /**
     * Insert task and return the auto-generated ID via callback on a background
     * thread.
     */
    public void insertAndThen(Task task, InsertCallback callback) {
        databaseWriteExecutor.execute(() -> {
            long rowId = taskDao.insertReturnId(task);
            if (callback != null)
                callback.onInserted((int) rowId);
        });
    }

    public void update(Task task) {
        databaseWriteExecutor.execute(() -> taskDao.update(task));
    }

    public void delete(Task task) {
        databaseWriteExecutor.execute(() -> taskDao.delete(task));
    }

    public LiveData<List<Task>> getPendingTasks() {
        MutableLiveData<List<Task>> pendingTasksData = new MutableLiveData<>();
        databaseWriteExecutor.execute(() -> {
            List<Task> pendingTasks = taskDao.getPendingTasks();
            pendingTasksData.postValue(pendingTasks);
        });
        return pendingTasksData;
    }
}
