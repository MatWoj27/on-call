package com.mattech.on_call.view_models;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
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

    public void addUpdate(@NonNull Update update) {
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

    public void updateUpdate(@NonNull Update update) {
        if (update.isEnabled()) {
            try {
                scheduleUpdate(update);
            } catch (UpdateNotScheduledException e) {
                handleNotScheduledUpdate(update, e);
            }
        }
        reactorRepository.updateUpdate(update);
    }

    public void deleteUpdate(@NonNull Update update) {
        if (update.isEnabled()) {
            cancelScheduledUpdate(update);
        }
        reactorRepository.deleteUpdate(update);
    }

    public void updateEnableStatusChanged(@NonNull Update update) {
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

    private void scheduleUpdate(@NonNull Update update) throws UpdateNotScheduledException {
        try {
            Intent intent = new Intent(getApplication(), SetForwardingRequestReceiver.class);
            PendingIntent pendingIntent;
            long updateTime = update.getPlannedUpdateTimeInMillis();
            if (!update.isOneTimeUpdate()) {
                intent.putExtra(SetForwardingRequestReceiver.EXTRA_REPETITION_DAYS_TAG, update.getRepetitionDays());
            } else if (DateTimeUtil.isMomentInPast(new Date(updateTime))) {
                updateTime = update.getTodayUpdateTimeInMillis() + 24 * 60 * 60 * 1000;
                SimpleDateFormat dateFormat = new SimpleDateFormat(Update.DATE_FORMAT, Locale.getDefault());
                update.setExactDate(dateFormat.format(new Date(updateTime)));
                Toast.makeText(getApplication(), getApplication().getString(R.string.past_update_rescheduled_warning), Toast.LENGTH_SHORT).show();
            }
            intent.putExtra(SetForwardingRequestReceiver.EXTRA_IS_ONE_TIME_UPDATE, update.isOneTimeUpdate());
            intent.putExtra(SetForwardingRequestReceiver.EXTRA_UPDATE_ID, update.getId());
            intent.putExtra(SetForwardingRequestReceiver.EXTRA_PRECONFIGURED_PHONE_NUMBER, update.getPreconfiguredPhoneNumber());
            pendingIntent = PendingIntent.getBroadcast(getApplication(), update.getId(), intent, 0);
            AlarmManager alarmManager = getApplication().getSystemService(AlarmManager.class);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, updateTime, pendingIntent);
        } catch (ParseException e) {
            if (update.isOneTimeUpdate()) {
                throw new UpdateNotScheduledException("Time or date string retrieved from Update object has wrong format: " + update.getExactDate() + " " + update.getTime(), e);
            } else {
                throw new UpdateNotScheduledException("Time string retrieved from Update object has wrong format: " + update.getTime(), e);
            }
        }
    }

    private void cancelScheduledUpdate(@NonNull Update update) {
        AlarmManager alarmManager = getApplication().getSystemService(AlarmManager.class);
        Intent intent = new Intent(getApplication(), SetForwardingRequestReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplication(), update.getId(), intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    private void handleNotScheduledUpdate(Update update, UpdateNotScheduledException e) {
        Log.e(getClass().getSimpleName(), e.getMessage(), e);
        deleteUpdate(update);
        Toast.makeText(getApplication(), getApplication().getString(R.string.update_scheduling_error_info), Toast.LENGTH_SHORT).show();
    }
}
