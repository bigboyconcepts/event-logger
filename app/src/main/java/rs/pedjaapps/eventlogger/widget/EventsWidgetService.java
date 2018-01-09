package rs.pedjaapps.eventlogger.widget;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import rs.pedjaapps.eventlogger.MainApp;
import rs.pedjaapps.eventlogger.R;
import rs.pedjaapps.eventlogger.constants.EventLevel;
import rs.pedjaapps.eventlogger.constants.EventType;
import rs.pedjaapps.eventlogger.model.Event;
import rs.pedjaapps.eventlogger.utility.SettingsManager;
import rs.pedjaapps.eventlogger.utility.Utility;

/**
 * Created by pedja on 7.7.14. 11.15.
 * This class is part of the WidgetTest
 * Copyright © 2014 ${OWNER}
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class EventsWidgetService extends RemoteViewsService
{
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent)
    {
        return new EventsRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class EventsRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory
{
    private List<Event> mWidgetItems = new ArrayList<Event>();
    private Context mContext;
    private int mAppWidgetId;
    DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());

    public EventsRemoteViewsFactory(Context context, Intent intent)
    {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    public void onCreate()
    {

    }

    public void onDestroy()
    {
        // In onDestroy() you should tear down anything that was setup for your data source,
        // eg. cursors, connections, etc.
        mWidgetItems.clear();
    }

    public int getCount()
    {
        return mWidgetItems.size();
    }

    public RemoteViews getViewAt(int position)
    {
        // position will always range from 0 to getCount() - 1.
        // We construct a remote views item based on our widget item xml file, and set the
        // text based on the position.
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.event_item_layout_widget);

        Event event = mWidgetItems.get(position);
        rv.setTextViewText(R.id.tvDescription, Html.fromHtml(event.getShort_desc()));
        rv.setImageViewResource(R.id.ivLogLevel, EventLevel.getDrawableForLevel(EventLevel.getLevelForInt(event.getLevel())));
        if("passed".equals(SettingsManager.getTimeDisplay()))
        {
            rv.setTextViewText(R.id.tvTimestamp, Utility.getTime(event.getTimestamp().getTime()));
        }
        else
        {
            rv.setTextViewText(R.id.tvTimestamp, format.format(event.getTimestamp()));
        }
        rv.setImageViewResource(R.id.ivType, EventType.getIconForId(event.getType()));

        // Next, we set a fill-intent which will be used to fill-in the pending intent template
        // which is set on the collection view in PostsWidgetProvider.
        Bundle extras = new Bundle();
        extras.putParcelable(EventsWidgetProvider.EXTRA_ITEM, event);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.llItemContainer, fillInIntent);

        // Return the remote views object.
        return rv;
    }

    public RemoteViews getLoadingView()
    {
        // You can create a custom loading view (for instance when getViewAt() is slow.) If you
        // return null here, you will get the default loading view.
        return null;
    }

    public int getViewTypeCount()
    {
        return 1;
    }

    public long getItemId(int position)
    {
        return position;
    }

    public boolean hasStableIds()
    {
        return true;
    }

    public void onDataSetChanged()
    {
        // This is triggered when you call AppWidgetManager notifyAppWidgetViewDataChanged
        // on the collection view corresponding to this factory. You can do heaving lifting in
        // here, synchronously. For example, if you need to process an image, fetch something
        // from the network, etc., it is ok to do it here, synchronously. The widget will remain
        // in its current state while work is being done here, so you don't need to worry about
        // locking up the widget.
        QueryBuilder<Event> queryBuilder = MainApp.getInstance().getDaoSession().getEventDao().queryBuilder();
        if(SettingsManager.isTimeFilterEnabled())
        {
            queryBuilder.where(EventDao.Properties.Timestamp.between(SettingsManager.getFilterTimeFrom(0), SettingsManager.getFilterTimeTo(new Date().getTime())));
        }
        if(SettingsManager.isTypeFilterEnabled())
        {
            List<Integer> enabledTypes = new ArrayList<Integer>();
            String[] filerTypes = SettingsManager.getFilterTypes();
            for(String s : filerTypes)
            {
                enabledTypes.add(EventType.getIntForType(EventType.fromString(s)));
            }
            if(enabledTypes.size() == 1)
            {
                queryBuilder.where(EventDao.Properties.Type.eq(enabledTypes.get(0)));
            }
            else if(enabledTypes.size() > 1)
            {
                WhereCondition cond1 = EventDao.Properties.Type.eq(enabledTypes.get(0));
                WhereCondition cond2 = EventDao.Properties.Type.eq(enabledTypes.get(1));
                List<WhereCondition> orConditions = new ArrayList<WhereCondition>();
                for(int i = 2; i < enabledTypes.size(); i++)
                {
                    orConditions.add(EventDao.Properties.Type.eq(enabledTypes.get(i)));
                }

                queryBuilder.where(queryBuilder.or(cond1, cond2, orConditions.toArray(new WhereCondition[orConditions.size()])));
            }
        }
        if(SettingsManager.isLevelFilterEnabled())
        {
            List<Integer> enabledLevels = new ArrayList<Integer>();
            if(SettingsManager.isFilterLevelErrorEnabled())enabledLevels.add(EventLevel.getIntForLevel(EventLevel.error));
            if(SettingsManager.isFilterLevelWarningEnabled())enabledLevels.add(EventLevel.getIntForLevel(EventLevel.warning));
            if(SettingsManager.isFilterLevelInfoEnabled())enabledLevels.add(EventLevel.getIntForLevel(EventLevel.info));
            if(SettingsManager.isFilterLevelOkEnabled())enabledLevels.add(EventLevel.getIntForLevel(EventLevel.ok));
            if(enabledLevels.size() == 1)
            {
                queryBuilder.where(EventDao.Properties.Level.eq(enabledLevels.get(0)));
            }
            else if(enabledLevels.size() > 1)
            {
                WhereCondition cond1 = EventDao.Properties.Level.eq(enabledLevels.get(0));
                WhereCondition cond2 = EventDao.Properties.Level.eq(enabledLevels.get(1));
                List<WhereCondition> orConditions = new ArrayList<WhereCondition>();
                for(int i = 2; i < enabledLevels.size(); i++)
                {
                    orConditions.add(EventDao.Properties.Level.eq(enabledLevels.get(i)));
                }

                queryBuilder.where(queryBuilder.or(cond1, cond2, orConditions.toArray(new WhereCondition[orConditions.size()])));
            }
        }
        int limit = 15;
        long count = queryBuilder.count();
        queryBuilder.offset((int) (count - limit) - 1);
        queryBuilder.limit(limit);
        mWidgetItems = queryBuilder.list();

    }
}
