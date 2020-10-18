package com.mattech.on_call.view_models;

import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;

import com.mattech.on_call.Constants;
import com.mattech.on_call.models.WebApiSettings;
import com.mattech.on_call.utils.WebUtil;

public class SettingsViewModel extends ViewModel {
    public WebApiSettings settings;

    public void setSettings(WebApiSettings settings) {
        if (this.settings == null) {
            this.settings = settings;
        }
    }

    public void updateSettings(@NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.WEB_API_PREFERENCES_NAME, Context.MODE_PRIVATE);
        if (WebUtil.isValidIPv4(settings.getIp())) {
            updatePreferenceIfChanged(sharedPreferences, Constants.WEB_API_IP_PREFERENCE_KEY, WebUtil.removeIPv4LeadingZeros(settings.getIp()), false);
        }
        if (WebUtil.isValidPortNumber(settings.getPort())) {
            updatePreferenceIfChanged(sharedPreferences, Constants.WEB_API_PORT_PREFERENCE_KEY, WebUtil.removeLeadingZeros(settings.getPort()), true);
        }
        updatePreferenceIfChanged(sharedPreferences, Constants.WEB_API_TEAM_PREFERENCE_KEY, settings.getTeam(), true);
    }

    public TextWatcher getIpTextWatcher() {
        return new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                settings.setIp(charSequence.toString());
            }
        };
    }

    public TextWatcher getPortTextWatcher() {
        return new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                settings.setPort(charSequence.toString());
            }
        };
    }

    public TextWatcher getTeamTextWatcher() {
        return new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                settings.setTeam(charSequence.toString());
            }
        };
    }

    private void updatePreferenceIfChanged(@NonNull SharedPreferences sharedPreferences, String key, @NonNull String newValue, boolean emptyAllowed) {
        String currentValue = sharedPreferences.getString(key, "");
        if ((emptyAllowed || !newValue.trim().isEmpty()) && !currentValue.equals(newValue)) {
            sharedPreferences.edit().putString(key, newValue.trim()).apply();
        }
    }

    abstract class SimpleTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    }
}
