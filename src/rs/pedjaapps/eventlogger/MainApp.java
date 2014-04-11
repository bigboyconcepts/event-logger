package rs.pedjaapps.eventlogger;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.crashlytics.android.Crashlytics;

import rs.pedjaapps.eventlogger.constants.Constants;
import rs.pedjaapps.eventlogger.model.DaoMaster;
import rs.pedjaapps.eventlogger.model.DaoSession;

/**
 * Created by pedja on 10/8/13.
 */
public class MainApp extends Application
{
    private static  MainApp mainApp = null;
    private static Context context;

    private Activity foregroundActivity = null;

    private DaoSession daoSession;

    public synchronized static MainApp getInstance()
    {
        if(mainApp == null)
        {
            mainApp = new MainApp();
        }
        return mainApp;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Crashlytics.start(this);
        context = this.getApplicationContext();
        mainApp = this;

        /*if (AppData.LOGGING)
        {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }*/
    }

    public DaoSession getDaoSession()
    {
        if (daoSession == null)
        {
            DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, Constants.DB_NAME, null);
            SQLiteDatabase db = helper.getWritableDatabase();
            DaoMaster daoMaster = new DaoMaster(db);
            daoSession = daoMaster.newSession();
        }
        return daoSession;
    }

    public static Context getContext()
    {
        return context;
    }

    public Activity getForegroundActivity()
    {
        return foregroundActivity;
    }

    public void setForegroundActivity(Activity foregroundActivity)
    {
        this.foregroundActivity = foregroundActivity;
    }
}
