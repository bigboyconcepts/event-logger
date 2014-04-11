package rs.pedjaapps.eventlogger.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.telephony.TelephonyManager;

import java.util.Date;
import java.util.List;

import rs.pedjaapps.eventlogger.MainApp;
import rs.pedjaapps.eventlogger.R;
import rs.pedjaapps.eventlogger.constants.EventLevel;
import rs.pedjaapps.eventlogger.constants.EventType;
import rs.pedjaapps.eventlogger.model.Event;
import rs.pedjaapps.eventlogger.model.EventDao;
import rs.pedjaapps.eventlogger.receiver.AbsEventReceiver;
import rs.pedjaapps.eventlogger.receiver.ManualRegisterReceiver;

/**
 * Created by pedja on 11.4.14..
 */
public class EventService extends Service
{
    private ManualRegisterReceiver manualRegisterReceiver;
    private String lastActiveApp = "";
    private long checkInterval = 1000l;
    Handler handler;
    private AppLaunchChecker appLaunchChecker = new AppLaunchChecker();

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        IntentFilter intentFilter = new IntentFilter();
        //screen
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);

        //misc
        intentFilter.addAction(Intent.ACTION_REBOOT);
        intentFilter.addAction(Intent.ACTION_SHUTDOWN);
        intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);

        //package
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_DATA_CLEARED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_RESTARTED);

        //configuration
        intentFilter.addAction(Intent.ACTION_LOCALE_CHANGED);
        intentFilter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);

        //media
        intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        intentFilter.addAction(Intent.ACTION_MEDIA_CHECKING);
        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_NOFS);
        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);

        intentFilter.addAction(Intent.ACTION_DEVICE_STORAGE_OK);
        intentFilter.addAction(Intent.ACTION_DEVICE_STORAGE_LOW);

        //power
        intentFilter.addAction(Intent.ACTION_BATTERY_LOW);
        intentFilter.addAction(Intent.ACTION_BATTERY_OKAY);
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);

        manualRegisterReceiver = new ManualRegisterReceiver();
        registerReceiver(manualRegisterReceiver, intentFilter);

        HandlerThread thread = new HandlerThread("AppLaunchCheckerThread");
        thread.start();
        handler = new Handler(thread.getLooper());
        handler.postDelayed(appLaunchChecker, checkInterval);

        super.onCreate();
    }

    private class AppLaunchChecker implements Runnable
    {
        @Override
        public void run()
        {
            List<ActivityManager.RunningTaskInfo> tasks = ((ActivityManager)getSystemService(Context.ACTIVITY_SERVICE)).getRunningTasks(1);
            if(tasks != null && !tasks.isEmpty())
            {
                ActivityManager.RunningTaskInfo task = tasks.get(0);
                if(task != null)
                {
                    ComponentName topActivity = task.topActivity;
                    if (topActivity != null)
                    {
                        String pn = topActivity.getPackageName();
                        if(pn != null && !lastActiveApp.equals(pn))
                        {
                            lastActiveApp = pn;
                            PackageManager pm = getPackageManager();
                            String appName;
                            try
                            {
                                ApplicationInfo ai = pm.getApplicationInfo(pn, 0);
                                appName = (String) pm.getApplicationLabel(ai);
                            }
                            catch (Exception e)
                            {
                                appName = "[" + getString(R.string.unknown).toUpperCase() + "]";
                            }
                            Event event = new Event();
                            event.setTimestamp(new Date());
                            event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
                            event.setType(EventType.getIntForType(EventType.app));
                            event.setShort_desc(getString(R.string.app_started, appName));
                            event.setLong_desc(getString(R.string.app_started_desc, appName, pn));
                            EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
                            eventDao.insert(event);
                            AbsEventReceiver.sendLocalBroadcast(event);
                        }
                    }
                }
            }
            handler.postDelayed(appLaunchChecker, checkInterval);
        }
    }

    @Override
    public void onDestroy()
    {
        unregisterReceiver(manualRegisterReceiver);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return Service.START_STICKY;
    }
}
