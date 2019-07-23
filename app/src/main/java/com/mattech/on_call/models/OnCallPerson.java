package com.mattech.on_call.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.mattech.on_call.JSONAttr;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;

public class OnCallPerson implements Parcelable {
    private static final String ERROR_TAG = OnCallPerson.class.getSimpleName();

    @JSONAttr
    private String name;

    @JSONAttr
    private String mail;

    @JSONAttr
    private String phoneNumber;

    public OnCallPerson() {
    }

    protected OnCallPerson(Parcel in) {
        name = in.readString();
        mail = in.readString();
        phoneNumber = in.readString();
    }

    public static OnCallPerson fromJson(JSONObject json) throws JSONException {
        OnCallPerson onCallPerson = new OnCallPerson();
        for (Field field : OnCallPerson.class.getDeclaredFields()) {
            JSONAttr attr = field.getAnnotation(JSONAttr.class);
            if (attr != null) {
                String jsonFieldName = attr.name().equals("") ? field.getName() : attr.name();
                field.setAccessible(true);
                Object value;
                if (attr.required()) {
                    value = json.get(jsonFieldName);
                } else {
                    value = json.opt(jsonFieldName);
                }
                try {
                    field.set(onCallPerson, value);
                } catch (IllegalAccessException e) {
                    Log.e(ERROR_TAG, "error", e);
                    return null;
                }
            }
        }
        return onCallPerson;
    }

    public static final Creator<OnCallPerson> CREATOR = new Creator<OnCallPerson>() {
        @Override
        public OnCallPerson createFromParcel(Parcel in) {
            return new OnCallPerson(in);
        }

        @Override
        public OnCallPerson[] newArray(int size) {
            return new OnCallPerson[size];
        }
    };

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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(name);
        parcel.writeString(mail);
        parcel.writeString(phoneNumber);
    }
}
