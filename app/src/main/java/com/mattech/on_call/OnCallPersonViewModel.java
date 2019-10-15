package com.mattech.on_call;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.mattech.on_call.models.OnCallPerson;
import com.mattech.on_call.models.Update;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OnCallPersonViewModel extends AndroidViewModel implements OnCallRepository.OperationOnUpdateListener {
    private LiveData<OnCallPerson> onCallPerson;
    private LiveData<List<Update>> updates;
    private OnCallRepository onCallRepository;

    public OnCallPersonViewModel(@NonNull Application application) {
        super(application);
        onCallRepository = new OnCallRepository(application);
        onCallPerson = onCallRepository.getOnCallPerson();
        updates = onCallRepository.getUpdates();
        onCallRepository.setUpdateListener(this);
    }

    public LiveData<OnCallPerson> getOnCallPerson() {
        return onCallPerson;
    }

    public LiveData<List<Update>> getUpdates() {
        return updates;
    }

    public void addUpdate(Update update) {
        onCallRepository.addUpdate(update);
    }

    public void updateUpdate(Update update) {
        if (update.isEnabled()) {
            scheduleUpdate(update);
        }
        onCallRepository.updateUpdate(update);
    }

    public void deleteUpdate(Update update) {
        if (update.isEnabled()) {
            cancelScheduledUpdate(update);
        }
        onCallRepository.deleteUpdate(update);
    }

    public void updateEnableStatusChanged(Update update) {
        if (update.isEnabled()) {
            scheduleUpdate(update);
        } else {
            cancelScheduledUpdate(update);
        }
        onCallRepository.updateUpdate(update);
    }

    public void updateOnCallPerson() {
        onCallRepository.updateOnCallPerson();
    }

    @Override
    public void scheduleUpdate(Update update) {
        AlarmManager alarmManager = (AlarmManager) getApplication().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplication(), SetForwardingRequestReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplication(), update.getId(), intent, 0);
        if (update.isOneTimeUpdate()) {
            try {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, getUpdateExactTimeInMillis(update), pendingIntent);
            } catch (ParseException e) {
                Log.e(getClass().getSimpleName(), "Time or date string retrieved from Update object has wrong format: " + update.getExactDate() + " " + update.getTime());
                // remove update and display message that update failed to be scheduled
            }
        } else {
            // handle repeating updates
        }
    }

    private void cancelScheduledUpdate(Update update) {
        AlarmManager alarmManager = (AlarmManager) getApplication().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplication(), SetForwardingRequestReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplication(), update.getId(), intent, 0);
        alarmManager.cancel(pendingIntent);
    }

    private long getUpdateExactTimeInMillis(Update update) throws ParseException {
        SimpleDateFormat updateExactDateTimeFormat = new SimpleDateFormat("HH:mm EEE, d MMM yyyy", Locale.US);
        Date exactDate = updateExactDateTimeFormat.parse(update.getTime() + " " + update.getExactDate());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(exactDate);
        return calendar.getTimeInMillis();
    }
}

