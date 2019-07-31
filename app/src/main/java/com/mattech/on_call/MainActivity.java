package com.mattech.on_call;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mattech.on_call.fragments.MainFragment;
import com.mattech.on_call.fragments.SettingsFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MainFragment.ActionPerformedListener, SettingsFragment.ActionPerformedListener {
    public static final int REQUEST_CALL_PERMISSION_CODE = 1;
    private AnimationDrawable background;

    @BindView(R.id.main_layout)
    LinearLayout mainLayout;

    private enum TransitionAnimation {
        TRANSLATE_LEFT,
        TRANSLATE_RIGHT,
        NO_ANIMATION
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        background = (AnimationDrawable) mainLayout.getBackground();
        background.setEnterFadeDuration(3000);
        background.setExitFadeDuration(4000);
        MainFragment fragment = new MainFragment();
        changeFragment(fragment, TransitionAnimation.NO_ANIMATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CALL_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startForwarding();
            } else {
                Toast.makeText(this, "Setting forwarding is not possible without call permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        background.start();
    }

    @Override
    public void goToMain() {
        changeFragment(new MainFragment(), TransitionAnimation.TRANSLATE_LEFT);
    }

    @Override
    public void startForwarding() {
//        if (onCallPerson != null) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//                String[] permissions = new String[]{Manifest.permission.CALL_PHONE};
//                requestPermissions(permissions, REQUEST_CALL_PERMISSION_CODE);
//            } else {
//                Intent callForwardingIntent = new Intent(Intent.ACTION_CALL);
//                String callForwardingString = String.format("*21*%s#", String.valueOf(onCallPerson.getPhoneNumber()));
//                Uri gsmCode = Uri.fromParts("tel", callForwardingString, "#");
//                callForwardingIntent.setData(gsmCode);
//                startActivity(callForwardingIntent);
//            }
//        }
    }

    @Override
    public void goToSettings() {
        changeFragment(new SettingsFragment(), TransitionAnimation.TRANSLATE_RIGHT);
    }

    private void changeFragment(Fragment fragment, TransitionAnimation animation) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        switch (animation) {
            case TRANSLATE_LEFT:
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_right, R.anim.enter_from_right, R.anim.exit_left);
                break;
            case TRANSLATE_RIGHT:
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_left, R.anim.enter_from_left, R.anim.exit_right);
                break;
            case NO_ANIMATION:
            default:
                break;
        }
        fragmentTransaction.replace(R.id.container, fragment);
        if (!animation.equals(TransitionAnimation.NO_ANIMATION)) {
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();
    }
}
