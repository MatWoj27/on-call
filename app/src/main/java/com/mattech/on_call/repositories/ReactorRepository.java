package com.mattech.on_call.repositories;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.mattech.on_call.daos.ReactorDAO;
import com.mattech.on_call.daos.UpdateDAO;
import com.mattech.on_call.databases.ReactorDatabase;
import com.mattech.on_call.databases.UpdateDatabase;
import com.mattech.on_call.models.Reactor;
import com.mattech.on_call.models.Update;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ReactorRepository {
    public static final String REACTOR_CHANGED = "REACTOR_CHANGED";
    private static final String webApiUrl = "http://192.168.43.74:8080/api/reactors?team=L2";
    private ReactorDAO reactorDAO;
    private UpdateDAO updateDAO;
    private LiveData<Reactor> reactorLiveData;
    private LiveData<List<Update>> updates;
    private OperationOnUpdateListener updateListener;

    @FunctionalInterface
    public interface OperationOnUpdateListener {
        void updateAdded(Update update);
    }

    public interface ReactorUpdateListener {
        void reactorUpdated(Reactor newReactor);

        void reactorNotChanged();

        void updateFailed();
    }

    @FunctionalInterface
    public interface ReactorRetrieveListener {
        void reactorRetrieved(Reactor currentReactor);
    }

    @FunctionalInterface
    public interface UpdatesRetrievedListener {
        void updatesRetrieved(List<Update> updates);
    }

    public ReactorRepository(Application application) {
        ReactorDatabase database = ReactorDatabase.getInstance(application);
        reactorDAO = database.getReactorDAO();
        UpdateDatabase updateDatabase = UpdateDatabase.getInstance(application);
        updateDAO = updateDatabase.getUpdateDAO();
        reactorLiveData = reactorDAO.getReactorLiveData();
        updates = updateDAO.getUpdates();
    }

    public LiveData<Reactor> getReactorLiveData() {
        return reactorLiveData;
    }

    public void getReactor(ReactorRetrieveListener listener) {
        GetReactorTask task = new GetReactorTask(reactorDAO, listener);
        task.execute();
    }

    public void updateReactor(@Nullable Reactor currentReactor, ReactorUpdateListener listener) {
        UpdateReactorTask task = new UpdateReactorTask(reactorDAO, currentReactor, listener);
        task.execute();
    }

    public void setCustomReactor(Reactor customReactor) {
        InsertReactorTask task = new InsertReactorTask(reactorDAO);
        task.execute(customReactor);
    }

    public LiveData<List<Update>> getUpdates() {
        return updates;
    }

    public void getActiveUpdates(UpdatesRetrievedListener listener) {
        GetActiveUpdatesTask task = new GetActiveUpdatesTask(updateDAO, listener);
        task.execute();
    }

    public void addUpdate(Update update) {
        InsertUpdateTask task = new InsertUpdateTask(updateDAO, updateListener);
        task.execute(update);
    }

    public void updateUpdate(Update update) {
        UpdateUpdateTask task = new UpdateUpdateTask(updateDAO);
        task.execute(update);
    }

    public void disableUpdate(int id) {
        DisableUpdateTask task = new DisableUpdateTask(updateDAO);
        task.execute(id);
    }

    public void deleteUpdate(Update update) {
        DeleteUpdateTask task = new DeleteUpdateTask(updateDAO);
        task.execute(update);
    }

    private static class GetReactorTask extends AsyncTask<Void, Void, Reactor> {
        private ReactorDAO dao;
        private ReactorRetrieveListener listener;

        GetReactorTask(ReactorDAO dao, ReactorRetrieveListener listener) {
            this.dao = dao;
            this.listener = listener;
        }

        @Override
        protected Reactor doInBackground(Void... voids) {
            return dao.getReactor();
        }

        @Override
        protected void onPostExecute(Reactor reactor) {
            if (listener != null) {
                listener.reactorRetrieved(reactor);
            }
        }
    }

    private static class UpdateReactorTask extends AsyncTask<Void, Void, Reactor> {
        private final String ERROR_TAG = UpdateReactorTask.class.getSimpleName();
        private ReactorDAO dao;
        private Reactor currentReactor;
        private ReactorUpdateListener listener;
        private final OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();

        UpdateReactorTask(ReactorDAO dao, Reactor currentReactor, ReactorUpdateListener listener) {
            this.dao = dao;
            this.currentReactor = currentReactor;
            this.listener = listener;
        }

        @Override
        protected Reactor doInBackground(Void... voids) {
            Reactor result = null;
            Request request = new Request.Builder()
                    .get()
                    .url(webApiUrl)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                String body = response.body().string();
                result = Reactor.fromJson(new JSONObject(body));
                if (result != null) {
                    if (currentReactor == null) {
                        dao.insert(result);
                    } else if (!currentReactor.getPhoneNumber().equals(result.getPhoneNumber())) {
                        dao.updateReactor(currentReactor.getPhoneNumber(), result.getPhoneNumber(), result.getName(), result.getMail());
                    }
                }
            } catch (IOException | JSONException e) {
                Log.e(ERROR_TAG, "error", e);
            }
            return result;
        }

        @Override
        protected void onPostExecute(Reactor newReactor) {
            if (listener != null) {
                if (newReactor == null) {
                    listener.updateFailed();
                } else if (currentReactor == null || !currentReactor.getPhoneNumber().equals(newReactor.getPhoneNumber())) {
                    listener.reactorUpdated(newReactor);
                } else {
                    listener.reactorNotChanged();
                }
            }
        }
    }

    private static class InsertReactorTask extends AsyncTask<Reactor, Void, Void> {
        private ReactorDAO dao;

        InsertReactorTask(ReactorDAO dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(Reactor... reactors) {
            dao.deleteAll();
            dao.insert(reactors[0]);
            return null;
        }
    }

    private static class GetActiveUpdatesTask extends AsyncTask<Void, Void, List<Update>> {
        private UpdateDAO dao;
        private UpdatesRetrievedListener listener;

        GetActiveUpdatesTask(UpdateDAO dao, UpdatesRetrievedListener listener) {
            this.dao = dao;
            this.listener = listener;
        }

        @Override
        protected List<Update> doInBackground(Void... voids) {
            return dao.getActiveUpdates();
        }

        @Override
        protected void onPostExecute(List<Update> updates) {
            if (listener != null) {
                listener.updatesRetrieved(updates);
            }
        }
    }

    private static class InsertUpdateTask extends AsyncTask<Update, Void, Update> {
        private UpdateDAO dao;
        private OperationOnUpdateListener listener;

        InsertUpdateTask(UpdateDAO dao, OperationOnUpdateListener listener) {
            this.dao = dao;
            this.listener = listener;
        }

        @Override
        protected Update doInBackground(Update... updates) {
            Long id = dao.insert(updates[0]);
            updates[0].setId(id.intValue());
            return updates[0];
        }

        @Override
        protected void onPostExecute(Update update) {
            if (listener != null) {
                listener.updateAdded(update);
            }
        }
    }

    private static class UpdateUpdateTask extends AsyncTask<Update, Void, Void> {
        UpdateDAO dao;

        UpdateUpdateTask(UpdateDAO dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(Update... updates) {
            dao.update(updates[0]);
            return null;
        }
    }

    private static class DisableUpdateTask extends AsyncTask<Integer, Void, Void> {
        private UpdateDAO dao;

        DisableUpdateTask(UpdateDAO dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            dao.disableUpdate(integers[0]);
            return null;
        }
    }

    private static class DeleteUpdateTask extends AsyncTask<Update, Void, Void> {
        UpdateDAO dao;

        DeleteUpdateTask(UpdateDAO dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(Update... updates) {
            dao.deleteById(updates[0].getId());
            return null;
        }
    }

    public void setUpdateListener(OperationOnUpdateListener updateListener) {
        this.updateListener = updateListener;
    }
}
