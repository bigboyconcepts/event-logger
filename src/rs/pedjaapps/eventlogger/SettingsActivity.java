package rs.pedjaapps.eventlogger;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.support.v4.content.LocalBroadcastManager;
import java.util.Arrays;
import java.util.List;
import rs.pedjaapps.eventlogger.R;
import rs.pedjaapps.eventlogger.constants.Constants;
import rs.pedjaapps.eventlogger.utility.SettingsManager;
import rs.pedjaapps.eventlogger.utility.Utility;
import android.preference.Preference.OnPreferenceChangeListener;
/**
 * Created by pedja on 20.4.14..
 */
public class SettingsActivity extends PreferenceActivity
{
    long aboutFirstClickTs = 0;
    int aboutClickCount = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        final EditTextPreference etpRemoveAds = (EditTextPreference) findPreference("remove_ads_unlock_key");
        if(etpRemoveAds != null)
        {
            if(!SettingsManager.showRemoveAds())
            {
                getPreferenceScreen().removePreference(etpRemoveAds);
            }
            refreshRemoveAds(etpRemoveAds);
            etpRemoveAds.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o)
                {
                    if(o == null)return false;
                    String hashedNewValue = Utility.md5(o.toString());
                    if(hashedNewValue == null)return false;

                    if(hashedNewValue.equals(Constants.UNLOCK_ADS_KEY_HASH))
                    {
                        SettingsManager.setAdsRemoved();
                        Intent intent = new Intent();
                        intent.setAction(MainActivity.ACTION_REMOVE_ADS);
                        LocalBroadcastManager.getInstance(SettingsActivity.this).sendBroadcast(intent);
                        Utility.showToast(SettingsActivity.this, R.string.ads_removed);
                    }
                    else
                    {
                        Utility.showToast(SettingsActivity.this, R.string.wrong_key);
                        SettingsManager.setUnlockAttemptsLeft();
                    }
                    refreshRemoveAds(etpRemoveAds);
                    return false;
                }
            });
        }

        PreferenceScreen about = (PreferenceScreen) findPreference("prefs_about");
        if(about != null)
        {
            String version = "";
            try
            {
                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                version = pInfo.versionName;
            }
            catch (PackageManager.NameNotFoundException e)
            {
                e.printStackTrace();
                //should never happen
            }
            about.setTitle(getString(R.string.app_name) + " " + version);
            about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    if(SettingsManager.showRemoveAds())
                    {
                        return true;
                    }
                    if(aboutClickCount == 0)
                    {
                        aboutFirstClickTs = System.currentTimeMillis();
                    }
                    aboutClickCount++;
                    long ts = System.currentTimeMillis();
                    if(aboutClickCount == 5 && (aboutFirstClickTs - ts) <= 5000)
                    {
                        getPreferenceScreen().addPreference(etpRemoveAds);
                        SettingsManager.setShowRemoveAds(true);
                    }
                    return true;
                }
            });
        }
		final ListPreference timeDisplay = (ListPreference) findPreference("time_display");
		final List<String> timeDisplayEntries = Arrays.asList(getResources().getStringArray(R.array.timeDisplayEntries));
		final List<String> timeDisplayValues = Arrays.asList(getResources().getStringArray(R.array.timeDisplayValues));
		timeDisplay.setSummary(timeDisplayEntries.get(timeDisplayValues.indexOf(SettingsManager.getTimeDisplay())));
		timeDisplay.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{

				@Override
				public boolean onPreferenceChange(Preference p1, Object p2)
				{
					timeDisplay.setSummary(timeDisplayEntries.get(timeDisplayValues.indexOf(p2.toString())));
					Intent intent = new Intent();
					intent.setAction(MainActivity.ACTION_REFRESH_ALL);
					LocalBroadcastManager.getInstance(SettingsActivity.this).sendBroadcast(intent);
					Utility.showToast(SettingsActivity.this, R.string.ads_removed);
					return true;
				}
		});
    }

    private void refreshRemoveAds(EditTextPreference etpRemoveAds)
    {
        if(SettingsManager.removeDisabled())
        {
            etpRemoveAds.setEnabled(false);
            etpRemoveAds.setSummary(R.string.unlock_disabled);
        }
        else if(SettingsManager.adsRemoved())
        {
            etpRemoveAds.setEnabled(false);
            etpRemoveAds.setSummary(R.string.ads_removed);
        }
        else
        {
            etpRemoveAds.setEnabled(true);
            etpRemoveAds.setSummary(R.string.ads_not_removed);
        }
    }
	
}
