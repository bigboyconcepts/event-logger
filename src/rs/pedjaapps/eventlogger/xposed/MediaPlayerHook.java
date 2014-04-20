package rs.pedjaapps.eventlogger.xposed;

import android.content.Context;

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
import rs.pedjaapps.eventlogger.constants.Constants;
import rs.pedjaapps.eventlogger.constants.EventLevel;
import rs.pedjaapps.eventlogger.constants.EventType;
import rs.pedjaapps.eventlogger.model.Event;
import rs.pedjaapps.eventlogger.model.EventDao;
import rs.pedjaapps.eventlogger.receiver.EventReceiver;
import rs.pedjaapps.eventlogger.receiver.InsertEventReceiver;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import android.app.AndroidAppHelper;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

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
        /*if(lpparam.packageName.equals(Constants.PACKAGE_NAME))
        {
            XposedBridge.log(lpparam.packageName);
            final Class<?> mClass = findClass(Constants.PACKAGE_NAME + ".MainApp", lpparam.classLoader);
            XposedBridge.hookAllMethods(mClass, "onCreate", new XC_MethodHook()
            {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable
                {
                    XposedBridge.log(param.method.getName());
                    // save the launcher instance and the context
                    Constants.context = (Context) callMethod(param.thisObject, "getApplicationContext");
                    XposedBridge.log("Context: " + Constants.context);
                }
            });
        }*/
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable
    {
        hookMediaPlayer("android.media.MediaPlayer");
    }

    private void hookMediaPlayer(final String context)
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
                    Context cappContext = AndroidAppHelper.currentApplication().getApplicationContext();
                    Context context = cappContext.createPackageContext(Constants.PACKAGE_NAME, Context.CONTEXT_IGNORE_SECURITY);
                    XposedBridge.log("Context: " + context);
                    Event event = new Event();
                    event.setTimestamp(new Date());
                    event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
                    event.setType(EventType.getIntForType(EventType.media));
                    if(param.method.getName().equals("start"))
                    {
                        event.setShort_desc(context.getString(R.string.media_start_short));
                        event.setLong_desc(context.getString(R.string.media_start_long));
                    }
                    else if(param.method.getName().equals("pause"))
                    {
                        event.setShort_desc(context.getString(R.string.media_pause_short));
                        event.setLong_desc(context.getString(R.string.media_pause_long));
                    }
                    else if(param.method.getName().equals("stop"))
                    {
                        event.setShort_desc(context.getString(R.string.media_stop_short));
                        event.setLong_desc(context.getString(R.string.media_stop_long));
                    }
                    XposedBridge.log(event.toString());
                    //EventDao eventDao = MainApp.getInstance().getDaoSession(context).getEventDao();
                    //eventDao.insert(event);
                    //EventReceiver.sendLocalBroadcast(event, context);
                    Intent intent = new Intent();
                    intent.setAction(InsertEventReceiver.ACTION_INSERT_EVENT);
                    Bundle extras = new Bundle();
                    extras.putParcelable(InsertEventReceiver.EXTRA_EVENT, event);
                    intent.putExtras(extras);
                    context.sendBroadcast(intent);
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
