package rs.pedjaapps.eventlogger.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Date;

import rs.pedjaapps.eventlogger.MainApp;
import rs.pedjaapps.eventlogger.constants.EventLevel;
import rs.pedjaapps.eventlogger.constants.EventType;
import rs.pedjaapps.eventlogger.model.Event;
import rs.pedjaapps.eventlogger.model.EventDao;

/**
 * Created by pedja on 1/8/18.
 */

public class AllReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Event event = new Event();
        event.setTimestamp(new Date());
        event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
        event.setType(EventType.getIntForType(EventType.general));
        event.setShort_desc(intent.getAction());
        event.setLong_desc(intent.getAction());//TODO params?
        EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
        eventDao.insert(event);
        EventReceiver.sendLocalBroadcast(event);
    }
}
