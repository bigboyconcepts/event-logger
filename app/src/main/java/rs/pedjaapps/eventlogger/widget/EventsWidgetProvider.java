package rs.pedjaapps.eventlogger.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import rs.pedjaapps.eventlogger.EventDetailsActivity;
import rs.pedjaapps.eventlogger.MainActivity;
import rs.pedjaapps.eventlogger.R;
import rs.pedjaapps.eventlogger.model.Event;

/**
 * Created by pedja on 7.7.14. 11.08.
 * This class is part of the WidgetTest
 * Copyright © 2014 ${OWNER}
 */
public class EventsWidgetProvider extends AppWidgetProvider
{

    public static final String INTENT_ACTION_EVENT_DETAILS = "rs.pedjaapps.eventlogger.INTENT_ACTION_EVENT_DETAILS";
    public static final String EXTRA_ITEM = "rs.pedjaapps.eventlogger.EXTRA_ITEM";
    public static final String INTENT_ACTION_REFRESH_WIDGET = "rs.pedjaapps.eventlogger.INTENT_ACTION_REFRESH_WIDGET";

    @Override
    public void onDeleted(Context context, int[] appWidgetIds)
    {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context)
    {
        super.onDisabled(context);
    }

    @Override
    public void onEnabled(Context context)
    {
        super.onEnabled(context);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onReceive(Context context, Intent intent)
    {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        if (intent.getAction().equals(INTENT_ACTION_EVENT_DETAILS))
        {
            Event event = intent.getParcelableExtra(EXTRA_ITEM);
            if(event != null)
            {
                context.startActivity(new Intent(context, EventDetailsActivity.class).putExtra(EventDetailsActivity.EXTRA_EVENT, event));
            }
        }
        else if(intent.getAction().equals(INTENT_ACTION_REFRESH_WIDGET))
        {
            //ComponentName cn = new ComponentName(context, EventsWidgetProvider.class);
            //RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.events_widget_layout);
            //mgr.updateAppWidget(cn, rv);
            int appWidgetIds[] = mgr.getAppWidgetIds(new ComponentName(context, EventsWidgetProvider.class));
            mgr.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.list);
        }
        super.onReceive(context, intent);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        // update each of the widgets with the remote adapter
        for (int appWidgetId : appWidgetIds)
        {
            // Here we setup the intent which points to the PostsWidgetService which will
            // provide the views for this collection.
            Intent intent = new Intent(context, EventsWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

            // When intents are compared, the extras are ignored, so we need to embed the extras
            // into the data so that the extras will not be ignored.
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.events_widget_layout);
            rv.setRemoteAdapter(R.id.list, intent);

            // The empty view is displayed when the collection has no items. It should be a sibling
            // of the collection view.
            rv.setEmptyView(R.id.list, R.id.tvEmptyView);

            // Here we setup the a pending intent template. Individuals items of a collection
            // cannot setup their own pending intents, instead, the collection as a whole can
            // setup a pending intent template, and the individual items can set a fillInIntent
            // to create unique before on an item to item basis.
            Intent toastIntent = new Intent(context, EventsWidgetProvider.class);
            toastIntent.setAction(EventsWidgetProvider.INTENT_ACTION_EVENT_DETAILS);
            toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, toastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.list, toastPendingIntent);

            Intent refreshIntent = new Intent(context, EventsWidgetProvider.class);
            refreshIntent.setAction(INTENT_ACTION_REFRESH_WIDGET);
            refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, refreshIntent, 0);
            rv.setOnClickPendingIntent(R.id.ivRefresh, pendingIntent);

            Intent appIntent = new Intent(context, MainActivity.class);
            PendingIntent appPendingIntent = PendingIntent.getActivity(context, 0, appIntent, 0);
            rv.setOnClickPendingIntent(R.id.widgetContainer, appPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
