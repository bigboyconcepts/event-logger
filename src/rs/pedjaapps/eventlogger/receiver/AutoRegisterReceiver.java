package rs.pedjaapps.eventlogger.receiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;

import java.util.Date;
import java.util.Set;

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
public class AutoRegisterReceiver extends AbsEventReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(intent == null || intent.getAction() == null /*|| intent.getExtras() == null*/)
        {
            return;
        }
        //TODO only if battery save isn't enabled
        context.startService(new Intent(context, EventService.class));
        Bundle extras = intent.getExtras();
        if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION))
        {
            /*networkinfo(NetworkInfo), bssid(string), linkProperties(LinkProperties)*/
            /*wifiInfo=SSID: WiredSSID, BSSID: 01:80:c2:00:00:03, MAC: 08:00:27:64:07:04, Supplicant state: COMPLETED, RSSI: -65, Link speed: 0, Net ID: 0, Metered hint: false*/
            NetworkInfo networkInfo = extras != null ? (NetworkInfo) extras.getParcelable("networkInfo") : null;
            if(networkInfo == null || networkInfo.getState() != NetworkInfo.State.CONNECTED && networkInfo.getState() != NetworkInfo.State.DISCONNECTED) return;
            WifiInfo wifiInfo = extras.getParcelable("wifiInfo");
            if(networkInfo.getState() == NetworkInfo.State.CONNECTED && wifiInfo == null)return;
            Event event = new Event();
            event.setTimestamp(new Date());
            event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
            event.setType(EventType.getIntForType(EventType.wifi));
            if(networkInfo.isConnected())
            {
                event.setShort_desc(context.getString(R.string.wifi_state_changed, "green", context.getString(R.string.connected_upper)));
                event.setLong_desc(context.getString(R.string.wifi_connected_description, wifiInfo.getSSID(), wifiInfo.getBSSID(), wifiInfo.getMacAddress(), wifiInfo.getIpAddress()));
            }
            else
            {
                event.setShort_desc(context.getString(R.string.wifi_state_changed, "red", context.getString(R.string.disconnected_upper)));
                event.setLong_desc(context.getString(R.string.wifi_disconnected_description));
            }

            EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
            eventDao.insert(event);
            sendLocalBroadcast(event);
        }
        if(intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION))
        {
            /*wifi_state(int), previous_wifi_state(int)*/
            int wifiState = extras != null ? extras.getInt("wifi_state") : -1;
            if (wifiState != WifiManager.WIFI_STATE_DISABLED && wifiState != WifiManager.WIFI_STATE_ENABLED)
                return;
            Event event = new Event();
            event.setTimestamp(new Date());
            event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
            event.setType(EventType.getIntForType(EventType.wifi));
            switch (wifiState)
            {
                case WifiManager.WIFI_STATE_DISABLED:
                    event.setShort_desc(context.getString(R.string.wifi_state_changed, "red", context.getString(R.string.disabled_upper)));
                    event.setLong_desc(context.getString(R.string.wifi_toggle_description, context.getString(R.string.disabled_lower)));
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    event.setShort_desc(context.getString(R.string.wifi_state_changed, "green", context.getString(R.string.enabled_upper)));
                    event.setLong_desc(context.getString(R.string.wifi_toggle_description, context.getString(R.string.enabled_lower)));
                    break;
            }
            EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
            eventDao.insert(event);
            sendLocalBroadcast(event);
        }
        if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED))
        {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            if(state != BluetoothAdapter.STATE_OFF && state != BluetoothAdapter.STATE_ON
                    && state != BluetoothAdapter.STATE_CONNECTED && state != BluetoothAdapter.STATE_DISCONNECTED) return;
            Event event = new Event();
            event.setTimestamp(new Date());
            event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
            event.setType(EventType.getIntForType(EventType.bluetooth));
            switch (state)
            {
                case BluetoothAdapter.STATE_OFF:
                    event.setShort_desc(context.getString(R.string.bluetooth_state_changed, "red", context.getString(R.string.disabled_upper)));
                    event.setLong_desc(context.getString(R.string.bluetooth_toggle_description, context.getString(R.string.disabled_lower)));
                    break;
                case BluetoothAdapter.STATE_ON:
                    event.setShort_desc(context.getString(R.string.bluetooth_state_changed, "green", context.getString(R.string.enabled_upper)));
                    event.setLong_desc(context.getString(R.string.bluetooth_toggle_description, context.getString(R.string.enabled_lower)));
                    break;
                case BluetoothAdapter.STATE_CONNECTED:

                    break;
                case BluetoothAdapter.STATE_DISCONNECTED:

                    break;
            }
            EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
            eventDao.insert(event);
            sendLocalBroadcast(event);
        }
        if (intent.getAction().equals(BluetoothDevice.ACTION_ACL_CONNECTED))
        {
            BluetoothDevice device = extras != null ? (BluetoothDevice) extras.getParcelable(BluetoothDevice.EXTRA_DEVICE) : null;
            Event event = new Event();
            event.setTimestamp(new Date());
            event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
            event.setType(EventType.getIntForType(EventType.bluetooth));
            event.setShort_desc(context.getString(R.string.bluetooth_state_changed, "green", context.getString(R.string.connected_upper)));
            if(device != null)
            {
                event.setLong_desc(context.getString(R.string.bluetooth_connected_description, device.getAddress(), device.getName()));
            }

            EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
            eventDao.insert(event);
            sendLocalBroadcast(event);
        }
        if (intent.getAction().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED))
        {
            BluetoothDevice device = extras != null ? (BluetoothDevice) extras.getParcelable(BluetoothDevice.EXTRA_DEVICE) : null;
            Event event = new Event();
            event.setTimestamp(new Date());
            event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
            event.setType(EventType.getIntForType(EventType.bluetooth));
            event.setShort_desc(context.getString(R.string.bluetooth_state_changed, "red", context.getString(R.string.disconnected_upper)));
            if(device != null)
            {
                event.setLong_desc(context.getString(R.string.bluetooth_disconnected_description, device.getAddress(), device.getName()));
            }

            EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
            eventDao.insert(event);
            sendLocalBroadcast(event);
        }
        if(intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION))
        {
            Event event = new Event();
            event.setTimestamp(new Date());
            event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
            event.setType(EventType.getIntForType(EventType.sms));
            event.setShort_desc(context.getString(R.string.sms_received));

            SmsMessage[] messages = new SmsMessage[0];
            try
            {
                Object[] pdus = (Object[]) extras.get("pdus");
                messages = new SmsMessage[pdus.length];
                for(int i = 0; i < pdus.length; i++)
                {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            String address = messages[0].getOriginatingAddress();
            event.setLong_desc(context.getString(R.string.received_sms_desc, address));
            EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
            eventDao.insert(event);
            sendLocalBroadcast(event);
        }
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) || intent.getAction().equals("android.intent.action.QUICKBOOT_POWERON"))
        {
            Event event = new Event();
            event.setTimestamp(new Date());
            event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
            event.setType(EventType.getIntForType(EventType.boot));
            event.setShort_desc(context.getString(R.string.boot_completed));
            event.setLong_desc(context.getString(R.string.boot_completed_desc));
            EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
            eventDao.insert(event);
            sendLocalBroadcast(event);
        }
        if(intent.getAction().equals(Intent.ACTION_TIME_CHANGED))
        {
            Event event = new Event();
            event.setTimestamp(new Date());
            event.setLevel(EventLevel.getIntForLevel(EventLevel.warning));
            event.setType(EventType.getIntForType(EventType.time));
            event.setShort_desc(context.getString(R.string.time_changed));
            event.setLong_desc(context.getString(R.string.time_changed_desc));
            EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
            eventDao.insert(event);
            sendLocalBroadcast(event);
        }
        if(intent.getAction().equals(Intent.ACTION_DATE_CHANGED))
        {
            Event event = new Event();
            event.setTimestamp(new Date());
            event.setLevel(EventLevel.getIntForLevel(EventLevel.warning));
            event.setType(EventType.getIntForType(EventType.date));
            event.setShort_desc(context.getString(R.string.date_changed));
            event.setLong_desc(context.getString(R.string.date_changed_desc));
            EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
            eventDao.insert(event);
            sendLocalBroadcast(event);
        }
        if(intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED))
        {
            Event event = new Event();
            event.setTimestamp(new Date());
            event.setLevel(EventLevel.getIntForLevel(EventLevel.warning));
            event.setType(EventType.getIntForType(EventType.time));
            event.setShort_desc(context.getString(R.string.timezone_changed));
            event.setLong_desc(context.getString(R.string.timezone_changed_desc, extras != null ? extras.getString("time-zone") : "n/a"));
            EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
            eventDao.insert(event);
            sendLocalBroadcast(event);
        }
        if(intent.getAction().equals(Intent.ACTION_AIRPLANE_MODE_CHANGED))
        {
            boolean isEnabled = extras != null && extras.getBoolean("state");
            Event event = new Event();
            event.setTimestamp(new Date());
            event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
            event.setType(EventType.getIntForType(EventType.airplane));
            event.setShort_desc(context.getString(R.string.airplane_mode_toggled, isEnabled ? "green" : "red", context.getString(isEnabled ? R.string.enabled_upper : R.string.disabled_upper)));
            event.setLong_desc(context.getString(R.string.airplane_mode_desc, context.getString(isEnabled ? R.string.enabled_lower : R.string.disabled_lower)));
            EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
            eventDao.insert(event);
            sendLocalBroadcast(event);
        }
        if(intent.getAction().equals(Intent.ACTION_WALLPAPER_CHANGED))
        {
            Event event = new Event();
            event.setTimestamp(new Date());
            event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
            event.setType(EventType.getIntForType(EventType.wallpaper));
            event.setShort_desc(context.getString(R.string.wallpaper_changed));
            event.setLong_desc(context.getString(R.string.wallpaper_changed_desc));
            EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
            eventDao.insert(event);
            sendLocalBroadcast(event);
        }
        if(intent.getAction().equals(LocationManager.PROVIDERS_CHANGED_ACTION))
        {
            final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            boolean gpsEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            Event event = new Event();
            event.setTimestamp(new Date());
            event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
            event.setType(EventType.getIntForType(EventType.gps));
            event.setShort_desc(context.getString(R.string.gps_state_changed, gpsEnabled ? "green" : "red", context.getString(gpsEnabled ? R.string.enabled_upper : R.string.disabled_upper)));
            event.setLong_desc(context.getString(R.string.gps_state_changed_desc, context.getString(gpsEnabled ? R.string.enabled_lower : R.string.disabled_lower)));
            EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
            eventDao.insert(event);
            sendLocalBroadcast(event);
        }
        if(intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION"))
        {
            /*android.media.EXTRA_VOLUME_STREAM_TYPE=2*/
            /*android.media.EXTRA_PREV_VOLUME_STREAM_VALUE=6*/
            /*android.media.EXTRA_VOLUME_STREAM_VALUE=7*/
            /*2=ring, 3=media*/
            if(extras != null)
            {
                int prev = extras.getInt("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE");
                int now = extras.getInt("android.media.EXTRA_VOLUME_STREAM_VALUE");
                if(prev == now)return;
            }
            String prevVolume = extras != null ? "" + extras.getInt("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE") : context.getString(R.string.na).toUpperCase();
            String newVolume = extras != null ? "" + extras.getInt("android.media.EXTRA_VOLUME_STREAM_VALUE") : context.getString(R.string.na).toUpperCase();
            int streamType = extras != null ? extras.getInt("android.media.EXTRA_VOLUME_STREAM_TYPE") : -1;
            String type = "";
            if(streamType == 2) type = context.getString(R.string.ringer);
            else if(streamType == 3) type = context.getString(R.string.media);
            Event event = new Event();
            event.setTimestamp(new Date());
            event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
            event.setType(EventType.getIntForType(EventType.volume));
            event.setShort_desc(context.getString(R.string.volume_changed, newVolume));
            event.setLong_desc(context.getString(R.string.volume_changed_desc, type, prevVolume, newVolume));
            EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
            eventDao.insert(event);
            sendLocalBroadcast(event);
        }
        if(intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL))
        {
            /*android.intent.extra.PHONE_NUMBER=2222*/
            String phoneNum = extras != null ? extras.getString(Intent.EXTRA_PHONE_NUMBER) : context.getString(R.string.na).toUpperCase();
            Event event = new Event();
            event.setTimestamp(new Date());
            event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
            event.setType(EventType.getIntForType(EventType.call));
            event.setShort_desc(context.getString(R.string.outgoing_call, phoneNum));
            event.setLong_desc(context.getString(R.string.outgoing_call_desc, phoneNum));
            EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
            eventDao.insert(event);
            sendLocalBroadcast(event);
        }
        if(intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED))
        {
            if(extras != null && !TelephonyManager.EXTRA_STATE_RINGING.equals(extras.getString(TelephonyManager.EXTRA_STATE)))
            {
                return;
            }
            String phoneNum = extras != null ? extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER) : context.getString(R.string.na).toUpperCase();
            Event event = new Event();
            event.setTimestamp(new Date());
            event.setLevel(EventLevel.getIntForLevel(EventLevel.info));
            event.setType(EventType.getIntForType(EventType.call));
            event.setShort_desc(context.getString(R.string.incoming_call, phoneNum));
            event.setLong_desc(context.getString(R.string.incoming_call_desc));
            EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
            eventDao.insert(event);
            sendLocalBroadcast(event);
        }
    }
}
