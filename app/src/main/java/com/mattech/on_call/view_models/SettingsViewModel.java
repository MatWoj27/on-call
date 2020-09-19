package com.mattech.on_call.view_models;

import android.arch.lifecycle.ViewModel;
import android.text.Editable;
import android.text.TextWatcher;

import com.mattech.on_call.models.WebApiSettings;

public class SettingsViewModel extends ViewModel {
    public WebApiSettings settings;

    public void setSettings(WebApiSettings settings) {
        if (this.settings == null) {
            this.settings = settings;
        }
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

    abstract class SimpleTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    }
}
