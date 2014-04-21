package rs.pedjaapps.eventlogger.utility;

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
    public static SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainApp.getContext());

    public enum Key
    {
        remove_ads, remove_ads_disabled, unlock_attempts_left, filter_date, add_shown_ts
    }

    public static void setAdsRemoved()
    {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Key.remove_ads.toString(), true);
        editor.apply();
    }

    public static boolean adsRemoved()
    {
        return prefs.getBoolean(Key.remove_ads.toString(), false);
    }

    public static int getUnlockAttemptsLeft()
    {
        return prefs.getInt(Key.unlock_attempts_left.toString(), 3);
    }

    public static synchronized void setUnlockAttemptsLeft()
    {
        SharedPreferences.Editor editor = prefs.edit();
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
        return prefs.getBoolean(Key.remove_ads_disabled.toString(), false);
    }

    public static boolean canDisplayAdds()
    {
        long now = new Date().getTime();
        return now - prefs.getLong(Key.add_shown_ts.toString(), now - Constants.ONE_HOUR_MS) >= Constants.ONE_HOUR_MS;
    }

    public static void setAdShownTs()
    {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(Key.add_shown_ts.toString(), new Date().getTime());
        editor.apply();
    }
}
