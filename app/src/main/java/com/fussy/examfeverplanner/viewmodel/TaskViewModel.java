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
    private LiveData<List<Task>> allPendingTasks;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        allPendingTasks = repository.getPendingTasks();
    }

    public LiveData<List<Task>> getPendingTasks() {
        return allPendingTasks;
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
