package rs.pedjaapps.eventlogger.xposed;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import rs.pedjaapps.eventlogger.MainApp;
import rs.pedjaapps.eventlogger.R;
import rs.pedjaapps.eventlogger.constants.EventLevel;
import rs.pedjaapps.eventlogger.constants.EventType;
import rs.pedjaapps.eventlogger.model.Event;
import rs.pedjaapps.eventlogger.model.EventDao;
import rs.pedjaapps.eventlogger.receiver.EventReceiver;

import static de.robv.android.xposed.XposedHelpers.findClass;

/**
 * Created by pedja on 12.4.14..
 */
public class MediaPlayerHook implements IXposedHookLoadPackage, IXposedHookZygoteInit
{
    //android.media.MediaPlayer
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable
    {
        //do nothing for now
    }


    @Override
    public void initZygote(StartupParam startupParam) throws Throwable
    {
        hookMediaPlayer("android.media.MediaPlayer");
    }

    private void hookMediaPlayer(String context)
    {
        try
        {
            XC_MethodHook methodHook = new XC_MethodHook()
            {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable
                {

                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable
                {
                    Event event = new Event();
                    event.setTimestamp(new Date());
                    event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
                    event.setType(EventType.getIntForType(EventType.media));
                    if(param.method.getName().equals("start"))
                    {
                        event.setShort_desc(MainApp.getContext().getString(R.string.media_start_short));
                        event.setLong_desc(MainApp.getContext().getString(R.string.media_start_long));
                    }
                    else if(param.method.getName().equals("pause"))
                    {
                        event.setShort_desc(MainApp.getContext().getString(R.string.media_pause_short));
                        event.setLong_desc(MainApp.getContext().getString(R.string.media_pause_long));
                    }
                    else if(param.method.getName().equals("stop"))
                    {
                        event.setShort_desc(MainApp.getContext().getString(R.string.media_stop_short));
                        event.setLong_desc(MainApp.getContext().getString(R.string.media_stop_long));
                    }
                    EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
                    eventDao.insert(event);
                    EventReceiver.sendLocalBroadcast(event);
                    XposedBridge.log("before hooked method" + param.method.getName());
                }
            };

            Set<XC_MethodHook.Unhook> hookSet = new HashSet<XC_MethodHook.Unhook>();

            Class<?> hookClass = null;
            try
            {
                hookClass = findClass(context, null);
                if (hookClass == null)
                    throw new ClassNotFoundException(context);
            }
            catch (Exception ex)
            {
                XposedBridge.log("MediaPlayer exception" + ex);
            }

             XposedBridge.log("MediaPlayer Find Class " + hookClass);
            Class<?> clazz = hookClass;
            while (clazz != null)
            {
                for (Method method : clazz.getDeclaredMethods())
                {
                    if (method != null && (method.getName().equals("start") || method.getName().equals("pause") || method.getName().equals("stop")))
                    {
                        hookSet.add(XposedBridge.hookMethod(method, methodHook));
                    }
                }
                clazz = (hookSet.isEmpty() ? clazz.getSuperclass() : null);
            }
        }
        catch (Exception ex)
        {
             XposedBridge.log("MediaPlayerHook Exception " + ex);
        }
    }

}
