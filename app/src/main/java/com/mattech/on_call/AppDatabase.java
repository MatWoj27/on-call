package com.mattech.on_call;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.mattech.on_call.daos.OnCallPersonDAO;
import com.mattech.on_call.daos.UpdateDAO;
import com.mattech.on_call.models.OnCallPerson;
import com.mattech.on_call.models.Update;

@Database(entities = OnCallPerson.class, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract OnCallPersonDAO getOnCallPersonDAO();

    private static volatile AppDatabase instance;

    public static AppDatabase getInstance(final Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "appDatabase").build();
                }
            }
        }
        return instance;
    }
}
