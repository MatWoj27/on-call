package com.mattech.on_call.receivers;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RemoteViews;

import com.mattech.on_call.events.ForwardingEvent;
import com.mattech.on_call.R;
import com.mattech.on_call.repositories.ReactorRepository;
import com.mattech.on_call.activities.ForwardingActivity;
import com.mattech.on_call.activities.MainActivity;

public class ForwardingAppWidgetProvider extends AppWidgetProvider {
    public static final String REACTOR_NAME_TAG = "name";
    public static final String REACTOR_PHONE_NUMBER_TAG = "phoneNumber";

    private enum Action {
        SET_REACTOR(0, MainActivity.class),
        UPDATE_REACTOR(-1, ForwardingActivity.class),
        STOP_FORWARDING(-2, ForwardingActivity.class),
        START_FORWARDING(-3, ForwardingActivity.class);

        int requestCode;
        Class cls;

        Action(int requestCode, Class cls) {
            this.requestCode = requestCode;
            this.cls = cls;
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.forwarding_widget);
            views.setOnClickPendingIntent(R.id.update_btn, getPendingIntentForAction(context, Action.UPDATE_REACTOR));
            views.setOnClickPendingIntent(R.id.reactor_info_container, getPendingIntentForAction(context, Action.SET_REACTOR));
            views.setOnClickPendingIntent(R.id.forward_btn, getPendingIntentForAction(context, Action.STOP_FORWARDING));
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case ReactorRepository.REACTOR_CHANGED:
                    displayReactor(context, intent.getStringExtra(REACTOR_NAME_TAG), intent.getStringExtra(REACTOR_PHONE_NUMBER_TAG));
                    break;
                case ForwardingEvent.FORWARDING_STARTED:
                    setForwardingButtonFunctionality(context, R.string.stop_forwarding, Action.STOP_FORWARDING);
                    break;
                case ForwardingEvent.FORWARDING_STOPPED:
                    setForwardingButtonFunctionality(context, R.string.start_forwarding, Action.START_FORWARDING);
                    break;
                default:
                    super.onReceive(context, intent);
                    break;
            }
        } else {
            super.onReceive(context, intent);
        }
    }

    private void displayReactor(@NonNull Context context, String name, String phoneNumber) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.forwarding_widget);
        views.setInt(R.id.set_reactor, "setVisibility", View.GONE);
        views.setInt(R.id.reactor_info, "setVisibility", View.VISIBLE);
        views.setTextViewText(R.id.reactor_name, name);
        views.setTextViewText(R.id.reactor_phone_number, phoneNumber);
        updateRemoteViews(context, views);
    }

    private void setForwardingButtonFunctionality(@NonNull Context context, int buttonTextId, Action buttonAction) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.forwarding_widget);
        views.setTextViewText(R.id.forward_btn, context.getString(buttonTextId));
        views.setOnClickPendingIntent(R.id.forward_btn, getPendingIntentForAction(context, buttonAction));
        updateRemoteViews(context, views);
    }

    private void updateRemoteViews(Context context, RemoteViews views) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(new ComponentName(context.getPackageName(), ForwardingAppWidgetProvider.class.getName()), views);
    }

    private PendingIntent getPendingIntentForAction(Context context, @NonNull Action action) {
        Intent intent = new Intent(context, action.cls);
        switch (action) {
            case UPDATE_REACTOR:
                intent.putExtra(ForwardingActivity.ACTION_TAG, ForwardingActivity.UPDATE_REACTOR_AND_START_FORWARDING_REQUEST_CODE);
                break;
            case STOP_FORWARDING:
                intent.putExtra(ForwardingActivity.ACTION_TAG, ForwardingActivity.STOP_FORWARDING_REQUEST_CODE);
                break;
            case START_FORWARDING:
                intent.putExtra(ForwardingActivity.ACTION_TAG, ForwardingActivity.START_FORWARDING_REQUEST_CODE);
                break;
            default:
                break;
        }
        return PendingIntent.getActivity(context, action.requestCode, intent, 0);
    }
}
