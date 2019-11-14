package com.mattech.on_call;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class ForwardingAppWidgetProvider extends AppWidgetProvider {

    private enum Action {
        SET_REACTOR(0, MainActivity.class), UPDATE_REACTOR(-1, ForwardingActivity.class), STOP_FORWARDING(-2, ForwardingActivity.class);

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

    private PendingIntent getPendingIntentForAction(Context context, Action action) {
        Intent intent = new Intent(context, action.cls);
        switch (action) {
            case UPDATE_REACTOR:
                intent.putExtra(ForwardingActivity.ACTION_TAG, ForwardingActivity.START_FORWARDING_REQUEST_CODE);
                break;
            case STOP_FORWARDING:
                intent.putExtra(ForwardingActivity.ACTION_TAG, ForwardingActivity.STOP_FORWARDING_REQUEST_CODE);
                break;
            default:
                break;
        }
        return PendingIntent.getActivity(context, action.requestCode, intent, 0);
    }
}
