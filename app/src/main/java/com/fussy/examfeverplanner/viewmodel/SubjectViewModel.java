package com.fussy.examfeverplanner.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.fussy.examfeverplanner.data.entity.Subject;
import com.fussy.examfeverplanner.repository.SubjectRepository;

import java.util.List;

public class SubjectViewModel extends AndroidViewModel {

    private SubjectRepository repository;
    private LiveData<List<Subject>> allSubjects;

    public SubjectViewModel(@NonNull Application application) {
        super(application);
        repository = new SubjectRepository(application);
        allSubjects = repository.getAllSubjects();
    }

    public LiveData<List<Subject>> getAllSubjects() {
        return allSubjects;
    }

    public void insert(Subject subject) {
        repository.insert(subject);
    }

    public void update(Subject subject) {
        repository.update(subject);
    }

    public void delete(Subject subject) {
        repository.delete(subject);
    }
}
