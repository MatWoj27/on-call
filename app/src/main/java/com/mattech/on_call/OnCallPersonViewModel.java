package com.mattech.on_call;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.mattech.on_call.models.OnCallPerson;

public class OnCallPersonViewModel extends AndroidViewModel {
    private LiveData<OnCallPerson> onCallPerson;
    private OnCallRepository onCallRepository;

    public OnCallPersonViewModel(@NonNull Application application) {
        super(application);
        onCallRepository = new OnCallRepository(application);
        onCallPerson = onCallRepository.getOnCallPerson();
    }

    public LiveData<OnCallPerson> getOnCallPerson() {
        return onCallPerson;
    }

    public void updateOnCallPerson() {
        onCallRepository.updateOnCallPerson();
    }
}

