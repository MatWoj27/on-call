package com.mattech.on_call.models;

import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;

@Entity(tableName = "updates", primaryKeys = {"day", "time"})
public class Update {
    private boolean enabled;

    @NonNull
    private String day;

    @NonNull
    private String time;
    private boolean repeatable;

    public Update(boolean enabled, String day, String time, boolean repeatable) {
        this.enabled = enabled;
        this.day = day;
        this.time = time;
        this.repeatable = repeatable;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }
}
