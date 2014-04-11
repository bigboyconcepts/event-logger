package rs.pedjaapps.eventlogger.receiver;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import rs.pedjaapps.eventlogger.MainActivity;
import rs.pedjaapps.eventlogger.MainApp;
import rs.pedjaapps.eventlogger.model.Event;

/**
 * Created by pedja on 11.4.14..
 */
public abstract class AbsEventReceiver extends BroadcastReceiver
{
    public static void sendLocalBroadcast(Event event)
    {
        Intent intent = new Intent();
        intent.setAction(MainActivity.ACTION_ADD_EVENT);
        intent.putExtra(MainActivity.EXTRA_EVENT, event);
        LocalBroadcastManager.getInstance(MainApp.getContext()).sendBroadcast(intent);
    }
}
