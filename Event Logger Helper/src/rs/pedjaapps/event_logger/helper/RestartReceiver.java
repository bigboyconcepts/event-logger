package rs.pedjaapps.event_logger.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by pedja on 14.4.14..
 */
public class RestartReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(intent == null || intent.getAction() == null)return;
        if(intent.getAction().equals(Intent.ACTION_PACKAGE_RESTARTED) || intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED))
        {
            if(intent.getData() != null && intent.getData().toString().contains("rs.pedjaapps.eventlogger"))
            {
                Intent serviceIntent = new Intent();
                serviceIntent.setClassName("rs.pedjaapps.eventlogger", "rs.pedjaapps.eventlogger.service.EventService");
                context.startService(serviceIntent);
                System.out.println(intent.getData().toString());
            }
        }
    }
}
