package rs.pedjaapps.eventlogger.utility;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Date;

import rs.pedjaapps.eventlogger.MainApp;
import rs.pedjaapps.eventlogger.constants.Constants;

/**
 * Created by pedja on 14.4.14..
 */
public class SettingsManager
{
    public static SharedPreferences prefsDefault = PreferenceManager.getDefaultSharedPreferences(MainApp.getContext());
    public static SharedPreferences prefsFilter = MainApp.getContext().getSharedPreferences(Constants.PREFS_FILTER, Activity.MODE_PRIVATE);

    public enum Key
    {
        remove_ads, remove_ads_disabled, unlock_attempts_left, filter_date, add_shown_ts, time_filter_enabled,
        type_filter_enabled, level_filter_enabled, filter_time_from, filter_time_to, filter_level_error,
        filter_level_warning, filter_level_info, filter_level_ok
    }

    public static void setAdsRemoved()
    {
        SharedPreferences.Editor editor = prefsDefault.edit();
        editor.putBoolean(Key.remove_ads.toString(), true);
        editor.apply();
    }

    public static boolean adsRemoved()
    {
        return prefsDefault.getBoolean(Key.remove_ads.toString(), false);
    }

    public static int getUnlockAttemptsLeft()
    {
        return prefsDefault.getInt(Key.unlock_attempts_left.toString(), 3);
    }

    public static synchronized void setUnlockAttemptsLeft()
    {
        SharedPreferences.Editor editor = prefsDefault.edit();
        int attemptsLeft = getUnlockAttemptsLeft();
        attemptsLeft--;
        if (attemptsLeft <= 0)
        {
            editor.putBoolean(Key.remove_ads_disabled.toString(), true);
        }
        editor.putInt(Key.unlock_attempts_left.toString(), attemptsLeft);
        editor.apply();
    }

    public static boolean removeDisabled()
    {
        return prefsDefault.getBoolean(Key.remove_ads_disabled.toString(), false);
    }

    public static boolean canDisplayAdds()
    {
        long now = new Date().getTime();
        return now - prefsDefault.getLong(Key.add_shown_ts.toString(), now - Constants.ONE_HOUR_MS) >= Constants.ONE_HOUR_MS;
    }

    public static void setAdShownTs()
    {
        SharedPreferences.Editor editor = prefsDefault.edit();
        editor.putLong(Key.add_shown_ts.toString(), new Date().getTime());
        editor.apply();
    }

    public static boolean isTimeFilterEnabled()
    {
        return prefsFilter.getBoolean(Key.time_filter_enabled.toString(), false);
    }

    public static boolean isTypeFilterEnabled()
    {
        return prefsFilter.getBoolean(Key.type_filter_enabled.toString(), false);
    }

    public static boolean isLevelFilterEnabled()
    {
        return prefsFilter.getBoolean(Key.level_filter_enabled.toString(), false);
    }

    public static Date getFilterTimeFrom(long def)
    {
        return new Date(prefsFilter.getLong(Key.filter_time_from.toString(), def));
    }

    public static Date getFilterTimeTo(long def)
    {
        return new Date(prefsFilter.getLong(Key.filter_time_to.toString(), def));
    }

    public static void setTimeFilterFrom(long time)
    {
        SharedPreferences.Editor editor = prefsFilter.edit();
        editor.putLong(Key.filter_time_from.toString(), time);
        editor.apply();
    }

    public static void setTimeFilterTo(long time)
    {
        SharedPreferences.Editor editor = prefsFilter.edit();
        editor.putLong(Key.filter_time_to.toString(), time);
        editor.apply();
    }

    public static boolean isFilterLevelErrorEnabled()
    {
        return prefsFilter.getBoolean(Key.filter_level_error.toString(), true);
    }

    public static boolean isFilterLevelWarningEnabled()
    {
        return prefsFilter.getBoolean(Key.filter_level_warning.toString(), true);
    }

    public static boolean isFilterLevelInfoEnabled()
    {
        return prefsFilter.getBoolean(Key.filter_level_info.toString(), true);
    }

    public static boolean isFilterLevelOkEnabled()
    {
        return prefsFilter.getBoolean(Key.filter_level_ok.toString(), true);
    }

    public static void setFilterLevelError(boolean enabled)
    {
        SharedPreferences.Editor editor = prefsFilter.edit();
        editor.putBoolean(Key.filter_level_error.toString(), enabled);
        editor.apply();
    }

    public static void setFilterLevelWarning(boolean enabled)
    {
        SharedPreferences.Editor editor = prefsFilter.edit();
        editor.putBoolean(Key.filter_level_warning.toString(), enabled);
        editor.apply();
    }

    public static void setFilterLevelInfo(boolean enabled)
    {
        SharedPreferences.Editor editor = prefsFilter.edit();
        editor.putBoolean(Key.filter_level_info.toString(), enabled);
        editor.apply();
    }

    public static void setFilterLevelOk(boolean enabled)
    {
        SharedPreferences.Editor editor = prefsFilter.edit();
        editor.putBoolean(Key.filter_level_ok.toString(), enabled);
        editor.apply();
    }
}
