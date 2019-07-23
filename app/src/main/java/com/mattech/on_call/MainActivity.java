package com.mattech.on_call;

import android.graphics.drawable.AnimationDrawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mattech.on_call.fragments.MainFragment;
import com.mattech.on_call.fragments.SettingsFragment;
import com.mattech.on_call.models.OnCallPerson;
import com.mattech.on_call.tasks.DownloadOnCallDataTask;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MainFragment.ActionPerformedListener, SettingsFragment.ActionPerformedListener, DownloadOnCallDataTask.TaskFinishedListener {
    public static final String ON_CALL_PERSON_TAG = "on-call-person";
    public static final String PROJECT_TAG = "project";
    public static final String TEAM_TAG = "team";
    private OnCallPerson onCallPerson;
    private String project;
    private String team;
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
        if (savedInstanceState != null) {
            onCallPerson = savedInstanceState.getParcelable(ON_CALL_PERSON_TAG);
            project = savedInstanceState.getString(PROJECT_TAG);
            team = savedInstanceState.getString(TEAM_TAG);
        } else{
            // try to get the oncall person data from sqlite or some other storage
            // if not present try to get project and team data from storage
            // if not go to SettingsProject
        }
        MainFragment fragment = new MainFragment();
        changeFragment(fragment, TransitionAnimation.NO_ANIMATION);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(ON_CALL_PERSON_TAG, onCallPerson);
        outState.putString(PROJECT_TAG, project);
        outState.putString(TEAM_TAG, team);
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
        if (onCallPerson != null) {
            // set forwarding
        }
    }

    @Override
    public void goToSettings() {
        changeFragment(new SettingsFragment(), TransitionAnimation.TRANSLATE_RIGHT);
    }

    @Override
    public void taskFinished(OnCallPerson onCallPerson) {
        if (onCallPerson != null && this.onCallPerson != null && !this.onCallPerson.getPhoneNumber().equals(onCallPerson.getPhoneNumber())) {
            this.onCallPerson = onCallPerson;
            startForwarding();
        }
    }

    private void updateOnCallData() {
        if (project != null && team != null) {
            String endpoint = String.format("%s/%s/%s", getResources().getString(R.string.on_call_person_endpoint), project, team);
            DownloadOnCallDataTask task = new DownloadOnCallDataTask();
            task.setListener(this);
            task.execute(getResources().getString(R.string.rest_api_domain) + endpoint);
        } else {
            Toast.makeText(this, "Please define the project and team in settings first", Toast.LENGTH_SHORT).show();
        }
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
