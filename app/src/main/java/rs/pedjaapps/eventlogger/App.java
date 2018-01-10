package rs.pedjaapps.eventlogger;

import android.app.Activity;
import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.crashlytics.android.Crashlytics;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import io.fabric.sdk.android.Fabric;
import rs.pedjaapps.eventlogger.constants.Constants;
import rs.pedjaapps.eventlogger.model.DaoMaster;
import rs.pedjaapps.eventlogger.model.DaoSession;
import rs.pedjaapps.eventlogger.utility.BroadcastActionsManager;
import rs.pedjaapps.eventlogger.utility.DatabaseHelper;
import rs.pedjaapps.eventlogger.utility.SqliteImageLoader;

/**
 * Created by pedja on 10/8/13.
 */
public class App extends Application
{
    private static App app = null;

    private Activity foregroundActivity = null;

    private DaoSession daoSession;

    private DisplayImageOptions defaultDisplayImageOptions;

    public static App getInstance()
    {
        return app;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
		try
		{
        	Fabric.with(this, new Crashlytics());
		}
		catch(Exception e)
		{
			//Crashlytics.logException(e);
		}
        app = this;

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
        defaultDisplayImageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .showImageOnLoading(R.drawable.ic_action_app)
                .showImageOnFail(R.drawable.ic_action_app)
                .showImageForEmptyUri(R.drawable.ic_action_app)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .memoryCacheSizePercentage(13) // default
                .writeDebugLogs()
                .imageDownloader(new SqliteImageLoader(this))
                .defaultDisplayImageOptions(defaultDisplayImageOptions)
                .build();
        ImageLoader.getInstance().init(config);
		
		DaoMaster.OpenHelper helper = new DatabaseHelper(this, Constants.DB_NAME, null);
		SQLiteDatabase db = helper.getWritableDatabase();
		DaoMaster daoMaster = new DaoMaster(db);
		daoSession = daoMaster.newSession();

        new ATImportActions().execute();
    }

    public DaoSession getDaoSession()
    {
        return daoSession;
    }

    public Activity getForegroundActivity()
    {
        return foregroundActivity;
    }

    public void setForegroundActivity(Activity foregroundActivity)
    {
        this.foregroundActivity = foregroundActivity;
    }

    public DisplayImageOptions getDefaultDisplayImageOptions()
    {
        return defaultDisplayImageOptions;
    }

    public static class ATImportActions extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {

            BroadcastActionsManager.getInstance().importBundledActions();
            return null;
        }
    }
}
