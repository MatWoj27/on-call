package com.mattech.on_call.daos;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.mattech.on_call.models.Update;

import java.util.List;

@Dao
public interface UpdateDAO {

    @Query("DELETE FROM updates WHERE id = :id")
    void deleteById(int id);

    @Insert
    long insert(Update update);

    @Query("SELECT * FROM updates")
    LiveData<List<Update>> getUpdates();

    @Query("SELECT * FROM updates WHERE enabled = 1")
    List<Update> getActiveUpdates();

    @android.arch.persistence.room.Update
    void update(Update update);

    @Query("UPDATE updates SET enabled = 'false' WHERE id = :id")
    void disableUpdate(int id);
}
