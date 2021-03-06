package com.b2b.sampleb2b.db;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.support.annotation.NonNull;

import com.b2b.sampleb2b.AppExecutors;
import com.b2b.sampleb2b.db.converter.ObjectConverter;
import com.b2b.sampleb2b.db.dao.FolderDao;
import com.b2b.sampleb2b.db.dao.SubFolderDao;
import com.b2b.sampleb2b.db.dao.TaskDetailsDao;
import com.b2b.sampleb2b.db.entities.FolderEntity;
import com.b2b.sampleb2b.db.entities.SubFolderEntity;
import com.b2b.sampleb2b.db.entities.TaskDetailsEntity;
import com.b2b.sampleb2b.helper.ApplicationHelper;
import com.b2b.sampleb2b.helper.HelperInterface;

import java.util.List;

/**
 * Created by Abhishek Singh on 27/5/18.
 */
@Database(entities = {FolderEntity.class, TaskDetailsEntity.class,
                      SubFolderEntity.class}
                    , version = 1)
@TypeConverters({ObjectConverter.class})
public abstract class MyTaskDatabase extends RoomDatabase implements HelperInterface {
    private static MyTaskDatabase taskDatabase;

    public abstract FolderDao      getFolderDao();
    public abstract SubFolderDao   getSubFolderDao();
    public abstract TaskDetailsDao getTaskDetailsDao();

    private final MutableLiveData<Boolean> isDBCreated = new MutableLiveData<>();

    public static MyTaskDatabase getInstance(final Context context, AppExecutors appExecutors){
        if(taskDatabase == null){
            synchronized (MyTaskDatabase.class){
                if(taskDatabase == null){
                    taskDatabase = buildDatabase(context.getApplicationContext(), appExecutors);
                    taskDatabase.updateDatabaseCreated(context.getApplicationContext());
                }
            }
        }
        return taskDatabase;
    }

    private static MyTaskDatabase buildDatabase(final Context context, final AppExecutors appExecutors){
        return Room.databaseBuilder(context, MyTaskDatabase.class, DATABASE_NAME)
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        appExecutors.getExeDiskIO().execute(()->{
                            // Add a delay to simulate a long-running operation
                            addDelay();
                            // Generate the data for pre-population
                            MyTaskDatabase database = MyTaskDatabase.getInstance(context, appExecutors);
                            List<FolderEntity>      folderEntities      = null; //DataGenerator.generateProducts();
                            List<TaskDetailsEntity> taskDetailsEntities = null; //DataGenerator.generateCommentsForProducts(products);
                            List<SubFolderEntity>   subFolderEntities   = null; //DataGenerator.generateCommentsForProducts(products);

                            insertData(database, folderEntities, subFolderEntities, taskDetailsEntities);
                            // notify that the database was created and it's ready to be used
                            database.setDatabaseCreated();
                        });
                    }
                }).build();
    }

    private void updateDatabaseCreated(final Context context) {
        if (context.getDatabasePath(DATABASE_NAME).exists()) {
            setDatabaseCreated();
        }
    }

    private void setDatabaseCreated(){
        isDBCreated.postValue(true);
    }

    private static void insertData(final MyTaskDatabase database,
                                   final List<FolderEntity>      folderEntities,
                                   final List<SubFolderEntity>   subFolderEntities,
                                   final List<TaskDetailsEntity> detailsEntities) {
        database.runInTransaction(() -> {
           // database.getFolderDao().insertAllFolder(folderEntities);
           // database.getTaskDetailsDao().insertAllTaskDetails(detailsEntities);
           // database.getSubFolderDao().insertAllSubFolder(subFolderEntities);
        });
    }

    private static void addDelay() {
        try {
            Thread.sleep(4000);
        } catch (InterruptedException ignored) {
        }
    }

    public LiveData<Boolean> getDatabaseCreated() {
        return isDBCreated;
    }

    @Override
    public ApplicationHelper getHelper() {
        return ApplicationHelper.getInstance();
    }
}
