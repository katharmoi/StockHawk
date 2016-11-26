package com.sam_chordas.android.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;
import android.support.v4.app.TaskStackBuilder;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.ui.DetailsActivity;


/**
 * Created by uyan on 07/11/16.
 */
public class AppWidgetProvider extends android.appwidget.AppWidgetProvider {
    public static final String ACTION_UPDATE = "com.kadirkertis.widgetexample.ACTION_UPDATE";
    public static final String EXTRA_STRING = "com.kadirkertis.widgetexample.EXTRA_STRING";
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            //Intent for the service
            Intent intent = new Intent(context, MyRemoteViewsWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            //Main layout
            RemoteViews rv = new RemoteViews(context.getPackageName(),
                    R.layout.appwidget);


            rv.setRemoteAdapter(R.id.widgetCollectionList, intent);


            //Open Details activity when item click
            Intent onItemClick = new Intent(context, DetailsActivity.class);
            PendingIntent onClickPendingIntent = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(onItemClick)
                    .getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);

            rv.setOnClickPendingIntent(R.id.widgetCollectionList,onClickPendingIntent);
            appWidgetManager.updateAppWidget(widgetId, rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("fetched")){
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,getClass()));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds,R.id.widgetCollectionList);
        }
        super.onReceive(context, intent);
    }

}
