package com.b2b.sampleb2b;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;

import com.b2b.sampleb2b.db.MyTaskDatabase;
import com.b2b.sampleb2b.db.entities.FolderEntity;
import com.b2b.sampleb2b.db.entities.SubFolderEntity;
import com.b2b.sampleb2b.db.entities.TaskDetailsEntity;
import com.b2b.sampleb2b.interfaces.TaskDetails;

import java.util.List;

/**
 * Created by Abhishek Singh on 27/5/18.
 */
public class DataRepository {
    private static DataRepository dataRepository;
    private final MyTaskDatabase taskDatabase;
    private MediatorLiveData<List<FolderEntity>> obervableFolderEntity;

    private DataRepository(MyTaskDatabase myTaskDatabase){
        taskDatabase = myTaskDatabase;
        obervableFolderEntity = new MediatorLiveData<>();

        obervableFolderEntity.addSource(taskDatabase.getFolderDao().getAllFolder(),
                allFolderEntity ->{
                    if(taskDatabase.getDatabaseCreated().getValue() != null){
                        obervableFolderEntity.postValue(allFolderEntity);
                    }
                });
    }

    public static DataRepository getDataRepository(final MyTaskDatabase myTaskDatabase){
        if(dataRepository == null){
            synchronized (DataRepository.class){
                if(dataRepository == null){
                    dataRepository = new DataRepository(myTaskDatabase);
                }
            }
        }
        return dataRepository;
    }

    /**
     * Get the list of folders from the database and get notified when the data changes.
     */
    public LiveData<List<FolderEntity>> getAllFolder(){
        return obervableFolderEntity;
    }

    public LiveData<List<TaskDetailsEntity>> getAllTaskDetails(){
        return taskDatabase.getTaskDetailsDao().loadAllTaskDetails();
    }

    public LiveData<List<SubFolderEntity>> getAllSubFolder(){
        return taskDatabase.getSubFolderDao().loadAllSubFolders();
    }


}
