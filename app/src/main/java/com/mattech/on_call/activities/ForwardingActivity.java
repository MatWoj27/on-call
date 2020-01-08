package com.mattech.on_call.activities;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.mattech.on_call.receivers.ForwardingAppWidgetProvider;
import com.mattech.on_call.events.ForwardingEvent;
import com.mattech.on_call.R;
import com.mattech.on_call.repositories.ReactorRepository;
import com.mattech.on_call.models.Reactor;
import com.mattech.on_call.utils.DrawableUtil;

import java.util.ArrayList;
import java.util.List;

public class ForwardingActivity extends AppCompatActivity {
    public static final String ACTION_TAG = "action";
    public static final int UPDATE_REACTOR_AND_START_FORWARDING_REQUEST_CODE = 1;
    public static final int STOP_FORWARDING_REQUEST_CODE = 2;
    public static final int START_FORWARDING_REQUEST_CODE = 3;
    private final int NOTIFICATION_ID = 1;
    private ReactorRepository repository;

    private enum ForwardingResultState {
        FORWARDING_SUCCESS(R.string.forwarding_success_title, R.string.forwarding_success_text, R.drawable.success_icon, R.string.stop_forwarding, R.drawable.cancel_icon, -2, STOP_FORWARDING_REQUEST_CODE),
        FORWARDING_CALL_FAILURE(R.string.forwarding_failure_title, R.string.forwarding_call_failure_text, R.drawable.failure_icon, R.string.retry, R.drawable.retry_icon, -1, START_FORWARDING_REQUEST_CODE),
        FORWARDING_FAILURE_NO_REACTOR(R.string.forwarding_failure_title, R.string.forwarding_no_reactor_text, R.drawable.failure_icon, R.string.retry, R.drawable.retry_icon, -1, UPDATE_REACTOR_AND_START_FORWARDING_REQUEST_CODE);

        int titleId;
        int textId;
        int iconId;
        int buttonTextId;
        int buttonIconId;
        int pendingIntentRequestCode;
        int buttonActionRequestCode;

        ForwardingResultState(int titleId, int textId, int iconId, int buttonTextId, int buttonIconId, int pendingIntentRequestCode, int buttonActionRequestCode) {
            this.titleId = titleId;
            this.textId = textId;
            this.iconId = iconId;
            this.buttonTextId = buttonTextId;
            this.buttonIconId = buttonIconId;
            this.pendingIntentRequestCode = pendingIntentRequestCode;
            this.buttonActionRequestCode = buttonActionRequestCode;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forwarding);
        Intent intent = getIntent();
        repository = new ReactorRepository(getApplication());
        cancelNotificationIfActive();
        switch (intent.getIntExtra(ACTION_TAG, 0)) {
            case UPDATE_REACTOR_AND_START_FORWARDING_REQUEST_CODE:
                repository.getReactor(currentReactor -> {
                    repository.updateReactor(currentReactor, new ReactorRepository.ReactorUpdateListener() {
                        @Override
                        public void reactorUpdated(Reactor newReactor) {
                            Intent reactorChangedIntent = new Intent(ReactorRepository.REACTOR_CHANGED);
                            reactorChangedIntent.putExtra(ForwardingAppWidgetProvider.REACTOR_NAME_TAG, newReactor.getName());
                            reactorChangedIntent.putExtra(ForwardingAppWidgetProvider.REACTOR_PHONE_NUMBER_TAG, newReactor.getPhoneNumber());
                            sendBroadcast(reactorChangedIntent);
                            startForwarding(newReactor);
                        }

                        @Override
                        public void reactorNotChanged() {
                            // to be implemented
                            finishAndRemoveTask();
                        }

                        @Override
                        public void updateFailed() {
                            // to be implemented
                        }
                    });
                });
                break;
            case STOP_FORWARDING_REQUEST_CODE:
                stopForwarding();
                break;
            case START_FORWARDING_REQUEST_CODE:
                repository.getReactor(this::startForwarding);
                break;
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int resultCode : grantResults) {
            if (resultCode != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Application cannot provide its basic functionality without requested permissions", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        switch (requestCode) {
            case START_FORWARDING_REQUEST_CODE:
                startForwarding(repository.getReactorLiveData().getValue());
                break;
            case STOP_FORWARDING_REQUEST_CODE:
                stopForwarding();
                break;
        }
    }

    private void startForwarding(Reactor reactor) {
        if (reactor != null && reactor.getPhoneNumber() != null) {
            String callForwardingString = String.format("*21*%s#", String.valueOf(reactor.getPhoneNumber()));
            makeCall(callForwardingString, START_FORWARDING_REQUEST_CODE, reactor);
        } else {
            showNotification(ForwardingResultState.FORWARDING_FAILURE_NO_REACTOR, reactor);
        }
    }

    private void stopForwarding() {
        makeCall("##21#", STOP_FORWARDING_REQUEST_CODE, null);
    }

    private void makeCall(String callForwardingString, int requestCode, @Nullable Reactor reactor) {
        String permissions[] = {Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE};
        List<String> missingPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (missingPermissions.size() > 0) {
            requestPermissions(missingPermissions.toArray(new String[missingPermissions.size()]), requestCode);
        } else {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            telephonyManager.listen(new PhoneStateListener() {
                @Override
                public void onCallForwardingIndicatorChanged(boolean cfi) {
                    super.onCallForwardingIndicatorChanged(cfi);
                    SharedPreferences preferences = getSharedPreferences("call-forwarding-info", MODE_PRIVATE);
                    boolean callForwardingActive = preferences.getBoolean("CALL_FORWARDING_ACTIVE", false);
                    switch (requestCode) {
                        case START_FORWARDING_REQUEST_CODE:
                            if (cfi || !callForwardingActive) {
                                preferences.edit().putBoolean("CALL_FORWARDING_ACTIVE", true).apply();
                                showNotification(ForwardingResultState.FORWARDING_SUCCESS, reactor);
                                sendBroadcast(new Intent(ForwardingEvent.FORWARDING_STARTED));
                            } else {
                                showNotification(ForwardingResultState.FORWARDING_CALL_FAILURE, null);
                            }
                            break;
                        case STOP_FORWARDING_REQUEST_CODE:
                            if (cfi) {
                                Toast.makeText(ForwardingActivity.this, "Call forwarding canceled", Toast.LENGTH_SHORT).show();
                                sendBroadcast(new Intent(ForwardingEvent.FORWARDING_STOPPED));
                                preferences.edit().putBoolean("CALL_FORWARDING_ACTIVE", false).apply();
                            } else if (callForwardingActive) {
                                Toast.makeText(ForwardingActivity.this, "Failed to cancel forwarding", Toast.LENGTH_SHORT).show();
                            }
                            break;
                    }
                    finishAndRemoveTask();
                }
            }, PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR);
            Intent callForwardingIntent = new Intent(Intent.ACTION_CALL);
            Uri gsmCode = Uri.fromParts("tel", callForwardingString, "#");
            callForwardingIntent.setData(gsmCode);
            startActivity(callForwardingIntent);
        }
    }

    private void showNotification(ForwardingResultState state, Reactor reactor) {
        String longDescription = getResources().getString(state.textId);
        PendingIntent contentTapPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        Intent actionIntent = new Intent(this, ForwardingActivity.class);
        actionIntent.putExtra(ACTION_TAG, state.buttonActionRequestCode);
        if (state == ForwardingResultState.FORWARDING_SUCCESS) {
            longDescription = String.format(longDescription + "\n%s\n%s\n%s", reactor.getName(), reactor.getPhoneNumber(), reactor.getMail());
        }
        PendingIntent buttonPendingIntent = PendingIntent.getActivity(this, state.pendingIntentRequestCode, actionIntent, 0);
        Notification.Action action = new Notification.Action.Builder(Icon.createWithResource(this, state.buttonIconId),
                getResources().getString(state.buttonTextId), buttonPendingIntent).build();
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getResources().getString(state.titleId))
                .setContentText(getResources().getString(state.textId))
                .setStyle(new Notification.BigTextStyle().bigText(longDescription))
                .setLargeIcon(DrawableUtil.vectorToBitmap(getResources().getDrawable(state.iconId, null)))
                .setContentIntent(contentTapPendingIntent)
                .addAction(action)
                .setAutoCancel(true)
                .build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void cancelNotificationIfActive() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        for (StatusBarNotification notification : notificationManager.getActiveNotifications()) {
            if (notification.getId() == NOTIFICATION_ID) {
                notificationManager.cancel(NOTIFICATION_ID);
            }
        }
    }
}
