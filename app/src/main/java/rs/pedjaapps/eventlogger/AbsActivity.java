package rs.pedjaapps.eventlogger;

import android.support.v7.app.AppCompatActivity;


/**
 * Created by pedja on 12.4.14..
 */
public abstract class AbsActivity extends AppCompatActivity
{
    public DaoSession getDaoSession()
    {
        return MainApp.getInstance().getDaoSession();
    }
}
