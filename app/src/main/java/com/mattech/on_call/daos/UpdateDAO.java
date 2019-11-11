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

    @android.arch.persistence.room.Update
    void update(Update update);
}
