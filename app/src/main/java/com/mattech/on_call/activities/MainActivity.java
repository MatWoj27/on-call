package com.mattech.on_call.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.mattech.on_call.R;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
