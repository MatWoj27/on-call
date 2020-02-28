package com.mattech.on_call.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.mattech.on_call.activities.ForwardingActivity;
import com.mattech.on_call.models.Update;

public class SetForwardingRequestReceiver extends BroadcastReceiver {
    public static final String EXTRA_IS_ONE_TIME_UPDATE = "isOneTimeUpdate";
    public static final String EXTRA_UPDATE_ID = "updateId";
    public static final String EXTRA_REPETITION_DAYS_TAG = "repetitionDays";
    public static final String EXTRA_UPDATE_TARGET_TIME = "android.intent.extra.ALARM_TARGET_TIME";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent forwardIntent = new Intent(context, ForwardingActivity.class);
        int updateId = intent.getIntExtra(EXTRA_UPDATE_ID, 0);
        if (!intent.getBooleanExtra(EXTRA_IS_ONE_TIME_UPDATE, false)) {
            boolean repetitionDays[] = intent.getBooleanArrayExtra(EXTRA_REPETITION_DAYS_TAG);
            long updateTargetTime = intent.getLongExtra(EXTRA_UPDATE_TARGET_TIME, 0);
            long nextUpdateTargetTime = Update.getNextRepetitionInMillis(updateTargetTime, repetitionDays);
            Intent nextUpdateIntent = new Intent(context, SetForwardingRequestReceiver.class);
            nextUpdateIntent.putExtra(EXTRA_UPDATE_ID, updateId);
            nextUpdateIntent.putExtra(EXTRA_REPETITION_DAYS_TAG, repetitionDays);
            nextUpdateIntent.putExtra(EXTRA_IS_ONE_TIME_UPDATE, false);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, updateId, nextUpdateIntent, 0);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextUpdateTargetTime, pendingIntent);
        } else {
            forwardIntent.putExtra(ForwardingActivity.EXTRA_DISABLE_UPDATE_ID, updateId);
        }
        forwardIntent.putExtra(ForwardingActivity.ACTION_TAG, ForwardingActivity.UPDATE_REACTOR_AND_START_FORWARDING_REQUEST_CODE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
            forwardIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(forwardIntent);
    }
}
