package com.mattech.on_call.daos;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.mattech.on_call.models.OnCallPerson;

@Dao
public interface OnCallPersonDAO {

    @Insert
    void insert(OnCallPerson... onCallPeople);

    @Query("DELETE FROM onCallPeople")
    void deleteAll();

    @Query("SELECT * FROM onCallPeople LIMIT 1")
    LiveData<OnCallPerson> getOnCallPerson();

    @Query("UPDATE onCallPeople SET phoneNumber=:newOnCallPersonPhoneNumber, name=:newOnCallPersonName, mail=:newOnCallPersonMail" +
            " WHERE phoneNumber LIKE :currentOnCallPersonPhoneNumber")
    void updateOnCallPerson(String currentOnCallPersonPhoneNumber, String newOnCallPersonPhoneNumber,
                            String newOnCallPersonName, String newOnCallPersonMail);
}
