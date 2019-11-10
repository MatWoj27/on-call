package com.mattech.on_call;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.mattech.on_call.models.Reactor;
import com.mattech.on_call.utils.DrawableUtil;

import java.util.ArrayList;
import java.util.List;

public class ForwardingActivity extends AppCompatActivity {
    public static final String ACTION_TAG = "action";
    public static final int START_FORWARDING_REQUEST_CODE = 1;
    public static final int STOP_FORWARDING_REQUEST_CODE = 2;
    private ReactorRepository repository;
    private boolean isCurrentPhoneNumberSet;
    private String currentPhoneNumber;
    private final String CURRENT_PHONE_NUMBER_TAG = "phoneNum";
    private final String IS_CURRENT_PHONE_NUMBER_SET_TAG = "isPhoneNumSet";

    private enum ForwardingResultState {
        FORWARDING_SUCCESS(R.string.forwarding_success_title, R.string.forwarding_success_text, R.drawable.success_icon, R.string.stop_forwarding, R.drawable.cancel_icon),
        FORWARDING_CALL_FAILURE(R.string.forwarding_failure_title, R.string.forwarding_call_failure_text, R.drawable.failure_icon, R.string.retry, R.drawable.retry_icon),
        FORWARDING_FAILURE_NO_REACTOR(R.string.forwarding_failure_title, R.string.forwarding_no_reactor_text, R.drawable.failure_icon, R.string.retry, R.drawable.retry_icon);

        int titleId;
        int textId;
        int iconId;
        int buttonTextId;
        int buttonIconId;

        ForwardingResultState(int titleId, int textId, int iconId, int buttonTextId, int buttonIconId) {
            this.titleId = titleId;
            this.textId = textId;
            this.iconId = iconId;
            this.buttonTextId = buttonTextId;
            this.buttonIconId = buttonIconId;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forwarding);
        Intent intent = getIntent();
        switch (intent.getIntExtra(ACTION_TAG, 0)) {
            case START_FORWARDING_REQUEST_CODE:
                repository = new ReactorRepository(getApplication());
                if (savedInstanceState != null) {
                    currentPhoneNumber = savedInstanceState.getString(CURRENT_PHONE_NUMBER_TAG);
                    isCurrentPhoneNumberSet = savedInstanceState.getBoolean(IS_CURRENT_PHONE_NUMBER_SET_TAG);
                }
                repository.getReactor().observe(this, reactor -> {
                    if (isCurrentPhoneNumberSet) {
                        currentPhoneNumber = reactor.getPhoneNumber();
                        startForwarding(reactor);
                    } else {
                        if (reactor != null) {
                            currentPhoneNumber = reactor.getPhoneNumber();
                        }
                        isCurrentPhoneNumberSet = true;
                        repository.updateReactor(reactor);
                    }
                });
                break;
            case STOP_FORWARDING_REQUEST_CODE:
                stopForwarding();
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
                startForwarding(repository.getReactor().getValue());
                break;
            case STOP_FORWARDING_REQUEST_CODE:
                stopForwarding();
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (isCurrentPhoneNumberSet) {
            outState.putString(CURRENT_PHONE_NUMBER_TAG, currentPhoneNumber);
            outState.putBoolean(IS_CURRENT_PHONE_NUMBER_SET_TAG, isCurrentPhoneNumberSet);
        }
    }

    private void startForwarding(Reactor reactor) {
        if (reactor != null && reactor.getPhoneNumber() != null) {
            String callForwardingString = String.format("*21*%s#", String.valueOf(reactor.getPhoneNumber()));
            makeCall(callForwardingString, START_FORWARDING_REQUEST_CODE);
        } else {
            showNotification(ForwardingResultState.FORWARDING_FAILURE_NO_REACTOR, reactor);
        }
    }

    private void stopForwarding() {
        makeCall("##21#", STOP_FORWARDING_REQUEST_CODE);
    }

    private void makeCall(String callForwardingString, int requestCode) {
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
                    if (cfi) {
                        switch (requestCode) {
                            case START_FORWARDING_REQUEST_CODE:
                                repository.getReactor().observe(ForwardingActivity.this, reactor -> {
                                    showNotification(ForwardingResultState.FORWARDING_SUCCESS, reactor);
                                    finish();
                                });
                                break;
                            case STOP_FORWARDING_REQUEST_CODE:
                                Toast.makeText(ForwardingActivity.this, "Call forwarding canceled", Toast.LENGTH_SHORT).show();
                                finish();
                                break;
                        }
                    } else {
                        showNotification(ForwardingResultState.FORWARDING_CALL_FAILURE, null);
                    }
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
        switch (state) {
            case FORWARDING_SUCCESS:
                actionIntent.putExtra(ACTION_TAG, STOP_FORWARDING_REQUEST_CODE);
                longDescription = String.format(longDescription + "\n%s\n%s\n%s", reactor.getName(), reactor.getPhoneNumber(), reactor.getMail());
                break;
            case FORWARDING_CALL_FAILURE:
            case FORWARDING_FAILURE_NO_REACTOR:
                actionIntent.putExtra(ACTION_TAG, START_FORWARDING_REQUEST_CODE);
                break;
        }
        PendingIntent buttonPendingIntent = PendingIntent.getActivity(this, 1, actionIntent, 0);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getResources().getString(state.titleId))
                .setContentText(getResources().getString(state.textId))
                .setStyle(new Notification.BigTextStyle().bigText(longDescription))
                .setLargeIcon(DrawableUtil.vectorToBitmap(getResources().getDrawable(state.iconId, null)))
                .setContentIntent(contentTapPendingIntent)
                .addAction(state.buttonIconId, getResources().getString(state.buttonTextId), buttonPendingIntent)
                .setAutoCancel(true)
                .build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }
}
