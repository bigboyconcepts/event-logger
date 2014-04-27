package rs.pedjaapps.eventlogger.constants;

import rs.pedjaapps.eventlogger.R;

/**
 * Created by pedja on 12.4.14..
 */
public enum EventType
{
    wifi, bluetooth, nfc, gps, mobile, media, usb, orientation, locale, screen, sms, app, call, headset,
    storage, boot, time, airplane, battery, wallpaper, volume, date;

    public static int getIconForType(EventType type)
    {
        switch (type)
        {
            case wifi:
                return R.drawable.ic_action_wifi;
            case bluetooth:
                return R.drawable.ic_action_bluetooth;
            case nfc:
                return R.drawable.ic_action_nfc;
            case gps:
                return R.drawable.ic_action_gps;
            case mobile:
                return R.drawable.ic_action_mobile;
            case media:
                return R.drawable.ic_action_media;
            case usb:
                return R.drawable.ic_action_usb;
            case orientation:
                return R.drawable.ic_action_orientation;
            case locale:
                return R.drawable.ic_action_locale;
            case screen:
                return R.drawable.ic_action_screen;
            case sms:
                return R.drawable.ic_action_sms;
            case app:
                return R.drawable.ic_action_app;
            case call:
                return R.drawable.ic_action_call;
            case headset:
                return R.drawable.ic_action_headset;
            case storage:
                return R.drawable.ic_action_ums;
            case boot:
                return R.drawable.ic_action_power;
            case time:
                return R.drawable.ic_action_timestamp;
            case airplane:
                return R.drawable.ic_action_airplane;
            case battery:
                return R.drawable.ic_action_battery;
            case wallpaper:
                return R.drawable.ic_action_wallpaper;
            case volume:
                return R.drawable.ic_action_volume;
            case date:
                return R.drawable.ic_action_date;
            default:
                return R.drawable.ic_action_info;
        }
    }

    public static int getIconForId(int id)
    {
        switch (id)
        {
            case 0:
                return R.drawable.ic_action_wifi;
            case 1:
                return R.drawable.ic_action_bluetooth;
            case 2:
                return R.drawable.ic_action_nfc;
            case 3:
                return R.drawable.ic_action_gps;
            case 4:
                return R.drawable.ic_action_mobile;
            case 5:
                return R.drawable.ic_action_media;
            case 6:
                return R.drawable.ic_action_usb;
            case 7:
                return R.drawable.ic_action_orientation;
            case 8:
                return R.drawable.ic_action_locale;
            case 9:
                return R.drawable.ic_action_screen;
            case 10:
                return R.drawable.ic_action_sms;
            case 11:
                return R.drawable.ic_action_app;
            case 12:
                return R.drawable.ic_action_call;
            case 13:
                return R.drawable.ic_action_headset;
            case 14:
                return R.drawable.ic_action_ums;
            case 15:
                return R.drawable.ic_action_power;
            case 16:
                return R.drawable.ic_action_timestamp;
            case 17:
                return R.drawable.ic_action_airplane;
            case 18:
                return R.drawable.ic_action_battery;
            case 19:
                return R.drawable.ic_action_wallpaper;
            case 20:
                return R.drawable.ic_action_volume;
            case 21:
                return R.drawable.ic_action_date;
            default:
                return R.drawable.ic_action_info;
        }
    }

    public static int getIntForType(EventType type)
    {
        if(type == null) return -1;
        switch (type)
        {
            case wifi:
                return 0;
            case bluetooth:
                return 1;
            case nfc:
                return 2;
            case gps:
                return 3;
            case mobile:
                return 4;
            case media:
                return 5;
            case usb:
                return 6;
            case orientation:
                return 7;
            case locale:
                return 8;
            case screen:
                return 9;
            case sms:
                return 10;
            case app:
                return 11;
            case call:
                return 12;
            case headset:
                return 13;
            case storage:
                return 14;
            case boot:
                return 15;
            case time:
                return 16;
            case airplane:
                return 17;
            case battery:
                return 18;
            case wallpaper:
                return 19;
            case volume:
                return 20;
            case date:
                return 21;
            default:
                return -1;
        }
    }

    public static EventType fromString(String type)
    {
        for (EventType t : EventType.values())
        {
            if(t.toString().equals(type))
            {
                return t;
            }
        }
        return null;
    }

}
