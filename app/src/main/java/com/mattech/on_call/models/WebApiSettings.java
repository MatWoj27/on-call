package com.mattech.on_call.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.mattech.on_call.Constants;

public class WebApiSettings {
    private String ip;
    private String port;
    private String team;

    private WebApiSettings() {
    }

    public static @NonNull
    WebApiSettings getCurrentSettings(@NonNull Context context) {
        WebApiSettings result = new WebApiSettings();
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.WEB_API_PREFERENCES_NAME, Context.MODE_PRIVATE);
        result.ip = sharedPreferences.getString(Constants.WEB_API_IP_PREFERENCE_KEY, "");
        result.port = sharedPreferences.getString(Constants.WEB_API_PORT_PREFERENCE_KEY, "");
        result.team = sharedPreferences.getString(Constants.WEB_API_TEAM_PREFERENCE_KEY, "");
        return result;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }
}
