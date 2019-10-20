package com.mattech.on_call;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.mattech.on_call.models.OnCallPerson;

public class ForwardingActivity extends AppCompatActivity {
    public static final int REQUEST_CALL_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forwarding);
        Toast.makeText(this, "Updating!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CALL_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startForwarding(new OnCallPerson());   // mocked on-call person to be changed
            } else {
                Toast.makeText(this, "Setting forwarding is not possible without call permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void startForwarding(OnCallPerson onCallPerson) {
        if (onCallPerson != null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                String[] permissions = new String[]{Manifest.permission.CALL_PHONE};
                requestPermissions(permissions, REQUEST_CALL_PERMISSION_CODE);
            } else {
                Intent callForwardingIntent = new Intent(Intent.ACTION_CALL);
                String callForwardingString = String.format("*21*%s#", String.valueOf(onCallPerson.getPhoneNumber()));
                Uri gsmCode = Uri.fromParts("tel", callForwardingString, "#");
                callForwardingIntent.setData(gsmCode);
                startActivity(callForwardingIntent);
            }
        }
    }
}
