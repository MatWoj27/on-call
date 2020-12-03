package com.mattech.on_call.daos;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.mattech.on_call.models.Reactor;

@Dao
public interface ReactorDAO {

    @Insert
    void insert(Reactor... reactors);

    @Query("DELETE FROM " + Reactor.TABLE_NAME)
    void deleteAll();

    @Query("SELECT * FROM " + Reactor.TABLE_NAME + " LIMIT 1")
    LiveData<Reactor> getReactorLiveData();

    @Query("SELECT * FROM " + Reactor.TABLE_NAME + " LIMIT 1")
    Reactor getReactor();

    @Query("UPDATE " + Reactor.TABLE_NAME + " SET phoneNumber=:newReactorPhoneNumber, name=:newReactorName, mail=:newReactorMail" +
            " WHERE phoneNumber LIKE :currentReactorPhoneNumber")
    void updateReactor(String currentReactorPhoneNumber, String newReactorPhoneNumber,
                       String newReactorName, String newReactorMail);
}
