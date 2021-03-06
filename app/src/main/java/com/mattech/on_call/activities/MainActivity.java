package com.mattech.on_call.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.mattech.on_call.Constants;
import com.mattech.on_call.R;
import com.mattech.on_call.fragments.SettingsDialogFragment;
import com.mattech.on_call.utils.NotificationUtil;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getIntExtra(ForwardingActivity.ACTION_TAG, 0) == ForwardingActivity.GO_TO_SETTINGS_REQUEST_CODE) {
            NotificationUtil.cancelActiveForwardingResultNotification(this, Constants.FORWARDING_NOTIFICATION_ID);
            new SettingsDialogFragment().show(getSupportFragmentManager(), "settings");
        }
    }
}
