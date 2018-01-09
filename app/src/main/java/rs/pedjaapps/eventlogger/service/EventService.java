package rs.pedjaapps.eventlogger.service;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rs.pedjaapps.eventlogger.BuildConfig;
import rs.pedjaapps.eventlogger.MainApp;
import rs.pedjaapps.eventlogger.R;
import rs.pedjaapps.eventlogger.ServiceRestartActivity;
import rs.pedjaapps.eventlogger.constants.Constants;
import rs.pedjaapps.eventlogger.constants.EventLevel;
import rs.pedjaapps.eventlogger.constants.EventType;
import rs.pedjaapps.eventlogger.model.Event;
import rs.pedjaapps.eventlogger.model.Icon;
import rs.pedjaapps.eventlogger.receiver.AllReceiver;
import rs.pedjaapps.eventlogger.receiver.EventReceiver;
import rs.pedjaapps.eventlogger.utility.SettingsManager;
import rs.pedjaapps.eventlogger.utility.Utility;

/**
 * Created by pedja on 11.4.14..
 */
public class EventService extends Service
{
    /*2:cpu:/apps1:cpuacct:/uid_10001/pid_1249
2:cpu:/1:cpuacct:/
2:cpu:/apps1:cpuacct:/
2:cpu:/apps/bg_non_interactive1:cpuacct:/uid_10039/pid_861*/
    private static final Pattern PROC_CGROUP_PATTERN = Pattern.compile(".*/uid_(\\d+)/pid_(\\d+)");
    private static final Matcher CGROUP_MATCHER = PROC_CGROUP_PATTERN.matcher("");
    /**
     * first app user
     */
    public static final int AID_APP = 10000;

    /**
     * offset for uid ranges for each user
     */
    public static final int AID_USER = 100000;

    private EventReceiver manualRegisterReceiver;
    private AllReceiver allReceiver;
    private String lastActiveApp = "";
    Handler handler;
    private Runnable appLaunchChecker;

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        Log.d(Constants.LOG_TAG, "EventService :: onCreate");
        IntentFilter intentFilter = new IntentFilter();
        //screen
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);

        //misc
        intentFilter.addAction(Intent.ACTION_REBOOT);
        intentFilter.addAction(Intent.ACTION_SHUTDOWN);
        intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        intentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);

        //package
        /*intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_DATA_CLEARED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_RESTARTED);*/

        //configuration
        intentFilter.addAction(Intent.ACTION_LOCALE_CHANGED);
        intentFilter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);

        //date/time
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        intentFilter.addAction(Intent.ACTION_DATE_CHANGED);

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
        intentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");

        //power
        intentFilter.addAction(Intent.ACTION_BATTERY_LOW);
        intentFilter.addAction(Intent.ACTION_BATTERY_OKAY);
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);

        //net
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);


        intentFilter.addAction(EventReceiver.INTENT_ACTION_APP_LAUNCHED);

        manualRegisterReceiver = new EventReceiver();
        registerReceiver(manualRegisterReceiver, intentFilter);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            appLaunchChecker = new AppLaunchCheckerLegacy();
        }
        else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1)
        {
            appLaunchChecker = new AppLaunchCheckerLP();
        }
        else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
        {
            appLaunchChecker = new AppLaunchCheckerLPMR1();
        }
        else
        {
            appLaunchChecker = new AppLaunchCheckerN();
        }

        HandlerThread thread = new HandlerThread("AppLaunchCheckerThread");
        thread.start();
        handler = new Handler(thread.getLooper());
        handler.postDelayed(appLaunchChecker, SettingsManager.getActiveAppCheckInterval());

        try
        {
            String[] actions = Utility.readRawFile(R.raw.broadcast_actions).split("\n");
            intentFilter = new IntentFilter();

            for(String action : actions)
                intentFilter.addAction(action.trim());

            allReceiver = new AllReceiver();
            registerReceiver(allReceiver, intentFilter);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        try {
            Process p = Runtime.getRuntime().exec("dumpsys activity b");
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));

            Set<String> actionsDumpsys = new HashSet<>();

            String line;
            while ((line = r.readLine()) != null) {

                if(line.startsWith("      Action:")) {
                line = line.substring(15, line.length() - 1);
                    actionsDumpsys.add(line);
                }
            }
            System.out.println("dumpsys" + Arrays.toString(actionsDumpsys.toArray()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onCreate();
    }

    private class AppLaunchCheckerLegacy implements Runnable
    {
        @Override
        public void run()
        {
            List<ActivityManager.RunningTaskInfo> tasks = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE)).getRunningTasks(1);
            if (tasks != null && !tasks.isEmpty())
            {
                ActivityManager.RunningTaskInfo task = tasks.get(0);
                if (task != null)
                {
                    ComponentName topActivity = task.topActivity;
                    if (topActivity != null)
                    {
                        String pn = topActivity.getPackageName();
                        if (pn != null && !lastActiveApp.equals(pn))
                        {
                            lastActiveApp = pn;
                            String appName = Utility.getNameForPackage(EventService.this, pn);

                            Event event = new Event();
                            event.setTimestamp(new Date());
                            event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
                            event.setType(EventType.getIntForType(EventType.app));
                            event.setShort_desc(getString(R.string.app_started, appName));
                            event.setLong_desc(getString(R.string.app_started_desc, appName, pn));
                            Icon icon = new Icon();
                            icon.setIcon(Utility.getApplicationIcon(EventService.this, pn));
                            long iconId = MainApp.getInstance().getDaoSession().getIconDao().insert(icon);
                            event.setIcon_id(iconId);
                            EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
                            eventDao.insert(event);
                            EventReceiver.sendLocalBroadcast(event);
                        }
                    }
                }
            }
            handler.postDelayed(appLaunchChecker, SettingsManager.getActiveAppCheckInterval());
        }
    }

    private class AppLaunchCheckerLP implements Runnable
    {
        @Override
        public void run()
        {
            List<ActivityManager.RunningAppProcessInfo> processes = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE)).getRunningAppProcesses();
            if (processes != null)
            {
                List<String> packages = new ArrayList<>();
                for (ActivityManager.RunningAppProcessInfo pi : processes)
                {
                    boolean hasActivities = true;
                    try
                    {
                        Field flagsField = pi.getClass().getDeclaredField("flags");
                        int flags = (int) flagsField.get(pi);
                        hasActivities = (flags & 1 << 2) == 1 << 2;
                    }
                    catch (Exception e)
                    {
                        if (BuildConfig.DEBUG) e.printStackTrace();
                    }
                    if (pi.processName.contains("com.android.systemui") || pi.processName.contains("android.process.acore"))
                    {
                        continue;
                    }
                    if (hasActivities && pi.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)
                    {
                        packages.add(pi.processName);
                    }
                }
                String packageName = packages.isEmpty() ? null : packages.get(0);
                if (packageName != null && !lastActiveApp.equals(packageName))
                {
                    lastActiveApp = packageName;
                    String appName = Utility.getNameForPackage(EventService.this, packageName);

                    Event event = new Event();
                    event.setTimestamp(new Date());
                    event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
                    event.setType(EventType.getIntForType(EventType.app));
                    event.setShort_desc(getString(R.string.app_started, appName));
                    event.setLong_desc(getString(R.string.app_started_desc, appName, packageName));
                    Icon icon = new Icon();
                    icon.setIcon(Utility.getApplicationIcon(EventService.this, packageName));
                    long iconId = MainApp.getInstance().getDaoSession().getIconDao().insert(icon);
                    event.setIcon_id(iconId);
                    EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
                    eventDao.insert(event);
                    EventReceiver.sendLocalBroadcast(event);
                }
            }
            handler.postDelayed(appLaunchChecker, SettingsManager.getActiveAppCheckInterval());
        }
    }

    private class AppLaunchCheckerLPMR1 implements Runnable
    {
        @Override
        public void run()
        {
            String packageName = getForegroundAppByParsingProc();
            System.out.println(packageName);
            if (packageName != null && !lastActiveApp.equals(packageName))
            {
                lastActiveApp = packageName;
                String appName = Utility.getNameForPackage(EventService.this, packageName);

                Event event = new Event();
                event.setTimestamp(new Date());
                event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
                event.setType(EventType.getIntForType(EventType.app));
                event.setShort_desc(getString(R.string.app_started, appName));
                event.setLong_desc(getString(R.string.app_started_desc, appName, packageName));
                Icon icon = new Icon();
                icon.setIcon(Utility.getApplicationIcon(EventService.this, packageName));
                long iconId = MainApp.getInstance().getDaoSession().getIconDao().insert(icon);
                event.setIcon_id(iconId);
                EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
                eventDao.insert(event);
                EventReceiver.sendLocalBroadcast(event);
            }
            handler.postDelayed(appLaunchChecker, SettingsManager.getActiveAppCheckInterval());
        }
    }

    private class AppLaunchCheckerN implements Runnable
    {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
        @Override
        public void run()
        {
            long now = System.currentTimeMillis();

            UsageStatsManager usageStatsManager = (UsageStatsManager) EventService.this.getSystemService(Context.USAGE_STATS_SERVICE);
            List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, now - SettingsManager.getActiveAppCheckInterval(), now);

            if(!usageStatsList.isEmpty()) {
                Collections.sort(usageStatsList, new Comparator<UsageStats>() {
                    @Override
                    public int compare(UsageStats o1, UsageStats o2) {
                        return Long.compare(o2.getLastTimeUsed(), o1.getLastTimeUsed());
                    }
                });

                UsageStats stats = usageStatsList.get(0);
                if (!stats.getPackageName().equals(lastActiveApp))
                {
                    lastActiveApp = stats.getPackageName();

                    String appName = Utility.getNameForPackage(EventService.this, lastActiveApp);

                    Event event = new Event();
                    event.setTimestamp(new Date());
                    event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
                    event.setType(EventType.getIntForType(EventType.app));
                    event.setShort_desc(getString(R.string.app_started, appName));
                    event.setLong_desc(getString(R.string.app_started_desc, appName, lastActiveApp));
                    Icon icon = new Icon();
                    icon.setIcon(Utility.getApplicationIcon(EventService.this, lastActiveApp));
                    long iconId = MainApp.getInstance().getDaoSession().getIconDao().insert(icon);
                    event.setIcon_id(iconId);
                    EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
                    eventDao.insert(event);
                    EventReceiver.sendLocalBroadcast(event);
                }
            }

            handler.postDelayed(appLaunchChecker, SettingsManager.getActiveAppCheckInterval());
        }
    }

    public static String getForegroundAppByParsingProc()
    {
        File[] files = new File("/proc").listFiles();
        int lowestOomScore = Integer.MAX_VALUE;
        String foregroundProcess = null;

        for (File file : files)
        {
            if (!file.isDirectory())
            {
                continue;
            }

            int pid;
            try
            {
                pid = Integer.parseInt(file.getName());
            }
            catch (NumberFormatException e)
            {
                continue;
            }

            try
            {
                String cgroup = Utility.readFileToString(String.format("/proc/%d/cgroup", pid));

                if (cgroup.contains("bg_non_interactive"))
                {
                    // background policy
                    continue;
                }

                CGROUP_MATCHER.reset(cgroup);
                if(!CGROUP_MATCHER.find())
                {
                    continue;
                }

                String cmdline = Utility.readFileToString(String.format("/proc/%d/cmdline", pid));

                if (cmdline.contains("com.android.systemui") || cmdline.contains("android.process.acore"))
                {
                    continue;
                }

                int uid = Integer.parseInt(CGROUP_MATCHER.group(1));
                if (uid >= 1000 && uid <= 1038)
                {
                    // system process
                    continue;
                }

                int appId = uid - AID_APP;
                int userId = 0;
                // loop until we get the correct user id.
                // 100000 is the offset for each user.
                while (appId > AID_USER)
                {
                    appId -= AID_USER;
                    userId++;
                }

                if (appId < 0)
                {
                    continue;
                }

                // u{user_id}_a{app_id} is used on API 17+ for multiple user account support.
                // String uidName = String.format("u%d_a%d", userId, appId);

                File oomScoreAdj = new File(String.format("/proc/%d/oom_score_adj", pid));
                if (oomScoreAdj.canRead())
                {
                    int oomAdj = Integer.parseInt(Utility.readFileToString(oomScoreAdj.getAbsolutePath()));
                    if (oomAdj != 0)
                    {
                        continue;
                    }
                }

                int oomscore = Integer.parseInt(Utility.readFileToString(String.format("/proc/%d/oom_score", pid)));
                if (oomscore < lowestOomScore)
                {
                    lowestOomScore = oomscore;
                    foregroundProcess = cmdline;
                }

            }
            catch (IOException e)
            {
                if (BuildConfig.DEBUG) e.printStackTrace();
            }
        }

        if(foregroundProcess != null)
            foregroundProcess = foregroundProcess.trim();

        if(foregroundProcess != null && foregroundProcess.contains(":"))
        {
            foregroundProcess = foregroundProcess.split(":")[0];
        }

        return foregroundProcess;
    }

    @Override
    public void onDestroy()
    {
        unregisterReceiver(manualRegisterReceiver);
        Log.d(Constants.LOG_TAG, "EventService :: onDestroy");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d(Constants.LOG_TAG, "EventService :: onStartCommand");
        return Service.START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        Log.d(Constants.LOG_TAG, "EventService :: onTaskRemoved");
        super.onTaskRemoved(rootIntent);
        startActivity(new Intent(this, ServiceRestartActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS));
    }
}
