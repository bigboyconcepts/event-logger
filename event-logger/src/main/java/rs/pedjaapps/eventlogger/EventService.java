package rs.pedjaapps.eventlogger;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by pedja on 11.4.14..
 */
public class EventService extends Service
{
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}
