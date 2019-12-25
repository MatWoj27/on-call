package com.mattech.on_call.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mattech.on_call.activities.ForwardingActivity;

import java.util.Calendar;

public class SetForwardingRequestReceiver extends BroadcastReceiver {
    private static final String TAG = "ForwardingReceiver";
    public static final String REPETITION_DAYS_TAG = "repetitionDays";

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean repetitionDays[] = intent.getBooleanArrayExtra(TAG);
        if (repetitionDays != null) {
            Calendar calendar = Calendar.getInstance();
            int todayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 2 == -1 ? 6 : calendar.get(Calendar.DAY_OF_WEEK) - 2;
            if (!repetitionDays[todayIndex]) {
                return;
            }
        }
        Intent forwardIntent = new Intent(context, ForwardingActivity.class);
        forwardIntent.putExtra(ForwardingActivity.ACTION_TAG, ForwardingActivity.UPDATE_REACTOR_AND_START_FORWARDING_REQUEST_CODE);
        context.startActivity(forwardIntent);
    }
}
