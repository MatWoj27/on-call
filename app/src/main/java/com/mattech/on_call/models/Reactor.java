package com.mattech.on_call.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.mattech.on_call.annotations.JSONAttr;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;

@Entity(tableName = "reactors")
public class Reactor {
    @Ignore
    private static final String ERROR_TAG = Reactor.class.getSimpleName();

    @JSONAttr
    private String name;

    @JSONAttr
    private String mail;

    @JSONAttr
    @PrimaryKey
    @NonNull
    private String phoneNumber = "";

    @Nullable
    public static Reactor fromJson(JSONObject json) throws JSONException {
        Reactor reactor = new Reactor();
        for (Field field : Reactor.class.getDeclaredFields()) {
            JSONAttr attr = field.getAnnotation(JSONAttr.class);
            if (attr != null) {
                String jsonFieldName = attr.name().equals("") ? field.getName() : attr.name();
                field.setAccessible(true);
                Object value = attr.required() ? json.get(jsonFieldName) : json.opt(jsonFieldName);
                try {
                    field.set(reactor, value);
                } catch (IllegalAccessException e) {
                    Log.e(ERROR_TAG, "Could not set " + field.getName() + " field using reflection because it is either inaccessible or final", e);
                    return null;
                }
            }
        }
        return reactor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    @NonNull
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(@NonNull String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
