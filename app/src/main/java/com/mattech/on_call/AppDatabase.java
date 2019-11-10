package com.mattech.on_call;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.mattech.on_call.daos.ReactorDAO;
import com.mattech.on_call.models.Reactor;

@Database(entities = Reactor.class, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ReactorDAO getReactorDAO();

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
