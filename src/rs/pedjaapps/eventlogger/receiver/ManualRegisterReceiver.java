package rs.pedjaapps.eventlogger.receiver;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.BatteryManager;
import android.os.Bundle;

import java.util.Date;
import java.util.Locale;

import rs.pedjaapps.eventlogger.constants.EventLevel;
import rs.pedjaapps.eventlogger.constants.EventType;
import rs.pedjaapps.eventlogger.MainApp;
import rs.pedjaapps.eventlogger.R;
import rs.pedjaapps.eventlogger.model.Event;
import rs.pedjaapps.eventlogger.model.EventDao;
import rs.pedjaapps.eventlogger.service.EventService;

/**
 * Created by pedja on 11.4.14..
 */
public class ManualRegisterReceiver extends AbsEventReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(intent == null || intent.getAction() == null /*|| intent.getExtras() == null*/)
        {
            return;
        }
        Bundle extras = intent.getExtras();
        if(intent.getAction().equals(Intent.ACTION_CONFIGURATION_CHANGED))
        {
            boolean landscape = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
            String newOrientation = landscape ? context.getString(R.string.landscape) : context.getString(R.string.portrait);
            String oldOrientation = !landscape ? context.getString(R.string.landscape) : context.getString(R.string.portrait);
            Event event = new Event();
            event.setTimestamp(new Date());
            event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
            event.setType(EventType.getIntForType(EventType.orientation));
            event.setShort_desc(context.getString(R.string.orientation_changed, newOrientation.toUpperCase()));
            event.setLong_desc(context.getString(R.string.orientation_changed_desc, oldOrientation, newOrientation));
            EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
            eventDao.insert(event);
            sendLocalBroadcast(event);
        }
        if(intent.getAction().equals(Intent.ACTION_REBOOT))
        {
            Event event = new Event();
            event.setTimestamp(new Date());
            event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
            event.setType(EventType.getIntForType(EventType.boot));
            event.setShort_desc(context.getString(R.string.device_reboot));
            event.setLong_desc(context.getString(R.string.reboot_desc));
            EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
            eventDao.insert(event);
            sendLocalBroadcast(event);
        }
        if(intent.getAction().equals(Intent.ACTION_SHUTDOWN))
        {
            Event event = new Event();
            event.setTimestamp(new Date());
            event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
            event.setType(EventType.getIntForType(EventType.boot));
            event.setShort_desc(context.getString(R.string.device_shutdown));
            event.setLong_desc(context.getString(R.string.shutdown_desc));
            EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
            eventDao.insert(event);
            sendLocalBroadcast(event);
        }
        if(intent.getAction().equals(Intent.ACTION_POWER_CONNECTED))
        {
            Event event = new Event();
            event.setTimestamp(new Date());
            event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
            event.setType(EventType.getIntForType(EventType.usb));
            event.setShort_desc(context.getString(R.string.power_connected));
            event.setLong_desc(context.getString(R.string.power_connected_desc));
            EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
            eventDao.insert(event);
            sendLocalBroadcast(event);
        }
        if(intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED))
        {
            Event event = new Event();
            event.setTimestamp(new Date());
            event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
            event.setType(EventType.getIntForType(EventType.usb));
            event.setShort_desc(context.getString(R.string.power_disconnected));
            event.setLong_desc(context.getString(R.string.power_disconnected_desc));
            EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
            eventDao.insert(event);
            sendLocalBroadcast(event);
        }
        if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED) && false)//TODO add option to log all battery options
        {
            Event event = new Event();
            event.setTimestamp(new Date());
            event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
            event.setType(EventType.getIntForType(EventType.battery));
            if(extras != null)
            {
                String sState = context.getString(R.string.unknown).toUpperCase();
                String sColor = "red";
                int state = extras.getInt(BatteryManager.EXTRA_STATUS);
                switch (state)
                {
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        sState = context.getString(R.string.charging).toUpperCase();
                        sColor = "green";
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        sState = context.getString(R.string.discharging).toUpperCase();
                        sColor = "yellow";
                        break;
                    case BatteryManager.BATTERY_STATUS_FULL:
                        sState = context.getString(R.string.full).toUpperCase();
                        sColor = "blue";
                        break;
                    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                        sState = context.getString(R.string.not_charging).toUpperCase();
                        sColor = "red";
                        break;
                    case BatteryManager.BATTERY_STATUS_UNKNOWN:
                        sState = context.getString(R.string.unknown).toUpperCase();
                        sColor = "red";
                        break;
                }
                int level = extras.getInt(BatteryManager.EXTRA_LEVEL);
                String batteryHealth = context.getString(R.string.good).toUpperCase();
                int health = extras.getInt(BatteryManager.EXTRA_HEALTH);
                switch (health)
                {
                    case BatteryManager.BATTERY_HEALTH_COLD:
                        batteryHealth = context.getString(R.string.cold).toUpperCase();
                        break;
                    case BatteryManager.BATTERY_HEALTH_DEAD:
                        batteryHealth = context.getString(R.string.dead).toUpperCase();
                        break;
                    case BatteryManager.BATTERY_HEALTH_GOOD:
                        batteryHealth = context.getString(R.string.good).toUpperCase();
                        break;
                    case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                        batteryHealth = context.getString(R.string.over_voltage).toUpperCase();
                        break;
                    case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                        batteryHealth = context.getString(R.string.overheat).toUpperCase();
                        break;
                    case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                        batteryHealth = context.getString(R.string.unknown).toUpperCase();
                        break;
                    case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                        batteryHealth = context.getString(R.string.unspecified_failure).toUpperCase();
                        break;
                }
                event.setShort_desc(context.getString(R.string.battery_state_changed, sColor, sState));
                event.setLong_desc(context.getString(R.string.battery_state_desc, sState, level, batteryHealth,
                        extras.getString(BatteryManager.EXTRA_TECHNOLOGY), extras.getString(BatteryManager.EXTRA_TEMPERATURE), extras.getString(BatteryManager.EXTRA_VOLTAGE)));
            }
            else
            {
                event.setShort_desc(context.getString(R.string.battery_state_changed, "red", context.getString(R.string.na).toUpperCase()));
                event.setLong_desc(context.getString(R.string.battery_state_desc, context.getString(R.string.na).toUpperCase(),
                        context.getString(R.string.na).toUpperCase(), context.getString(R.string.na).toUpperCase(),
                        context.getString(R.string.na).toUpperCase(), context.getString(R.string.na).toUpperCase(),
                        context.getString(R.string.na).toUpperCase()));
            }

            EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
            eventDao.insert(event);
            sendLocalBroadcast(event);
        }
        if(intent.getAction().equals(Intent.ACTION_BATTERY_LOW))
        {
            Event event = new Event();
            event.setTimestamp(new Date());
            event.setLevel(EventLevel.getIntForLevel(EventLevel.warning));
            event.setType(EventType.getIntForType(EventType.battery));
            event.setShort_desc(context.getString(R.string.battery_low));
            event.setLong_desc(context.getString(R.string.battery_low_desc));
            EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
            eventDao.insert(event);
            sendLocalBroadcast(event);
        }
        if(intent.getAction().equals(Intent.ACTION_BATTERY_OKAY))
        {
            Event event = new Event();
            event.setTimestamp(new Date());
            event.setLevel(EventLevel.getIntForLevel(EventLevel.ok));
            event.setType(EventType.getIntForType(EventType.battery));
            event.setShort_desc(context.getString(R.string.battery_ok));
            event.setLong_desc(context.getString(R.string.battery_ok_desc));
            EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
            eventDao.insert(event);
            sendLocalBroadcast(event);
        }
        if(intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED))
        {
            String locale = Locale.getDefault().getDisplayLanguage();
            Event event = new Event();
            event.setTimestamp(new Date());
            event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
            event.setType(EventType.getIntForType(EventType.locale));
            event.setShort_desc(context.getString(R.string.locale_changed, locale));
            event.setLong_desc(context.getString(R.string.locale_changed_desc, locale));
            EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
            eventDao.insert(event);
            sendLocalBroadcast(event);
        }
        if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
        {
            Event event = new Event();
            event.setTimestamp(new Date());
            event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
            event.setType(EventType.getIntForType(EventType.screen));
            event.setShort_desc(context.getString(R.string.screen_off));
            event.setLong_desc(context.getString(R.string.screen_off_desc));
            EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
            eventDao.insert(event);
            sendLocalBroadcast(event);
        }
        if(intent.getAction().equals(Intent.ACTION_SCREEN_ON))
        {
            Event event = new Event();
            event.setTimestamp(new Date());
            event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
            event.setType(EventType.getIntForType(EventType.screen));
            event.setShort_desc(context.getString(R.string.screen_on));
            event.setLong_desc(context.getString(R.string.screen_on_desc));
            EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
            eventDao.insert(event);
            sendLocalBroadcast(event);
        }
        if(intent.getAction().equals(Intent.ACTION_USER_PRESENT))
        {
            Event event = new Event();
            event.setTimestamp(new Date());
            event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
            event.setType(EventType.getIntForType(EventType.screen));
            event.setShort_desc(context.getString(R.string.screen_unlocked));
            event.setLong_desc(context.getString(R.string.screen_unlocked_desc));
            EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
            eventDao.insert(event);
            sendLocalBroadcast(event);
        }
        if(intent.getAction().equals(Intent.ACTION_HEADSET_PLUG))
        {
            int state = extras != null ? extras.getInt("state") : -1;//1=plug, 0=unplug
            String name = extras != null ? extras.getString("name") : context.getString(R.string.na).toUpperCase();
            int hasMic = extras != null ? extras.getInt("microphone") : -1;
            String sHasMic = context.getString(R.string.na);
            if(hasMic == 1)
            {
                sHasMic = context.getString(R.string.yes);
            }
            else if(hasMic == 2)
            {
                sHasMic = context.getString(R.string.no);
            }
            Event event = new Event();
            event.setTimestamp(new Date());
            event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
            event.setType(EventType.getIntForType(EventType.headset));
            if(state == 1)
            {
                event.setShort_desc(context.getString(R.string.headset_plugged, name));
                event.setLong_desc(context.getString(R.string.headset_plugged_desc, name, sHasMic));
            }
            else if(state == 0)
            {
                event.setShort_desc(context.getString(R.string.headset_unplugged, name));
                event.setLong_desc(context.getString(R.string.headset_unplugged_desc, name, sHasMic));
            }
            else
            {
                event.setShort_desc(context.getString(R.string.headset_status_changed));
                event.setLong_desc(context.getString(R.string.headset_unknown_desc));
            }

            EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
            eventDao.insert(event);
            sendLocalBroadcast(event);
        }
        if(intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_STARTED))
        {
            Event event = new Event();
            event.setTimestamp(new Date());
            event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
            event.setType(EventType.getIntForType(EventType.media));
            event.setShort_desc(context.getString(R.string.scanner_started));
            event.setLong_desc(context.getString(R.string.scanner_started_desc));
            EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
            eventDao.insert(event);
            sendLocalBroadcast(event);
        }
        if(intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_FINISHED))
        {
            Event event = new Event();
            event.setTimestamp(new Date());
            event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
            event.setType(EventType.getIntForType(EventType.media));
            event.setShort_desc(context.getString(R.string.scanner_finished));
            event.setLong_desc(context.getString(R.string.scanner_finished_desc));
            EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
            eventDao.insert(event);
            sendLocalBroadcast(event);
        }
    }
}
