package rs.pedjaapps.eventlogger.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.Date;

import rs.pedjaapps.eventlogger.MainApp;
import rs.pedjaapps.eventlogger.R;
import rs.pedjaapps.eventlogger.constants.EventLevel;
import rs.pedjaapps.eventlogger.constants.EventType;
import rs.pedjaapps.eventlogger.model.Event;

/**
 * Created by pedja on 1/8/18.
 */

public class AllReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        StringBuilder builder = new StringBuilder();
        Bundle bundle = intent.getExtras();
        if (bundle != null)
        {
            for (String key : bundle.keySet())
            {
                Object value = bundle.get(key);
                builder.append(context.getString(R.string.intent_extra_template, key, value, (value != null ? value.getClass().getName() : null))).append("<br><br>");
            }
        }

        Event event = new Event();
        event.setTimestamp(new Date());
        event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
        event.setType(EventType.getIntForType(EventType.general));
        event.setShort_desc(intent.getAction());
        event.setLong_desc(context.getString(R.string.arbitary_event_template, intent.getAction(), builder.toString()));
        EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
        eventDao.insert(event);
        EventReceiver.sendLocalBroadcast(event);
    }
}
