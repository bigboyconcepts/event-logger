package rs.pedjaapps.eventlogger.utility;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import rs.pedjaapps.eventlogger.MainApp;

/**
 * Created by pedja on 14.4.14..
 */
public class SettingsManager
{
    public static SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainApp.getContext());


}
