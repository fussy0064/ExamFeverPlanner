package com.fussy.examfeverplanner.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.fussy.examfeverplanner.data.entity.Topic;
import com.fussy.examfeverplanner.repository.TopicRepository;

import java.util.List;

public class TopicViewModel extends AndroidViewModel {
    private TopicRepository repository;

    public TopicViewModel(@NonNull Application application) {
        super(application);
        repository = new TopicRepository(application);
    }

    public LiveData<List<Topic>> getTopicsBySubject(int subjectId) {
        return repository.getTopicsBySubject(subjectId);
    }

    public void insert(Topic topic) {
        repository.insert(topic);
    }

    public void update(Topic topic) {
        repository.update(topic);
    }

    public void delete(Topic topic) {
        repository.delete(topic);
    }
}
