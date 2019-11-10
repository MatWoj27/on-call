package com.mattech.on_call;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.mattech.on_call.daos.ReactorDAO;
import com.mattech.on_call.daos.UpdateDAO;
import com.mattech.on_call.databases.UpdateDatabase;
import com.mattech.on_call.models.Reactor;
import com.mattech.on_call.models.Update;

import java.util.List;

public class ReactorRepository {
    private static final String webApiUrl = "http://10.84.136.193/api/v1/onCall/Sky/L2";
    private ReactorDAO reactorDAO;
    private UpdateDAO updateDAO;
    private LiveData<Reactor> reactor;
    private LiveData<List<Update>> updates;
    private OperationOnUpdateListener updateListener;

    public interface OperationOnUpdateListener {
        void updateAdded(Update update);
    }

    public ReactorRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        reactorDAO = database.getReactorDAO();
        UpdateDatabase updateDatabase = UpdateDatabase.getInstance(application);
        updateDAO = updateDatabase.getUpdateDAO();
        reactor = reactorDAO.getReactor();
        updates = updateDAO.getUpdates();
    }

    public LiveData<Reactor> getReactor() {
        return reactor;
    }

    public void updateReactor(Reactor currentReactor) {
        UpdateReactorTask updateTask = new UpdateReactorTask(reactorDAO, currentReactor);
        updateTask.execute();
    }

    public void setCustomReactor(Reactor customReactor) {
        InsertReactorTask insertTask = new InsertReactorTask(reactorDAO);
        insertTask.execute(customReactor);
    }

    public LiveData<List<Update>> getUpdates() {
        return updates;
    }

    public void addUpdate(Update update) {
        InsertUpdateTask task = new InsertUpdateTask(updateDAO, updateListener);
        task.execute(update);
    }

    public void updateUpdate(Update update) {
        UpdateUpdateTask task = new UpdateUpdateTask(updateDAO);
        task.execute(update);
    }

    public void deleteUpdate(Update update) {
        DeleteUpdateTask task = new DeleteUpdateTask(updateDAO);
        task.execute(update);
    }

    private static class UpdateReactorTask extends AsyncTask<Void, Void, Void> {
        private final String ERROR_TAG = UpdateReactorTask.class.getSimpleName();
        private ReactorDAO asyncDao;
        private Reactor currentReactor;

        UpdateReactorTask(ReactorDAO asyncDao, Reactor currentReactor) {
            this.asyncDao = asyncDao;
            this.currentReactor = currentReactor;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Reactor result = null;
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
//                    new Handler().postDelayed(() -> new UpdateOnCallPersonTask(currentReactor, asyncDao).execute(), 30 * 1000);
//                } else if (currentReactor == null || !currentReactor.getPhoneNumber().equals(result.getPhoneNumber())) {
//                    asyncDao.insert(result);
//                }
//            } catch (IOException | JSONException e) {
//                Log.e(ERROR_TAG, "error", e);
//            }
            result = new Reactor();
            result.setName("Artur Machowicz");
            result.setMail("artur.machowicz@atos.net");
            result.setPhoneNumber("876456779");
            if (result != null) {
                if (currentReactor == null) {
                    asyncDao.insert(result);
                } else if (!currentReactor.getPhoneNumber().equals(result.getPhoneNumber())) {
                    asyncDao.updateReactor(currentReactor.getPhoneNumber(), result.getPhoneNumber(), result.getName(), result.getMail());
                }
            }
            return null;
        }
    }

    private static class InsertReactorTask extends AsyncTask<Reactor, Void, Void> {
        private ReactorDAO asyncDao;

        InsertReactorTask(ReactorDAO asyncDao) {
            this.asyncDao = asyncDao;
        }

        @Override
        protected Void doInBackground(Reactor... reactors) {
            asyncDao.deleteAll();
            asyncDao.insert(reactors[0]);
            return null;
        }
    }

    private static class InsertUpdateTask extends AsyncTask<Update, Void, Update> {
        private UpdateDAO dao;
        private OperationOnUpdateListener listener;

        public InsertUpdateTask(UpdateDAO dao, OperationOnUpdateListener listener) {
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

        public UpdateUpdateTask(UpdateDAO dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(Update... updates) {
            dao.update(updates[0]);
            return null;
        }
    }

    private static class DeleteUpdateTask extends AsyncTask<Update, Void, Void> {
        UpdateDAO dao;

        public DeleteUpdateTask(UpdateDAO dao) {
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
