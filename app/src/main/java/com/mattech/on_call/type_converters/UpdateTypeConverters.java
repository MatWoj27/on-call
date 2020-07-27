package com.mattech.on_call.type_converters;

import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class UpdateTypeConverters {
    private static Gson gson = new Gson();

    @TypeConverter
    public static boolean[] stringToBooleanArray(String data) {
        if (data == null) {
            return null;
        }
        Type arrayType = new TypeToken<boolean[]>() {
        }.getType();
        return gson.fromJson(data, arrayType);
    }

    @TypeConverter
    public static String booleanArrayToString(boolean[] array) {
        return gson.toJson(array);
    }
}
