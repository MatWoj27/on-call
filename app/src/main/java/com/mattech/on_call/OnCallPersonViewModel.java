package com.mattech.on_call;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.mattech.on_call.models.OnCallPerson;

public class OnCallPersonViewModel extends AndroidViewModel {
    private LiveData<OnCallPerson> onCallPersonLiveData;
    private OnCallRepository onCallRepository;

    public OnCallPersonViewModel(@NonNull Application application) {
        super(application);
        onCallRepository = new OnCallRepository(application);
        onCallPersonLiveData = onCallRepository.getOnCallPersonLiveData();
    }

    public LiveData<OnCallPerson> getOnCallPersonLiveData() {
        return onCallPersonLiveData;
    }

    public void updateOnCallPerson() {
        onCallRepository.updateOnCallPerson();
    }
}

