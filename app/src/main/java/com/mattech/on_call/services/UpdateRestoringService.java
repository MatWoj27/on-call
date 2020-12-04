package com.mattech.on_call.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.mattech.on_call.models.Update;
import com.mattech.on_call.receivers.SetForwardingRequestReceiver;
import com.mattech.on_call.repositories.ReactorRepository;
import com.mattech.on_call.utils.DateTimeUtil;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class UpdateRestoringService extends IntentService {
    private ReactorRepository repository;

    public UpdateRestoringService() {
        super("UpdateRestoringService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        repository = new ReactorRepository(getApplication());
        repository.getActiveUpdates(this::restoreActiveUpdates);
    }

    private void restoreActiveUpdates(@NonNull List<Update> activeUpdates) {
        for (Update update : activeUpdates) {
            try {
                long updateTime = update.getPlannedUpdateTimeInMillis();
                if (update.isOneTimeUpdate() && DateTimeUtil.isMomentInPast(new Date(updateTime))) {
                    repository.disableUpdate(update.getId());
                    continue;
                }
                scheduleUpdate(update, updateTime);
            } catch (ParseException e) {
                Log.e("error", "Time or date string retrieved from Update object has wrong format: " + update.getExactDate() + " " + update.getTime(), e);
            }
        }
    }

    private void scheduleUpdate(@NonNull Update update, long updateTimeInMillis) {
        AlarmManager alarmManager = getSystemService(AlarmManager.class);
        Intent intent = new Intent(this, SetForwardingRequestReceiver.class);
        if (!update.isOneTimeUpdate()) {
            intent.putExtra(SetForwardingRequestReceiver.EXTRA_REPETITION_DAYS_TAG, update.getRepetitionDays());
        }
        intent.putExtra(SetForwardingRequestReceiver.EXTRA_IS_ONE_TIME_UPDATE, update.isOneTimeUpdate());
        intent.putExtra(SetForwardingRequestReceiver.EXTRA_UPDATE_ID, update.getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, update.getId(), intent, 0);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, updateTimeInMillis, pendingIntent);
    }
}
