package com.mattech.on_call.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import com.mattech.on_call.UpdateTypeConverters;

import java.util.Arrays;

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof Update)) {
            return false;
        } else {
            Update update = (Update) obj;
            return this.id == update.getId()
                    && this.enabled == update.enabled
                    && this.oneTimeUpdate == update.oneTimeUpdate
                    && Arrays.equals(this.repetitionDays, update.repetitionDays)
                    && this.exactDate.equals(update.exactDate)
                    && this.time.equals(update.time);
        }
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
