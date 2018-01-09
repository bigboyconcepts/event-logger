package rs.pedjaapps.eventlogger.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import rs.pedjaapps.eventlogger.MainApp;
import rs.pedjaapps.eventlogger.model.Event;
import rs.pedjaapps.eventlogger.service.EventService;

/**
 * Created by pedja on 20.4.14..
 */
public class InsertEventReceiver extends BroadcastReceiver
{
    public static final String ACTION_INSERT_EVENT = "rs.pedjaapps.eventlogger.INSERT_EVENT";
    public static final String EXTRA_EVENT = "event";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(intent == null || intent.getAction() == null || intent.getExtras() == null)
        {
            return;
        }
        context.startService(new Intent(context, EventService.class));
        Bundle extras = intent.getExtras();
        if(intent.getAction().equals(ACTION_INSERT_EVENT))
        {
            Event event = extras.getParcelable(EXTRA_EVENT);
            EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
            eventDao.insert(event);
            EventReceiver.sendLocalBroadcast(event, context);
        }
    }
}
