package com.mattech.on_call.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.mattech.on_call.models.OnCallPerson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadOnCallDataTask extends AsyncTask<String, Void, OnCallPerson> {
    private static final String ERROR_TAG = DownloadOnCallDataTask.class.getSimpleName();
    private TaskFinishedListener listener;

    public interface TaskFinishedListener {
        void taskFinished(OnCallPerson onCallPerson);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected OnCallPerson doInBackground(String... args) {
        OnCallPerson result = null;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .get()
                .url(args[0])
                .build();
        try {
            Response response = client.newCall(request).execute();
            String body = response.body().string();
            result = OnCallPerson.fromJson(new JSONObject(body));
        } catch (IOException | JSONException e) {
            Log.e(ERROR_TAG, "error", e);
        }
        return result;
    }

    @Override
    protected void onPostExecute(OnCallPerson onCallPerson) {
        if (listener != null) {
            listener.taskFinished(onCallPerson);
        }
    }

    public void setListener(TaskFinishedListener listener) {
        this.listener = listener;
    }
}
