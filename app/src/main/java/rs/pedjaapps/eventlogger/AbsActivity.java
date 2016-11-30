package rs.pedjaapps.eventlogger;

import android.support.v7.app.ActionBarActivity;

import rs.pedjaapps.eventlogger.model.DaoSession;


/**
 * Created by pedja on 12.4.14..
 */
public abstract class AbsActivity extends ActionBarActivity
{
    public DaoSession getDaoSession()
    {
        return MainApp.getInstance().getDaoSession();
    }
}
