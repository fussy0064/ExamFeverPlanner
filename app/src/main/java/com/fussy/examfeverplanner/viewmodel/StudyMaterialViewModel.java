package com.fussy.examfeverplanner.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.fussy.examfeverplanner.data.entity.StudyMaterial;
import com.fussy.examfeverplanner.repository.StudyMaterialRepository;

import java.util.List;

public class StudyMaterialViewModel extends AndroidViewModel {
    private StudyMaterialRepository repository;

    public StudyMaterialViewModel(@NonNull Application application) {
        super(application);
        repository = new StudyMaterialRepository(application);
    }

    public LiveData<List<StudyMaterial>> getMaterialsBySubject(int subjectId) {
        return repository.getMaterialsBySubject(subjectId);
    }

    public LiveData<List<StudyMaterial>> getAllMaterials() {
        return repository.getAllMaterials();
    }

    public void insert(StudyMaterial material) {
        repository.insert(material);
    }

    public void update(StudyMaterial material) {
        repository.update(material);
    }

    public void delete(StudyMaterial material) {
        repository.delete(material);
    }

    public void deleteByGroup(String groupName, int subjectId) {
        repository.deleteByGroup(groupName, subjectId);
    }
}
