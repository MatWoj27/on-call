package com.mattech.on_call.models;

import android.annotation.SuppressLint;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;

import com.mattech.on_call.R;
import com.mattech.on_call.type_converters.UpdateTypeConverters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Entity(tableName = "updates")
public class Update {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private boolean enabled;
    private boolean oneTimeUpdate;

    @TypeConverters(UpdateTypeConverters.class)
    private boolean[] repetitionDays;
    private String exactDate;

    @NonNull
    private String time = "12:00";
    private String preconfiguredPhoneNumber;

    @Ignore
    public static final String TIME_FORMAT = "HH:mm";

    @Ignore
    public static final String DATE_FORMAT = "EEE, d MMM yyyy";

    public enum TIME {
        HOUR(Calendar.HOUR_OF_DAY),
        MINUTE(Calendar.MINUTE);

        int field;

        TIME(int field) {
            this.field = field;
        }
    }

    public Update() {
    }

    @Ignore
    public Update(boolean enabled, boolean oneTimeUpdate, boolean[] repetitionDays, String exactDate, @NonNull String time, String preconfiguredPhoneNumber) {
        this.enabled = enabled;
        this.oneTimeUpdate = oneTimeUpdate;
        this.repetitionDays = repetitionDays;
        this.exactDate = exactDate;
        this.time = time;
        this.preconfiguredPhoneNumber = preconfiguredPhoneNumber;
    }

    @Ignore
    public Update(@NonNull Update update) {
        this.id = update.id;
        this.enabled = update.enabled;
        this.oneTimeUpdate = update.oneTimeUpdate;
        this.repetitionDays = update.repetitionDays;
        this.exactDate = update.exactDate;
        this.time = update.time;
        this.preconfiguredPhoneNumber = update.preconfiguredPhoneNumber;
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
                    && this.time.equals(update.time)
                    && this.preconfiguredPhoneNumber.equals(update.preconfiguredPhoneNumber);
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

    public void setRepetitionDays(boolean[] repetitionDays) {
        this.repetitionDays = repetitionDays;
    }

    public String getExactDate() {
        return exactDate;
    }

    public void setExactDate(String exactDate) {
        this.exactDate = exactDate;
    }

    @NonNull
    public String getTime() {
        return time;
    }

    public void setTime(@NonNull String time) {
        this.time = time;
    }

    public String getPreconfiguredPhoneNumber() {
        return preconfiguredPhoneNumber;
    }

    public void setPreconfiguredPhoneNumber(String preconfiguredPhoneNumber) {
        this.preconfiguredPhoneNumber = preconfiguredPhoneNumber;
    }

    @SuppressLint("DefaultLocale")
    public String getFormattedTime() throws ParseException {
        return String.format("%02d:%02d", get(TIME.HOUR), get(TIME.MINUTE));
    }

    public int get(@NonNull TIME timeField) throws ParseException {
        SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
        Calendar tmpCalendar = Calendar.getInstance();
        Date date = timeFormat.parse(time);
        tmpCalendar.setTime(date);
        return tmpCalendar.get(timeField.field);
    }

    public long getPlannedUpdateTimeInMillis() throws ParseException {
        return isOneTimeUpdate() ? getExactDateTimeInMillis() : getNextUpdateTimeInMillis();
    }

    private long getNextUpdateTimeInMillis() throws ParseException {
        Calendar calendar = Calendar.getInstance();
        long currentTimeInMillis = calendar.getTimeInMillis();
        long todayUpdateTimeInMillis = getTodayUpdateTimeInMillis();
        int todayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 2 == -1 ? 6 : calendar.get(Calendar.DAY_OF_WEEK) - 2;
        if (getRepetitionDays()[todayIndex] && todayUpdateTimeInMillis > currentTimeInMillis) {
            return todayUpdateTimeInMillis;
        } else {
            return Update.getNextRepetitionInMillis(todayUpdateTimeInMillis, getRepetitionDays());
        }
    }

    public long getTodayUpdateTimeInMillis() throws ParseException {
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.set(Calendar.HOUR_OF_DAY, get(TIME.HOUR));
        todayCalendar.set(Calendar.MINUTE, get(TIME.MINUTE));
        todayCalendar.set(Calendar.SECOND, 0);
        todayCalendar.set(Calendar.MILLISECOND, 0);
        return todayCalendar.getTimeInMillis();
    }

    private long getExactDateTimeInMillis() throws ParseException {
        SimpleDateFormat exactDateTimeFormat = new SimpleDateFormat(TIME_FORMAT + " " + DATE_FORMAT, Locale.getDefault());
        Date exactDate = exactDateTimeFormat.parse(getTime() + " " + getExactDate());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(exactDate);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    @ColorRes
    public int getDayViewColor(int dayIndex) {
        if (repetitionDays[dayIndex] && enabled) {
            return R.color.enabledActive;
        } else if (repetitionDays[dayIndex]) {
            return R.color.disabledActive;
        }
        return R.color.disabledInactive;
    }

    public static long getNextRepetitionInMillis(long previousUpdateTimeInMillis, @NonNull boolean[] repetitionDays) {
        Calendar calendar = Calendar.getInstance();
        long nextUpdateTargetTime = 0;
        int todayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 2 == -1 ? 6 : calendar.get(Calendar.DAY_OF_WEEK) - 2;
        for (int i = 1; i < 8; i++) {
            if (repetitionDays[(todayIndex + i) % 7]) {
                nextUpdateTargetTime = previousUpdateTimeInMillis + i * 24 * 60 * 60 * 1000;
                break;
            }
        }
        return nextUpdateTargetTime;
    }

    public static int getRemovedItemIndex(@NonNull List<Update> original, @NonNull List<Update> changed) {
        int removedItemIndex = original.size() == changed.size() ? -1 : original.size() - 1;
        for (int i = 0; i < original.size() && i < changed.size(); i++) {
            if (original.get(i).getId() != changed.get(i).getId()) {
                removedItemIndex = i;
                break;
            }
        }
        return removedItemIndex;
    }

    public static int getInsertedItemIndex(@NonNull List<Update> original, @NonNull List<Update> changed) {
        int insertedItemIndex = original.size() == changed.size() ? -1 : original.size();
        for (int i = 0; i < original.size() && i < changed.size(); i++) {
            if (original.get(i).getId() != changed.get(i).getId()) {
                insertedItemIndex = i;
                break;
            }
        }
        return insertedItemIndex;
    }

    public static int getEditedItemIndex(@NonNull List<Update> original, List<Update> changed) {
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
