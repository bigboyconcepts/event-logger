package rs.pedjaapps.eventlogger.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v4.content.LocalBroadcastManager;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import rs.pedjaapps.eventlogger.MainActivity;
import rs.pedjaapps.eventlogger.MainApp;
import rs.pedjaapps.eventlogger.R;
import rs.pedjaapps.eventlogger.utility.SettingsManager;
import rs.pedjaapps.eventlogger.utility.Utility;

/**
 * Created by pedja on 4.11.14. 11.28.
 * This class is part of the event-logger
 * Copyright © 2014 ${OWNER}
 */
public class SettingsFragment extends PreferenceFragment
{
    //long aboutFirstClickTs = 0;
    //int aboutClickCount = 0;
    ListPreference displaylimit;
    PreferenceScreen clearDb;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        /*final EditTextPreference etpRemoveAds = (EditTextPreference) findPreference("remove_ads_unlock_key");
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
        }*/

        PreferenceScreen about = (PreferenceScreen) findPreference("prefs_about");
        if(about != null)
        {
            String version = "";
            try
            {
                PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                version = pInfo.versionName;
            }
            catch (PackageManager.NameNotFoundException e)
            {
                e.printStackTrace();
                //should never happen
            }
            about.setTitle(getString(R.string.app_name) + " " + version);
            /*about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
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
            });*/
        }

        final ListPreference timeDisplay = (ListPreference) findPreference("time_display");
        final List<String> timeDisplayEntries = Arrays.asList(getResources().getStringArray(R.array.timeDisplayEntries));
        final List<String> timeDisplayValues = Arrays.asList(getResources().getStringArray(R.array.timeDisplayValues));
        timeDisplay.setSummary(timeDisplayEntries.get(timeDisplayValues.indexOf(SettingsManager.getTimeDisplay())));
        timeDisplay.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference p1, Object p2)
            {
                timeDisplay.setSummary(timeDisplayEntries.get(timeDisplayValues.indexOf(p2.toString())));
                Intent intent = new Intent();
                intent.setAction(MainActivity.ACTION_REFRESH_ALL);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                return true;
            }
        });


        final long eventCount = MainApp.getInstance().getDaoSession().getEventDao().queryBuilder().count();
        displaylimit = (ListPreference) findPreference("items_display_limit");
        final List<String> displayLimit = Arrays.asList(getResources().getStringArray(R.array.displayLimit));
        displaylimit.setSummary(displayLimit.get(displayLimit.indexOf(SettingsManager.getItemsDisplayLimit())) + "/" + eventCount);
        displaylimit.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference p1, Object p2)
            {
                displaylimit.setSummary(displayLimit.get(displayLimit.indexOf(p2.toString())) + "/" + eventCount);
                Intent intent = new Intent();
                intent.setAction(MainActivity.ACTION_REFRESH_ALL);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                return true;
            }
        });

        clearDb = (PreferenceScreen) findPreference("prefs_clear_db");
        if(clearDb != null)
        {
            setClearDbSummary();
            clearDb.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    clearDbDialog();
                    return true;
                }
            });
        }
    }

    private void setClearDbSummary()
    {
        if(clearDb == null)return;
        long dbSizeBytes = new File(MainApp.getInstance().getDaoSession().getDatabase().getPath()).length();
        clearDb.setSummary(getString(R.string.db_size) + " " + Utility.byteToHumanReadableSize(dbSizeBytes));
    }

    private void clearDbDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.clear_db);
        builder.setMessage(R.string.clear_db_warning);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                new ATClearDb().execute();
            }
        });
        builder.setNegativeButton(R.string.no, null);
        builder.show();
    }

    /*private void refreshRemoveAds(EditTextPreference etpRemoveAds)
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
    }*/

    private class ATClearDb extends AsyncTask<Void, Void, Void>
    {
        ProgressDialog pdLoading;

        public ATClearDb()
        {
            pdLoading = new ProgressDialog(getActivity());
            pdLoading.setMessage(getString(R.string.please_wait));
        }

        @Override
        protected Void doInBackground(Void... voids)
        {
            MainApp.getInstance().getDaoSession().getEventDao().deleteAll();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            if (displaylimit != null)
            {
                final long eventCount = MainApp.getInstance().getDaoSession().getEventDao().queryBuilder().count();
                final List<String> displayLimit = Arrays.asList(getResources().getStringArray(R.array.displayLimit));
                displaylimit.setSummary(displayLimit.get(displayLimit.indexOf(SettingsManager.getItemsDisplayLimit())) + "/" + eventCount);
            }
            setClearDbSummary();
            Intent intent = new Intent();
            intent.setAction(MainActivity.ACTION_REFRESH_ALL);
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
            if(pdLoading != null)
            {
                pdLoading.dismiss();
            }
        }

        @Override
        protected void onPreExecute()
        {
            if(pdLoading != null)
            {
                pdLoading.show();
            }
        }
    }
}
