package com.mattech.on_call;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.mattech.on_call.models.OnCallPerson;
import com.mattech.on_call.models.Update;

import java.util.List;

public class OnCallPersonViewModel extends AndroidViewModel {
    private LiveData<OnCallPerson> onCallPerson;
    private LiveData<List<Update>> updates;
    private OnCallRepository onCallRepository;

    public OnCallPersonViewModel(@NonNull Application application) {
        super(application);
        onCallRepository = new OnCallRepository(application);
        onCallPerson = onCallRepository.getOnCallPerson();
        updates = onCallRepository.getUpdates();
    }

    public LiveData<OnCallPerson> getOnCallPerson() {
        return onCallPerson;
    }

    public LiveData<List<Update>> getUpdates() {
        return updates;
    }

    public void addUpdate(Update update) {
        onCallRepository.addUpdate(update);
    }

    public void updateOnCallPerson() {
        onCallRepository.updateOnCallPerson();
    }
}

