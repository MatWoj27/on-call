package com.mattech.on_call.daos;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.mattech.on_call.models.Update;

import java.util.List;

@Dao
public interface UpdateDAO {

    @Query("DELETE FROM " + Update.TABLE_NAME + " WHERE id = :id")
    void deleteById(int id);

    @Insert
    long insert(Update update);

    @Query("SELECT * FROM " + Update.TABLE_NAME)
    LiveData<List<Update>> getUpdates();

    @Query("SELECT * FROM " + Update.TABLE_NAME + " WHERE enabled = 1")
    List<Update> getActiveUpdates();

    @android.arch.persistence.room.Update
    void update(Update update);

    @Query("UPDATE " + Update.TABLE_NAME + " SET enabled = 'false' WHERE id = :id")
    void disableUpdate(int id);
}
