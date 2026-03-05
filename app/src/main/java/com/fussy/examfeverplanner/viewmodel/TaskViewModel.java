package com.fussy.examfeverplanner.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.fussy.examfeverplanner.data.entity.Task;
import com.fussy.examfeverplanner.repository.TaskRepository;

import java.util.List;

public class TaskViewModel extends AndroidViewModel {

    private TaskRepository repository;
    private LiveData<List<Task>> allTasksLive;
    private LiveData<List<Task>> pendingTasksLive;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        allTasksLive = repository.getAllTasksLive();
        pendingTasksLive = repository.getPendingTasks();
    }

    /** All tasks (pending + done) — for TaskActivity list */
    public LiveData<List<Task>> getAllTasks() {
        return allTasksLive;
    }

    /** Only incomplete tasks — for the home screen summary */
    public LiveData<List<Task>> getPendingTasks() {
        return pendingTasksLive;
    }

    public void insert(Task task) {
        repository.insert(task);
    }

    public void insertAndThen(Task task, TaskRepository.InsertCallback callback) {
        repository.insertAndThen(task, callback);
    }

    public void update(Task task) {
        repository.update(task);
    }

    public void delete(Task task) {
        repository.delete(task);
    }
}
