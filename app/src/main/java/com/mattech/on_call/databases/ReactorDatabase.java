package com.mattech.on_call.databases;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;

import com.mattech.on_call.daos.ReactorDAO;
import com.mattech.on_call.models.Reactor;

@Database(entities = Reactor.class, version = 1)
public abstract class ReactorDatabase extends RoomDatabase {
    public abstract ReactorDAO getReactorDAO();

    private static volatile ReactorDatabase instance;

    @NonNull
    public static ReactorDatabase getInstance(@NonNull final Context context) {
        if (instance == null) {
            synchronized (ReactorDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(), ReactorDatabase.class, "appDatabase").build();
                }
            }
        }
        return instance;
    }
}
