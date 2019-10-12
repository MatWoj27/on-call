package com.mattech.on_call;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class SetForwardingRequestReceiver extends BroadcastReceiver {
    private static final String TAG = "ForwardingReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Updating!", Toast.LENGTH_SHORT).show();
    }
}
