package rs.pedjaapps.eventlogger;

import android.content.Intent;
import android.os.Bundle;

import rs.pedjaapps.eventlogger.service.EventService;

/**
 * Created by pedja on 14.4.14..
 */
public class ServiceRestartActivity extends AbsActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //moveTaskToBack(true);
        super.onCreate(savedInstanceState);
        startService(new Intent(this, EventService.class));
        finish();
    }
}
