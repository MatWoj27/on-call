package com.mattech.on_call;

import com.mattech.on_call.utils.NotificationUtil;

public class Constants {
    public static final String WEB_API_PREFERENCES_NAME = "web-api-preferences";
    public static final String WEB_API_IP_PREFERENCE_KEY = "web-api-ip";
    public static final String WEB_API_PORT_PREFERENCE_KEY = "web-api-port";
    public static final String WEB_API_TEAM_PREFERENCE_KEY = "team-name";

    public static final int FORWARDING_NOTIFICATION_ID = 1;
    public static final String FORWARDING_NOTIFICATION_CHANNEL_ID = "forwarding-notification";

    public static final NotificationUtil.NotificationChannelInfo FORWARDING_CHANNEL_INFO = new NotificationUtil.NotificationChannelInfo(
            FORWARDING_NOTIFICATION_CHANNEL_ID,
            R.string.forwarding_notification_channel_name,
            R.string.forwarding_notification_channel_description
    );
}
