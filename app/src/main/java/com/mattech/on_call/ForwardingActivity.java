package com.mattech.on_call;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.mattech.on_call.models.OnCallPerson;
import com.mattech.on_call.utils.DrawableUtil;

public class ForwardingActivity extends AppCompatActivity {
    public static final int REQUEST_CALL_PERMISSION_CODE = 1;
    public static final int SET_FORWARDING_REQUEST = 1;
    private OnCallRepository repository;
    private boolean isCurrentPhoneNumberSet;
    private String currentPhoneNumber;
    private final String CURRENT_PHONE_NUMBER_TAG = "phoneNum";
    private final String IS_CURRENT_PHONE_NUMBER_SET_TAG = "isPhoneNumSet";

    private enum ForwardingResultState {
        FORWARDING_SUCCESS(R.string.forwarding_success_title, R.string.forwarding_success_text, R.drawable.success_icon),
        FORWARDING_FAILURE_NO_REACTOR(R.string.forwarding_failure_title, R.string.forwarding_no_reactor_text, R.drawable.failure_icon);

        int titleId;
        int textId;
        int iconId;

        ForwardingResultState(int titleId, int textId, int iconId) {
            this.titleId = titleId;
            this.textId = textId;
            this.iconId = iconId;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forwarding);
        repository = new OnCallRepository(getApplication());
        if (savedInstanceState != null) {
            currentPhoneNumber = savedInstanceState.getString(CURRENT_PHONE_NUMBER_TAG);
            isCurrentPhoneNumberSet = savedInstanceState.getBoolean(IS_CURRENT_PHONE_NUMBER_SET_TAG);
        }
        repository.getOnCallPerson().observe(this, onCallPerson -> {
            if (isCurrentPhoneNumberSet) {
                currentPhoneNumber = onCallPerson.getPhoneNumber();
                startForwarding(onCallPerson);
            } else {
                if (onCallPerson != null) {
                    currentPhoneNumber = onCallPerson.getPhoneNumber();
                }
                isCurrentPhoneNumberSet = true;
                repository.updateOnCallPerson(onCallPerson);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CALL_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startForwarding(repository.getOnCallPerson().getValue());
            } else {
                Toast.makeText(this, "Setting forwarding is not possible without call permission", Toast.LENGTH_SHORT).show();
            }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        showNotification(ForwardingResultState.FORWARDING_SUCCESS);
        finish();
    }

    private void startForwarding(OnCallPerson onCallPerson) {
        if (onCallPerson != null && onCallPerson.getPhoneNumber() != null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                String[] permissions = new String[]{Manifest.permission.CALL_PHONE};
                requestPermissions(permissions, REQUEST_CALL_PERMISSION_CODE);
            } else {
                Intent callForwardingIntent = new Intent(Intent.ACTION_CALL);
                String callForwardingString = String.format("*21*%s#", String.valueOf(onCallPerson.getPhoneNumber()));
                Uri gsmCode = Uri.fromParts("tel", callForwardingString, "#");
                callForwardingIntent.setData(gsmCode);
                startActivityForResult(callForwardingIntent, SET_FORWARDING_REQUEST);
            }
        } else {
            showNotification(ForwardingResultState.FORWARDING_FAILURE_NO_REACTOR);
        }
    }

    private void showNotification(ForwardingResultState state) {
        String description = getResources().getString(state.textId);
        if (state == ForwardingResultState.FORWARDING_SUCCESS) {
            description = description + currentPhoneNumber;
        }
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getResources().getString(state.titleId))
                .setContentText(description)
                .setLargeIcon(DrawableUtil.vectorToBitmap(getResources().getDrawable(state.iconId, null)))
                .build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }
}
