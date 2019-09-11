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
    private LiveData<OnCallPerson> onCallPerson;

    public OnCallRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        onCallPersonDAO = database.getOnCallPersonDAO();
        onCallPerson = onCallPersonDAO.getOnCallPerson();
    }

    public LiveData<OnCallPerson> getOnCallPerson() {
        return onCallPerson;
    }

    public void updateOnCallPerson() {
        UpdateOnCallPersonTask updateTask = new UpdateOnCallPersonTask(onCallPersonDAO);
        updateTask.execute();
    }

    public void setCustomOnCallPerson(OnCallPerson customOnCallPerson) {
        InsertOnCallPersonTask insertTask = new InsertOnCallPersonTask(onCallPersonDAO);
        insertTask.execute(customOnCallPerson);
    }

    private static class UpdateOnCallPersonTask extends AsyncTask<Void, Void, Void> {
        private final String ERROR_TAG = UpdateOnCallPersonTask.class.getSimpleName();
        private OnCallPersonDAO asyncDao;

        UpdateOnCallPersonTask(OnCallPersonDAO asyncDao) {
            this.asyncDao = asyncDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            OnCallPerson result = null;
//            OkHttpClient client = new OkHttpClient();
//            Request request = new Request.Builder()
//                    .get()
//                    .url(webApiUrl)
//                    .build();
//            try {
//                Response response = client.newCall(request).execute();
//                String body = response.body().string();
//                result = OnCallPerson.fromJson(new JSONObject(body));
//                if (result == null) {
//                    new Handler().postDelayed(() -> new UpdateOnCallPersonTask(currentOnCallPerson, asyncDao).execute(), 30 * 1000);
//                } else if (currentOnCallPerson == null || !currentOnCallPerson.getPhoneNumber().equals(result.getPhoneNumber())) {
//                    asyncDao.insert(result);
//                }
//            } catch (IOException | JSONException e) {
//                Log.e(ERROR_TAG, "error", e);
//            }
            result = new OnCallPerson();
            result.setName("Artur Machowicz");
            result.setMail("artur.machowicz@atos.net");
            result.setPhoneNumber("876456789");
            OnCallPerson currentOnCallPerson = asyncDao.getOnCallPerson().getValue();
            if (currentOnCallPerson == null || !currentOnCallPerson.getPhoneNumber().equals(result.getPhoneNumber())) {
                asyncDao.insert(result);
            }
            return null;
        }
    }

    private static class InsertOnCallPersonTask extends AsyncTask<OnCallPerson, Void, Void> {
        private OnCallPersonDAO asyncDao;

        InsertOnCallPersonTask(OnCallPersonDAO asyncDao) {
            this.asyncDao = asyncDao;
        }

        @Override
        protected Void doInBackground(OnCallPerson... onCallPeople) {
            asyncDao.deleteAll();
            asyncDao.insert(onCallPeople[0]);
            return null;
        }
    }
}
