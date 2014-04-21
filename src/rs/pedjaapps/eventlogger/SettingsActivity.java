package rs.pedjaapps.eventlogger;

import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v4.content.LocalBroadcastManager;

import rs.pedjaapps.eventlogger.constants.Constants;
import rs.pedjaapps.eventlogger.utility.SettingsManager;
import rs.pedjaapps.eventlogger.utility.Utility;

/**
 * Created by pedja on 20.4.14..
 */
public class SettingsActivity extends PreferenceActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        final EditTextPreference etpRemoveAds = (EditTextPreference) findPreference("remove_ads_unlock_key");
        if(etpRemoveAds != null)
        {
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
