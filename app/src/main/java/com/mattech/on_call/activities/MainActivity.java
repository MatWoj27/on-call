package com.mattech.on_call.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.mattech.on_call.R;
import com.mattech.on_call.fragments.SettingsDialogFragment;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getIntent().getIntExtra(ForwardingActivity.ACTION_TAG, 0) == ForwardingActivity.GO_TO_SETTINGS_REQUEST_CODE) {
            new SettingsDialogFragment().show(getSupportFragmentManager(), "settings");
        }
    }
}
