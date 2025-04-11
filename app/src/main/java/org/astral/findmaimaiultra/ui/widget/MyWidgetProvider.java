package org.astral.findmaimaiultra.ui.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import org.astral.findmaimaiultra.R;
import org.astral.findmaimaiultra.ui.MainActivity;

public class MyWidgetProvider extends AppWidgetProvider {
    private static final String ACTION_UPDATE = "org.astral.findmaimaiultra.ui.widget.UPDATE_WIDGET";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_2x2);

            // 设置点击事件
            Intent clickIntent = new Intent(context, MyWidgetProvider.class);
            clickIntent.setAction(ACTION_UPDATE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_UPDATE.equals(intent.getAction())) {
            // 启动 WidgetUpdateService
            Intent serviceIntent = new Intent(context, WidgetUpdateService.class);
            context.startService(serviceIntent);
        }
    }
}
