package com.mattech.on_call;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class ForwardingAppWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            Intent intent = new Intent(context, ForwardingActivity.class);
            intent.putExtra(ForwardingActivity.ACTION_TAG, ForwardingActivity.START_FORWARDING_REQUEST_CODE);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.forwarding_widget);
            views.setOnClickPendingIntent(R.id.update_btn, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
