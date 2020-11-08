package com.mattech.on_call.activities;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.mattech.on_call.Constants;
import com.mattech.on_call.models.WebApiSettings;
import com.mattech.on_call.receivers.ForwardingAppWidgetProvider;
import com.mattech.on_call.events.ForwardingEvent;
import com.mattech.on_call.R;
import com.mattech.on_call.repositories.ReactorRepository;
import com.mattech.on_call.models.Reactor;
import com.mattech.on_call.utils.DrawableUtil;
import com.mattech.on_call.utils.NotificationUtil;

import java.util.ArrayList;
import java.util.List;

public class ForwardingActivity extends AppCompatActivity {
    public static final String ACTION_TAG = "action";
    public static final int UPDATE_REACTOR_AND_START_FORWARDING_REQUEST_CODE = 1;
    public static final int STOP_FORWARDING_REQUEST_CODE = 2;
    public static final int START_FORWARDING_REQUEST_CODE = 3;
    public static final int GO_TO_SETTINGS_REQUEST_CODE = 4;
    public static final int CUSTOM_PHONE_NUMBER_START_FORWARDING_REQUEST_CODE = 5;
    public static final String EXTRA_DISABLE_UPDATE_ID = "disableUpdateId";
    public static final String EXTRA_PHONE_NUMBER = "phoneNumber";

    private static final String ERROR_TAG = ForwardingActivity.class.getSimpleName();
    private final String CALL_FORWARDING_PREFERENCES_NAME = "call-forwarding-info";
    private final String CALL_FORWARDING_ACTIVE_PREFERENCE_KEY = "CALL_FORWARDING_ACTIVE";

    private ReactorRepository repository;

    private enum ForwardingResultState {
        UPDATE_FAILURE_WEB_API_IP_NOT_SET(R.string.update_failure_title, R.string.update_failure_no_web_api_ip_text, R.drawable.failure_icon, R.string.settings, R.drawable.settings_notification_icon, -3, GO_TO_SETTINGS_REQUEST_CODE),
        REACTOR_NOT_CHANGED(R.string.reactor_not_changed_title, R.string.reactor_not_changed_text, R.drawable.success_icon, R.string.stop_forwarding, R.drawable.cancel_icon, -2, STOP_FORWARDING_REQUEST_CODE),
        UPDATE_FAILURE(R.string.update_failure_title, R.string.update_failure_text, R.drawable.failure_icon, R.string.retry, R.drawable.retry_icon, -1, UPDATE_REACTOR_AND_START_FORWARDING_REQUEST_CODE),
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

        ForwardingResultState(@StringRes int titleId, @StringRes int textId, @DrawableRes int iconId,
                              @StringRes int buttonTextId, @DrawableRes int buttonIconId,
                              int pendingIntentRequestCode, int buttonActionRequestCode) {
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
        if (intent.hasExtra(EXTRA_DISABLE_UPDATE_ID)) {
            repository.disableUpdate(intent.getIntExtra(EXTRA_DISABLE_UPDATE_ID, -1));
        }
        handleForwardingAction(intent.getIntExtra(ACTION_TAG, 0));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int resultCode : grantResults) {
            if (resultCode != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getString(R.string.missing_permissions_info), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        switch (requestCode) {
            case START_FORWARDING_REQUEST_CODE:
                repository.getReactor(this::startForwarding);
                break;
            case STOP_FORWARDING_REQUEST_CODE:
                stopForwarding();
                break;
        }
    }

    private void handleForwardingAction(final int actionCode) {
        switch (actionCode) {
            case UPDATE_REACTOR_AND_START_FORWARDING_REQUEST_CODE:
                repository.getReactor(currentReactor -> {
                    WebApiSettings webApiSettings = WebApiSettings.getCurrentSettings(this);
                    if (!webApiSettings.getIp().isEmpty()) {
                        ReactorUpdateListener listener = new ReactorUpdateListener(currentReactor);
                        repository.updateReactor(currentReactor, listener, webApiSettings);
                    } else {
                        showNotification(ForwardingResultState.UPDATE_FAILURE_WEB_API_IP_NOT_SET, null);
                        finish();
                    }
                });
                break;
            case STOP_FORWARDING_REQUEST_CODE:
                stopForwarding();
                break;
            case START_FORWARDING_REQUEST_CODE:
                repository.getReactor(this::startForwarding);
                break;
            case CUSTOM_PHONE_NUMBER_START_FORWARDING_REQUEST_CODE:
                handleCustomPhoneNumberForwardingAction();
                break;
            default:
                Log.wtf(ERROR_TAG, "Unknown action request code received: " + actionCode);
                break;
        }
    }

    private void startForwarding(@Nullable Reactor reactor) {
        if (reactor != null && !reactor.getPhoneNumber().isEmpty()) {
            String callForwardingString = String.format("*21*%s#", reactor.getPhoneNumber());
            makeCall(callForwardingString, START_FORWARDING_REQUEST_CODE, reactor);
        } else {
            showNotification(ForwardingResultState.FORWARDING_FAILURE_NO_REACTOR, reactor);
        }
    }

    private void stopForwarding() {
        makeCall("##21#", STOP_FORWARDING_REQUEST_CODE, null);
    }

    private void makeCall(String callForwardingString, int requestCode, @Nullable Reactor reactor) {
        String[] permissions = {Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE};
        List<String> missingPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (missingPermissions.size() > 0) {
            requestPermissions(missingPermissions.toArray(new String[missingPermissions.size()]), requestCode);
        } else {
            TelephonyManager telephonyManager = getSystemService(TelephonyManager.class);
            telephonyManager.listen(new CallForwardingIndicatorListener(reactor, requestCode),
                    PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR);
            Intent callForwardingIntent = new Intent(Intent.ACTION_CALL);
            Uri gsmCode = Uri.fromParts("tel", callForwardingString, "#");
            callForwardingIntent.setData(gsmCode);
            startActivity(callForwardingIntent);
        }
    }

    private void showNotification(@NonNull ForwardingResultState state, @Nullable Reactor reactor) {
        String longDescription = getString(state.textId);
        PendingIntent contentTapPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        Class cls = state == ForwardingResultState.UPDATE_FAILURE_WEB_API_IP_NOT_SET ? MainActivity.class : ForwardingActivity.class;
        Intent actionIntent = new Intent(this, cls);
        actionIntent.putExtra(ACTION_TAG, state.buttonActionRequestCode);
        if (state == ForwardingResultState.FORWARDING_SUCCESS && reactor != null && reactor.getName() != null) {
            longDescription = String.format(longDescription + "\n%s\n%s\n%s", reactor.getName(), reactor.getPhoneNumber(), reactor.getMail());
        }
        PendingIntent buttonPendingIntent = PendingIntent.getActivity(this, state.pendingIntentRequestCode, actionIntent, 0);
        Notification.Action action = new Notification.Action.Builder(Icon.createWithResource(this, state.buttonIconId),
                getString(state.buttonTextId), buttonPendingIntent).build();
        Notification.Builder builder = NotificationUtil.getNotificationBuilder(this, Constants.FORWARDING_CHANNEL_INFO);
        Notification notification = builder
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(state.titleId))
                .setContentText(getString(state.textId))
                .setStyle(new Notification.BigTextStyle().bigText(longDescription))
                .setLargeIcon(DrawableUtil.vectorToBitmap(getResources().getDrawable(state.iconId, null)))
                .setContentIntent(contentTapPendingIntent)
                .addAction(action)
                .setAutoCancel(true)
                .build();
        NotificationUtil.cancelActiveForwardingResultNotification(this, Constants.FORWARDING_NOTIFICATION_ID);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify(Constants.FORWARDING_NOTIFICATION_ID, notification);
    }

    private void handleCustomPhoneNumberForwardingAction() {
        String preconfiguredPhoneNumber = getIntent().getStringExtra(EXTRA_PHONE_NUMBER);
        Reactor customReactor = Reactor.getCustomReactor(this, preconfiguredPhoneNumber);
        repository.setCustomReactor(customReactor);
        notifyReactorChange(customReactor);
        startForwarding(customReactor);
    }

    private void notifyReactorChange(@NonNull Reactor reactor) {
        Intent reactorChangedIntent = new Intent(ReactorRepository.REACTOR_CHANGED);
        reactorChangedIntent.putExtra(ForwardingAppWidgetProvider.REACTOR_NAME_TAG, reactor.getName());
        reactorChangedIntent.putExtra(ForwardingAppWidgetProvider.REACTOR_PHONE_NUMBER_TAG, reactor.getPhoneNumber());
        sendBroadcast(reactorChangedIntent);
    }

    private class ReactorUpdateListener implements ReactorRepository.ReactorUpdateListener {
        private Reactor currentReactor;

        ReactorUpdateListener(@NonNull Reactor currentReactor) {
            this.currentReactor = currentReactor;
        }

        @Override
        public void reactorUpdated(@NonNull Reactor newReactor) {
            notifyReactorChange(newReactor);
            startForwarding(newReactor);
        }

        @Override
        public void reactorNotChanged() {
            SharedPreferences preferences = getSharedPreferences(CALL_FORWARDING_PREFERENCES_NAME, MODE_PRIVATE);
            if (preferences.getBoolean(CALL_FORWARDING_ACTIVE_PREFERENCE_KEY, false)) {
                showNotification(ForwardingResultState.REACTOR_NOT_CHANGED, null);
                finish();
            } else {
                startForwarding(currentReactor);
            }
        }

        @Override
        public void updateFailed() {
            showNotification(ForwardingResultState.UPDATE_FAILURE, null);
            finish();
        }
    }

    private class CallForwardingIndicatorListener extends PhoneStateListener {
        private Reactor reactor;
        private int requestCode;

        CallForwardingIndicatorListener(@Nullable Reactor reactor, int requestCode) {
            this.reactor = reactor;
            this.requestCode = requestCode;
        }

        @Override
        public void onCallForwardingIndicatorChanged(boolean cfi) {
            super.onCallForwardingIndicatorChanged(cfi);
            SharedPreferences preferences = getSharedPreferences(CALL_FORWARDING_PREFERENCES_NAME, MODE_PRIVATE);
            boolean callForwardingActive = preferences.getBoolean(CALL_FORWARDING_ACTIVE_PREFERENCE_KEY, false);
            switch (requestCode) {
                case START_FORWARDING_REQUEST_CODE:
                    if (cfi || !callForwardingActive) {
                        preferences.edit().putBoolean(CALL_FORWARDING_ACTIVE_PREFERENCE_KEY, true).apply();
                        showNotification(ForwardingResultState.FORWARDING_SUCCESS, reactor);
                        sendBroadcast(new Intent(ForwardingEvent.FORWARDING_STARTED));
                    } else {
                        showNotification(ForwardingResultState.FORWARDING_CALL_FAILURE, null);
                    }
                    break;
                case STOP_FORWARDING_REQUEST_CODE:
                    if (cfi) {
                        NotificationUtil.cancelActiveForwardingResultNotification(ForwardingActivity.this, Constants.FORWARDING_NOTIFICATION_ID);
                        Toast.makeText(ForwardingActivity.this, getString(R.string.forwarding_canceled_info), Toast.LENGTH_SHORT).show();
                        sendBroadcast(new Intent(ForwardingEvent.FORWARDING_STOPPED));
                        preferences.edit().putBoolean(CALL_FORWARDING_ACTIVE_PREFERENCE_KEY, false).apply();
                    } else if (callForwardingActive) {
                        Toast.makeText(ForwardingActivity.this, getString(R.string.forwarding_cancellation_failure_info), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
            finish();
        }
    }
}
