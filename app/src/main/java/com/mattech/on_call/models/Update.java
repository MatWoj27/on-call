package com.mattech.on_call.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import com.mattech.on_call.UpdateTypeConverters;

@Entity(tableName = "updates")
public class Update {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private boolean enabled;
    private boolean oneTimeUpdate;

    @TypeConverters(UpdateTypeConverters.class)
    private boolean repetitionDays[];
    private String exactDate;

    @NonNull
    private String time;

    public Update() {
    }

    public Update(boolean enabled, boolean oneTimeUpdate, boolean[] repetitionDays, String exactDate, @NonNull String time) {
        this.enabled = enabled;
        this.oneTimeUpdate = oneTimeUpdate;
        this.repetitionDays = repetitionDays;
        this.exactDate = exactDate;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isOneTimeUpdate() {
        return oneTimeUpdate;
    }

    public void setOneTimeUpdate(boolean oneTimeUpdate) {
        this.oneTimeUpdate = oneTimeUpdate;
    }

    public boolean[] getRepetitionDays() {
        return repetitionDays;
    }

    public void setRepetitionDays(boolean repetitionDays[]) {
        this.repetitionDays = repetitionDays;
    }

    public String getExactDate() {
        return exactDate;
    }

    public void setExactDate(String exactDate) {
        this.exactDate = exactDate;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
