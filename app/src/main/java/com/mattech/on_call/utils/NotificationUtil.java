package com.mattech.on_call.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

public class NotificationUtil {
    public static final String DEFAULT_NOTIFICATION_CHANNEL_ID = "app_default_id";

    public static class NotificationChannelInfo {
        private String id;
        private int name_id;
        private int description_id;

        public NotificationChannelInfo(String id, int name_id, int description_id) {
            this.id = id;
            this.name_id = name_id;
            this.description_id = description_id;
        }
    }

    private NotificationUtil() {
    }

    @NonNull
    public static Notification.Builder getNotificationBuilder(Context context, @Nullable NotificationChannelInfo notificationChannelInfo) {
        Notification.Builder builder = new Notification.Builder(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationChannelInfo != null) {
                createNotificationChannel(context, notificationChannelInfo.id, context.getString(notificationChannelInfo.name_id), context.getString(notificationChannelInfo.description_id));
                builder.setChannelId(notificationChannelInfo.id);
            } else {
                createNotificationChannel(context, DEFAULT_NOTIFICATION_CHANNEL_ID, "", "");
                builder.setChannelId(DEFAULT_NOTIFICATION_CHANNEL_ID);
            }
        }
        return builder;
    }

    public static void cancelActiveForwardingResultNotification(@NonNull Context context, int id) {
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        for (StatusBarNotification notification : notificationManager.getActiveNotifications()) {
            if (notification.getId() == id) {
                notificationManager.cancel(id);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void createNotificationChannel(@NonNull Context context, String id, String name, String description) {
        NotificationChannel channel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(description);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}
