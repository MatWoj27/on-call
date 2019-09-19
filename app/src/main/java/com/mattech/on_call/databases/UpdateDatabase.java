package com.mattech.on_call.databases;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.mattech.on_call.daos.UpdateDAO;
import com.mattech.on_call.models.Update;

@Database(entities = Update.class, version = 2)
public abstract class UpdateDatabase extends RoomDatabase {
    private static volatile UpdateDatabase instance;

    public abstract UpdateDAO getUpdateDAO();

    public static UpdateDatabase getInstance(final Context context) {
        if (instance == null) {
            synchronized (UpdateDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(), UpdateDatabase.class, "updateDatabase")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
