package com.fussy.examfeverplanner.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.fussy.examfeverplanner.data.dao.TopicDao;
import com.fussy.examfeverplanner.data.database.AppDatabase;
import com.fussy.examfeverplanner.data.entity.Topic;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TopicRepository {
    private TopicDao topicDao;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public TopicRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        topicDao = db.topicDao();
    }

    public void insert(Topic topic) {
        databaseWriteExecutor.execute(() -> topicDao.insert(topic));
    }

    public void update(Topic topic) {
        databaseWriteExecutor.execute(() -> topicDao.update(topic));
    }

    public void delete(Topic topic) {
        databaseWriteExecutor.execute(() -> topicDao.delete(topic));
    }

    public LiveData<List<Topic>> getTopicsBySubject(int subjectId) {
        MutableLiveData<List<Topic>> topicsData = new MutableLiveData<>();
        databaseWriteExecutor.execute(() -> {
            List<Topic> topics = topicDao.getTopicsBySubject(subjectId);
            topicsData.postValue(topics);
        });
        return topicsData;
    }
}
