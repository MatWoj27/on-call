package com.mattech.on_call.view_models;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;

import com.mattech.on_call.repositories.ReactorRepository;
import com.mattech.on_call.activities.ForwardingActivity;
import com.mattech.on_call.models.Reactor;

public class ReactorViewModel extends AndroidViewModel {
    private LiveData<Reactor> reactor;
    private ReactorRepository reactorRepository;

    public ReactorViewModel(@NonNull Application application) {
        super(application);
        reactorRepository = new ReactorRepository(application);
        reactor = reactorRepository.getReactorLiveData();
    }

    public LiveData<Reactor> getReactor() {
        return reactor;
    }

    public void updateReactor() {
        Intent intent = new Intent(getApplication().getApplicationContext(), ForwardingActivity.class);
        intent.putExtra(ForwardingActivity.ACTION_TAG, ForwardingActivity.UPDATE_REACTOR_AND_START_FORWARDING_REQUEST_CODE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        getApplication().getApplicationContext().startActivity(intent);
    }
}
