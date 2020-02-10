package com.mattech.on_call.view_models;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.mattech.on_call.R;
import com.mattech.on_call.exceptions.UpdateNotScheduledException;
import com.mattech.on_call.models.Update;
import com.mattech.on_call.receivers.SetForwardingRequestReceiver;
import com.mattech.on_call.repositories.ReactorRepository;
import com.mattech.on_call.utils.DateTimeUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UpdateViewModel extends AndroidViewModel implements ReactorRepository.OperationOnUpdateListener {
    private ReactorRepository reactorRepository;
    private LiveData<List<Update>> updates;

    public UpdateViewModel(@NonNull Application application) {
        super(application);
        reactorRepository = new ReactorRepository(application);
        updates = reactorRepository.getUpdates();
        reactorRepository.setUpdateListener(this);
    }

    public void addUpdate(Update update) {
        reactorRepository.addUpdate(update);
    }

    public LiveData<List<Update>> getUpdates() {
        return updates;
    }

    @Override
    public void updateAdded(Update update) {
        try {
            scheduleUpdate(update);
        } catch (UpdateNotScheduledException e) {
            handleNotScheduledUpdate(update, e);
        }
    }

    public void updateUpdate(Update update) {
        if (update.isEnabled()) {
            try {
                scheduleUpdate(update);
            } catch (UpdateNotScheduledException e) {
                handleNotScheduledUpdate(update, e);
            }
        }
        reactorRepository.updateUpdate(update);
    }

    public void deleteUpdate(Update update) {
        if (update.isEnabled()) {
            cancelScheduledUpdate(update);
        }
        reactorRepository.deleteUpdate(update);
    }

    public void updateEnableStatusChanged(Update update) {
        if (update.isEnabled()) {
            try {
                scheduleUpdate(update);
            } catch (UpdateNotScheduledException e) {
                handleNotScheduledUpdate(update, e);
            }
        } else {
            cancelScheduledUpdate(update);
        }
        reactorRepository.updateUpdate(update);
    }

    private void scheduleUpdate(Update update) throws UpdateNotScheduledException {
        Intent intent = new Intent(getApplication(), SetForwardingRequestReceiver.class);
        PendingIntent pendingIntent;
        long updateTime;
        if (update.isOneTimeUpdate()) {
            try {
                updateTime = update.getExactDateTimeInMillis();
                if (DateTimeUtil.isMomentInPast(new Date(updateTime))) {
                    updateTime = update.getTodayUpdateTimeInMillis() + 24 * 60 * 60 * 1000;
                    SimpleDateFormat dateFormat = new SimpleDateFormat(Update.DATE_FORMAT, Locale.getDefault());
                    update.setExactDate(dateFormat.format(new Date(updateTime)));
                    Toast.makeText(getApplication(), getApplication().getResources().getString(R.string.past_update_rescheduled_warning), Toast.LENGTH_SHORT).show();
                }
            } catch (ParseException e) {
                throw new UpdateNotScheduledException("Time or date string retrieved from Update object has wrong format: " + update.getExactDate() + " " + update.getTime(), e);
            }
        } else {
            try {
                updateTime = getNextUpdateTimeInMillis(update);
                intent.putExtra(SetForwardingRequestReceiver.EXTRA_REPETITION_DAYS_TAG, update.getRepetitionDays());
            } catch (ParseException e) {
                throw new UpdateNotScheduledException("Time string retrieved from Update object has wrong format: " + update.getTime(), e);
            }
        }
        intent.putExtra(SetForwardingRequestReceiver.EXTRA_IS_ONE_TIME_UPDATE, update.isOneTimeUpdate());
        intent.putExtra(SetForwardingRequestReceiver.EXTRA_UPDATE_ID, update.getId());
        pendingIntent = PendingIntent.getBroadcast(getApplication(), update.getId(), intent, 0);
        AlarmManager alarmManager = (AlarmManager) getApplication().getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, updateTime, pendingIntent);
    }

    private void cancelScheduledUpdate(Update update) {
        AlarmManager alarmManager = (AlarmManager) getApplication().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplication(), SetForwardingRequestReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplication(), update.getId(), intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    private long getNextUpdateTimeInMillis(Update update) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        long currentTimeInMillis = calendar.getTimeInMillis();
        long todayUpdateTimeInMillis = update.getTodayUpdateTimeInMillis();
        int todayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 2 == -1 ? 6 : calendar.get(Calendar.DAY_OF_WEEK) - 2;
        if (update.getRepetitionDays()[todayIndex] && todayUpdateTimeInMillis > currentTimeInMillis) {
            return todayUpdateTimeInMillis;
        } else {
            return Update.getNextRepetitionInMillis(todayUpdateTimeInMillis, update.getRepetitionDays());
        }
    }

    private void handleNotScheduledUpdate(Update update, UpdateNotScheduledException e) {
        Log.e(getClass().getSimpleName(), e.getMessage(), e);
        deleteUpdate(update);
        Toast.makeText(getApplication(), getApplication().getResources().getString(R.string.update_scheduling_error_info), Toast.LENGTH_SHORT).show();
    }
}
