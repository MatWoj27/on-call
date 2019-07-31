package com.mattech.on_call;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.mattech.on_call.daos.OnCallPersonDAO;
import com.mattech.on_call.models.OnCallPerson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OnCallRepository {
    private static final String webApiUrl = "http://10.84.136.193/api/v1/onCall/Sky/L2";
    private OnCallPersonDAO onCallPersonDAO;
    private LiveData<OnCallPerson> onCallPersonLiveData;

    public OnCallRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        onCallPersonDAO = database.getOnCallPersonDAO();
        onCallPersonLiveData = onCallPersonDAO.getOnCallPerson();
        if (onCallPersonLiveData.getValue() == null) {
            updateOnCallPerson();
        }
    }

    public LiveData<OnCallPerson> getOnCallPersonLiveData() {
        return onCallPersonLiveData;
    }

    public void updateOnCallPerson() {
        UpdateOnCallPersonTask updateTask = new UpdateOnCallPersonTask(onCallPersonLiveData.getValue(), onCallPersonDAO);
        updateTask.execute();
    }

    private static class UpdateOnCallPersonTask extends AsyncTask<Void, Void, Void> {
        private final String ERROR_TAG = UpdateOnCallPersonTask.class.getSimpleName();
        private OnCallPerson currentOnCallPerson;
        private OnCallPersonDAO asyncDao;

        UpdateOnCallPersonTask(OnCallPerson currentOnCallPerson, OnCallPersonDAO asyncDao) {
            this.currentOnCallPerson = currentOnCallPerson;
            this.asyncDao = asyncDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            OnCallPerson result = null;
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .get()
                    .url(webApiUrl)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                String body = response.body().string();
                result = OnCallPerson.fromJson(new JSONObject(body));
                if (result == null) {
                    new Handler().postDelayed(() -> new UpdateOnCallPersonTask(currentOnCallPerson, asyncDao).execute(), 30 * 1000);
                } else if (currentOnCallPerson == null || !currentOnCallPerson.getPhoneNumber().equals(result.getPhoneNumber())) {
                    asyncDao.insert(result);
                }
            } catch (IOException | JSONException e) {
                Log.e(ERROR_TAG, "error", e);
            }
            return null;
        }
    }
}
