package com.mattech.on_call;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.mattech.on_call.daos.OnCallPersonDAO;
import com.mattech.on_call.daos.UpdateDAO;
import com.mattech.on_call.databases.UpdateDatabase;
import com.mattech.on_call.models.OnCallPerson;
import com.mattech.on_call.models.Update;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OnCallRepository {
    private static final String webApiUrl = "http://10.84.136.193/api/v1/onCall/Sky/L2";
    private OnCallPersonDAO onCallPersonDAO;
    private UpdateDAO updateDAO;
    private LiveData<OnCallPerson> onCallPerson;
    private LiveData<List<Update>> updates;

    public OnCallRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        onCallPersonDAO = database.getOnCallPersonDAO();
        UpdateDatabase updateDatabase = UpdateDatabase.getInstance(application);
        updateDAO = updateDatabase.getUpdateDAO();
        onCallPerson = onCallPersonDAO.getOnCallPerson();
        updates = updateDAO.getUpdates();
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

    public LiveData<List<Update>> getUpdates() {
        return updates;
    }

    public void addUpdate(Update update) {
        InsertUpdateTask task = new InsertUpdateTask(updateDAO);
        task.execute(update);
    }

    public void updateUpdate(Update update) {
        UpdateUpdateTask task = new UpdateUpdateTask(updateDAO);
        task.execute(update);
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

    private static class InsertUpdateTask extends AsyncTask<Update, Void, Void> {
        private UpdateDAO dao;

        public InsertUpdateTask(UpdateDAO dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(Update... updates) {
            dao.insert(updates[0]);
            return null;
        }
    }

    private static class UpdateUpdateTask extends AsyncTask<Update, Void, Void> {
        UpdateDAO dao;

        public UpdateUpdateTask(UpdateDAO dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(Update... updates) {
            dao.update(updates[0]);
            return null;
        }
    }
}
