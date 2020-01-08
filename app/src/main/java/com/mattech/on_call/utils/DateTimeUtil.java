package com.mattech.on_call.utils;

import java.util.Calendar;
import java.util.Date;

public class DateTimeUtil {

    public static boolean isMomentInPast(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long now = calendar.getTimeInMillis();
        calendar.setTime(date);
        long momentToCheck = calendar.getTimeInMillis();
        return momentToCheck <= now;
    }

    public static boolean isMomentToday(Date date) {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentDay = calendar.get(Calendar.DAY_OF_YEAR);
        calendar.setTime(date);
        int momentYear = calendar.get(Calendar.YEAR);
        int momentDay = calendar.get(Calendar.DAY_OF_YEAR);
        return currentDay == momentDay && currentYear == momentYear;
    }
}
