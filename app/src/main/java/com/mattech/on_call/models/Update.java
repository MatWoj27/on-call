package com.mattech.on_call.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import com.mattech.on_call.type_converters.UpdateTypeConverters;

import java.util.Arrays;
import java.util.List;

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

    public static int getRemovedItemIndex(List<Update> original, List<Update> changed) {
        int removedItemIndex = original.size() == changed.size() ? -1 : original.size() - 1;
        for (int i = 0; i < original.size() && i < changed.size(); i++) {
            if (original.get(i).getId() != changed.get(i).getId()) {
                removedItemIndex = i;
                break;
            }
        }
        return removedItemIndex;
    }

    public static int getInsertedItemIndex(List<Update> original, List<Update> changed) {
        int insertedItemIndex = original.size() == changed.size() ? -1 : original.size();
        for (int i = 0; i < original.size() && i < changed.size(); i++) {
            if (original.get(i).getId() != changed.get(i).getId()) {
                insertedItemIndex = i;
                break;
            }
        }
        return insertedItemIndex;
    }

    public static int getEditedItemIndex(List<Update> original, List<Update> changed) {
        int changedItemIndex = -1;
        for (int i = 0; i < original.size() && i < changed.size(); i++) {
            if (!original.get(i).equals(changed.get(i))) {
                changedItemIndex = i;
                break;
            }
        }
        return changedItemIndex;
    }
}
