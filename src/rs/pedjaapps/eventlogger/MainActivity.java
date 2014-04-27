package rs.pedjaapps.eventlogger;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import au.com.bytecode.opencsv.CSVWriter;
import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.dao.query.WhereCondition;
import rs.pedjaapps.eventlogger.adapter.EventAdapter;
import rs.pedjaapps.eventlogger.constants.Constants;
import rs.pedjaapps.eventlogger.constants.EventLevel;
import rs.pedjaapps.eventlogger.constants.EventType;
import rs.pedjaapps.eventlogger.fragment.EventFilterDialog;
import rs.pedjaapps.eventlogger.fragment.EventInfoDialog;
import rs.pedjaapps.eventlogger.model.Event;
import rs.pedjaapps.eventlogger.model.EventDao;
import rs.pedjaapps.eventlogger.service.EventService;
import rs.pedjaapps.eventlogger.utility.SettingsManager;
import rs.pedjaapps.eventlogger.utility.Utility;
import rs.pedjaapps.eventlogger.view.EventListView;

public class MainActivity extends AbsActivity implements AdapterView.OnItemClickListener
{
    // Any change in height in the message list view greater than this threshold will not
    // cause a smooth scroll. Instead, we jump the list directly to the desired position.
    protected static final int SMOOTH_SCROLL_THRESHOLD = 200;

    // When the conversation has a lot of messages and a new message is sent, the list is scrolled
    // so the user sees the just sent message. If we have to scroll the list more than 20 items,
    // then a scroll shortcut is invoked to move the list near the end before scrolling.
    protected static final int MAX_ITEMS_TO_INVOKE_SCROLL_SHORTCUT = 20;

    protected EventListView mEventListView;
    protected EventAdapter mEventListAdapter;
    protected int mLastSmoothScrollPosition;
    final Object refreshLock = new Object();
    boolean autorefreshList = false;
    boolean doRefresh = true;

    public static final String ACTION_ADD_EVENT = "action_add_event";
    public static final String ACTION_REMOVE_ADS = "action_remove_ads";
    public static final String EXTRA_EVENT = "extra_event";

    InterstitialAd interstitial;
    TextView tvNoEvents;
    ProgressBar pbLoading;
    AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setupActivityStyle();
        setContentView(R.layout.activity_main);

        tvNoEvents = (TextView) findViewById(R.id.tvNoEvents);
        pbLoading = (ProgressBar) findViewById(R.id.pbLoading);

        startService(new Intent(this, EventService.class));
        mEventListView = (EventListView) findViewById(R.id.lvEvents);
        mEventListView.setOnSizeChangedListener(new EventListView.OnSizeChangedListener()
        {
            public void onSizeChanged(int width, int height, int oldWidth, int oldHeight)
            {
                // The message list view changed size, most likely because the keyboard
                // appeared or disappeared or the user typed/deleted chars in the message
                // box causing it to change its height when expanding/collapsing to hold more
                // lines of text.
                smoothScrollToEnd(false, height - oldHeight);
            }
        });
        mEventListView.setClipToPadding(true);

        mEventListAdapter = new EventAdapter(this, new ArrayList<Event>());
        mEventListView.setAdapter(mEventListAdapter);
        mEventListView.setOnItemClickListener(this);

        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (doRefresh)
                {
                    while (!autorefreshList)
                    {
                        synchronized (refreshLock)
                        {
                            try
                            {
                                refreshLock.wait();
                            }
                            catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                    try
                    {
                        Thread.sleep(Constants.LIST_REFRESH_INTERVAL);
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                mEventListAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }

            }
        });
        thread.start();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_ADD_EVENT);
        filter.addAction(ACTION_REMOVE_ADS);
        LocalBroadcastManager.getInstance(this).registerReceiver(localReceiver, filter);

        adView = (AdView)findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("5750ECFACEA6FCE685DE7A97D8C59A5F")
                .addTestDevice("05FBCDCAC44495595ACE7DC1AEC5C208")
                .build();
        if(!SettingsManager.adsRemoved())adView.loadAd(adRequest);
        adView.setAdListener(new AdListener()
        {
            @Override
            public void onAdLoaded()
            {
                super.onAdLoaded();
                adView.setVisibility(View.VISIBLE);
            }
        });

        //test interstitial ad
        // Create the interstitial.
        /*interstitial = new InterstitialAd(this);
        interstitial.setAdUnitId("ca-app-pub-6294976772687752/1839387229");
        if(!SettingsManager.adsRemoved() && SettingsManager.canDisplayAdds())interstitial.loadAd(adRequest);
        interstitial.setAdListener(new AdListener()
        {
            @Override
            public void onAdLoaded()
            {
                super.onAdLoaded();
                displayInterstitial();
            }
        });*/
        new ATLoadEvents().execute();
    }

    private void setupActivityStyle()
    {
        if (getResources().getBoolean(R.bool.isTablet))
        {
            requestWindowFeature(Window.FEATURE_ACTION_BAR);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.alpha = 1.0f;
            params.dimAmount = 0.5f;
            getWindow().setAttributes(params);

            // This sets the window size, while working around the IllegalStateException thrown by ActionBarView
            WindowManager manager = getWindowManager();
            Display display = manager.getDefaultDisplay();
            int sWidth, sHeight, aWidth, aHeight;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
            {
                Point size = new Point();
                display.getSize(size);
                sWidth = size.x;
                sHeight = size.y;
            }
            else
            {
                sWidth = display.getWidth();
                sHeight = display.getHeight();
            }
            boolean landscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
            aWidth = landscape ? sWidth / 2 : (sWidth - sWidth / 5);
            aHeight = landscape ? (sHeight - sHeight / 5) : sHeight / 2;

            getWindow().setLayout(aWidth, aHeight);
        }
    }

    public void displayInterstitial()
    {
        if (interstitial.isLoaded())
        {
            interstitial.show();
            SettingsManager.setAdShownTs();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        autorefreshList = true;
        synchronized (refreshLock)
        {
            refreshLock.notifyAll();
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (!isFinishing()) autorefreshList = false;
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        doRefresh = false;
    }

    BroadcastReceiver localReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if(ACTION_ADD_EVENT.equals(intent.getAction()))
            {
                Event event = intent.getParcelableExtra(EXTRA_EVENT);
                if (event == null || !eventMatchesFilter(event)) return;
                mEventListAdapter.add(event);
                mEventListAdapter.notifyDataSetChanged();
                smoothScrollToEnd(true, 0);
                tvNoEvents.setVisibility(mEventListAdapter.getCount() == 0 ? View.VISIBLE : View.GONE);
            }
            if(ACTION_REMOVE_ADS.equals(intent.getAction()))
            {
                if (adView != null)
                {
                    adView.destroy();
                    adView.setVisibility(View.GONE);
                }
            }
        }
    };

    private boolean eventMatchesFilter(Event event)
    {
        if(SettingsManager.isTimeFilterEnabled())
        {
            if(event.getTimestamp().before(SettingsManager.getFilterTimeFrom(0))) return false;
            if(event.getTimestamp().after(SettingsManager.getFilterTimeTo(new Date().getTime()))) return false;
        }
        if(SettingsManager.isTypeFilterEnabled())
        {
            List<Integer> enabledTypes = new ArrayList<Integer>();
            String[] filerTypes = SettingsManager.getFilterTypes();
            for(String s : filerTypes)
            {
                enabledTypes.add(EventType.getIntForType(EventType.fromString(s)));
            }
            if(!enabledTypes.contains(event.getType())) return false;
        }
        if(SettingsManager.isLevelFilterEnabled())
        {
            List<Integer> enabledLevels = new ArrayList<Integer>();
            if(SettingsManager.isFilterLevelErrorEnabled())enabledLevels.add(EventLevel.getIntForLevel(EventLevel.error));
            if(SettingsManager.isFilterLevelWarningEnabled())enabledLevels.add(EventLevel.getIntForLevel(EventLevel.warning));
            if(SettingsManager.isFilterLevelInfoEnabled())enabledLevels.add(EventLevel.getIntForLevel(EventLevel.info));
            if(SettingsManager.isFilterLevelOkEnabled())enabledLevels.add(EventLevel.getIntForLevel(EventLevel.ok));
            if(!enabledLevels.contains(event.getLevel())) return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_about:
                break;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.action_filter:
                EventFilterDialog dialog = EventFilterDialog.newInstance();
                dialog.show(getSupportFragmentManager(), "event_filter");
                break;
            case R.id.action_export:
                new ATExportDB().execute();
                break;
            case R.id.action_share:
                try
                {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, Utility.readFileToString(Constants.EXPORT_FILE));
                    sendIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Utility.showMessageAlertDialog(this, getString(R.string.share_failed, e.getMessage()), null, null);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * smoothScrollToEnd will scroll the message list to the bottom if the list is already near
     * the bottom. Typically this is called to smooth scroll a newly received message into view.
     * It's also called when sending to scroll the list to the bottom, regardless of where it is,
     * so the user can see the just sent message. This function is also called when the message
     * list view changes size because the keyboard state changed or the compose message field grew.
     *
     * @param force          always scroll to the bottom regardless of current list position
     * @param listSizeChange the amount the message list view size has vertically changed
     */
    protected void smoothScrollToEnd(boolean force, int listSizeChange)
    {
        int lastItemVisible = mEventListView.getLastVisiblePosition();
        int lastItemInList = mEventListAdapter.getCount() - 1;
        if (lastItemVisible < 0 || lastItemInList < 0)
        {
            return;
        }

        View lastChildVisible =
                mEventListView.getChildAt(lastItemVisible - mEventListView.getFirstVisiblePosition());
        int lastVisibleItemBottom = 0;
        int lastVisibleItemHeight = 0;
        if (lastChildVisible != null)
        {
            lastVisibleItemBottom = lastChildVisible.getBottom();
            lastVisibleItemHeight = lastChildVisible.getHeight();
        }

        // Only scroll if the list if we're responding to a newly sent message (force == true) or
        // the list is already scrolled to the end. This code also has to handle the case where
        // the listview has changed size (from the keyboard coming up or down or the message entry
        // field growing/shrinking) and it uses that grow/shrink factor in listSizeChange to
        // compute whether the list was at the end before the resize took place.
        // For example, when the keyboard comes up, listSizeChange will be negative, something
        // like -524. The lastChild listitem's bottom value will be the old value before the
        // keyboard became visible but the size of the list will have changed. The test below
        // add listSizeChange to bottom to figure out if the old position was already scrolled
        // to the bottom. We also scroll the list if the last item is taller than the size of the
        // list. This happens when the keyboard is up and the last item is an mms with an
        // attachment thumbnail, such as picture. In this situation, we want to scroll the list so
        // the bottom of the thumbnail is visible and the top of the item is scroll off the screen.
        int listHeight = mEventListView.getHeight();
        boolean lastItemTooTall = lastVisibleItemHeight > listHeight;
        boolean willScroll = force ||
                ((listSizeChange != 0 || lastItemInList != mLastSmoothScrollPosition) &&
                        lastVisibleItemBottom + listSizeChange <=
                                listHeight - mEventListView.getPaddingBottom());
        if (willScroll || (lastItemTooTall && lastItemInList == lastItemVisible))
        {
            if (Math.abs(listSizeChange) > SMOOTH_SCROLL_THRESHOLD)
            {
                // When the keyboard comes up, the window manager initiates a cross fade
                // animation that conflicts with smooth scroll. Handle that case by jumping the
                // list directly to the end.

                if (lastItemTooTall)
                {
                    // If the height of the last item is taller than the whole height of the list,
                    // we need to scroll that item so that its top is negative or above the top of
                    // the list. That way, the bottom of the last item will be exposed above the
                    // keyboard.
                    mEventListView.setSelectionFromTop(lastItemInList,
                            listHeight - lastVisibleItemHeight);
                }
                else
                {
                    mEventListView.setSelection(lastItemInList);
                }
            }
            else if (lastItemInList - lastItemVisible > MAX_ITEMS_TO_INVOKE_SCROLL_SHORTCUT)
            {
                mEventListView.setSelection(lastItemInList);
            }
            else
            {
                if (lastItemTooTall)
                {
                    // If the height of the last item is taller than the whole height of the list,
                    // we need to scroll that item so that its top is negative or above the top of
                    // the list. That way, the bottom of the last item will be exposed above the
                    // keyboard. We should use smoothScrollToPositionFromTop here, but it doesn't
                    // seem to work -- the list ends up scrolling to a random position.
                    mEventListView.setSelectionFromTop(lastItemInList,
                            listHeight - lastVisibleItemHeight);
                }
                else
                {
                    mEventListView.smoothScrollToPosition(lastItemInList);
                }
                mLastSmoothScrollPosition = lastItemInList;
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {
        EventInfoDialog dialog = EventInfoDialog.newInstance(mEventListAdapter.getItem(i));
        dialog.show(getSupportFragmentManager(), "event_details");
    }

    public void refreshList()
    {
        new ATLoadEvents().execute();
    }

    public class ATLoadEvents extends AsyncTask<String, Void, List<Event>>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pbLoading.setVisibility(View.VISIBLE);
            tvNoEvents.setVisibility(View.GONE);
        }

        @Override
        protected List<Event> doInBackground(String... params)
        {
            QueryBuilder<Event> queryBuilder = getDaoSession().getEventDao().queryBuilder();
            if(SettingsManager.isTimeFilterEnabled())
            {
                queryBuilder.where(EventDao.Properties.Timestamp.between(SettingsManager.getFilterTimeFrom(0), SettingsManager.getFilterTimeTo(new Date().getTime())));
            }
            if(SettingsManager.isTypeFilterEnabled())
            {
                List<Integer> enabledTypes = new ArrayList<Integer>();
                String[] filerTypes = SettingsManager.getFilterTypes();
                for(String s : filerTypes)
                {
                    enabledTypes.add(EventType.getIntForType(EventType.fromString(s)));
                }
                if(enabledTypes.size() == 1)
                {
                    queryBuilder.where(EventDao.Properties.Type.eq(enabledTypes.get(0)));
                }
                else if(enabledTypes.size() > 1)
                {
                    WhereCondition cond1 = EventDao.Properties.Type.eq(enabledTypes.get(0));
                    WhereCondition cond2 = EventDao.Properties.Type.eq(enabledTypes.get(1));
                    List<WhereCondition> orConditions = new ArrayList<WhereCondition>();
                    for(int i = 2; i < enabledTypes.size(); i++)
                    {
                        orConditions.add(EventDao.Properties.Type.eq(enabledTypes.get(i)));
                    }

                    queryBuilder.where(queryBuilder.or(cond1, cond2, orConditions.toArray(new WhereCondition[orConditions.size()])));
                }
            }
            if(SettingsManager.isLevelFilterEnabled())
            {
                List<Integer> enabledLevels = new ArrayList<Integer>();
                if(SettingsManager.isFilterLevelErrorEnabled())enabledLevels.add(EventLevel.getIntForLevel(EventLevel.error));
                if(SettingsManager.isFilterLevelWarningEnabled())enabledLevels.add(EventLevel.getIntForLevel(EventLevel.warning));
                if(SettingsManager.isFilterLevelInfoEnabled())enabledLevels.add(EventLevel.getIntForLevel(EventLevel.info));
                if(SettingsManager.isFilterLevelOkEnabled())enabledLevels.add(EventLevel.getIntForLevel(EventLevel.ok));
                if(enabledLevels.size() == 1)
                {
                    queryBuilder.where(EventDao.Properties.Level.eq(enabledLevels.get(0)));
                }
                else if(enabledLevels.size() > 1)
                {
                    WhereCondition cond1 = EventDao.Properties.Level.eq(enabledLevels.get(0));
                    WhereCondition cond2 = EventDao.Properties.Level.eq(enabledLevels.get(1));
                    List<WhereCondition> orConditions = new ArrayList<WhereCondition>();
                    for(int i = 2; i < enabledLevels.size(); i++)
                    {
                        orConditions.add(EventDao.Properties.Level.eq(enabledLevels.get(i)));
                    }

                    queryBuilder.where(queryBuilder.or(cond1, cond2, orConditions.toArray(new WhereCondition[orConditions.size()])));
                }
            }

            return queryBuilder.list();
        }

        @Override
        protected void onPostExecute(List<Event> events)
        {
            pbLoading.setVisibility(View.GONE);
            mEventListAdapter.clear();
            if (events == null || events.isEmpty())
            {
                tvNoEvents.setVisibility(View.VISIBLE);
            }
            else
            {
                for (Event event : events)
                {
                    mEventListAdapter.add(event);
                }
                mEventListAdapter.notifyDataSetChanged();
                mEventListView.setSelection(mEventListAdapter.getCount() - 1);
                mEventListView.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mEventListView.setSelection(mEventListAdapter.getCount() - 1);
                    }
                });
            }
        }
    }

    private class ATExportDB extends AsyncTask<String, Integer, Boolean>
    {
        ProgressDialog progressDialog;
        String failMessage;
        @Override
        protected void onPreExecute()
        {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage(getString(R.string.please_wait));
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings)
        {
            File dir = new File(Constants.EXTERNAL_APP_FOLDER);
            File file = Constants.EXPORT_FILE;
            dir.mkdirs();
            CSVWriter csvWrite = null;
            try
            {
                DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault());

                csvWrite = new CSVWriter(new FileWriter(file));

                //ormlite core method
                EventDao eventDao = MainApp.getInstance().getDaoSession().getEventDao();
                List<Event> events = eventDao.loadAll();

                // this is the Column of the table and same for Header of CSV file
                String header[] ={EventDao.Properties.Id.columnName, EventDao.Properties.Timestamp.columnName,
                        EventDao.Properties.Short_desc.columnName, EventDao.Properties.Long_desc.columnName,
                        EventDao.Properties.Type.columnName, EventDao.Properties.Level.columnName};
                csvWrite.writeNext(header);
                publishProgress(-1, events.size());

                if(events.size() > 1)
                {
                    int offset = 0;
                    for(Event event : events)
                    {
                        String value[] ={event.getId() + "", format.format(event.getTimestamp()), event.getShort_desc(),
                                event.getLong_desc(), EventType.getEventTypeForId(event.getType()).toString(),
                                EventLevel.getLevelForInt(event.getLevel()).toString()};
                        csvWrite.writeNext(value);
                        publishProgress(offset);
                        offset++;
                    }
                }
                return true;
            }
            catch (IOException e)
            {
                e.printStackTrace();
                failMessage = e.getMessage();
                return false;
            }
            finally
            {
                try
                {
                    if(csvWrite != null)csvWrite.close();
                }
                catch (IOException ignored){}
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values)
        {
            if(values == null || values.length == 0)
            {
                return;
            }
            if(values[0] == -1)
            {
                progressDialog.setMax(values[1]);
            }
            else
            {
                progressDialog.setProgress(values[0]);
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean)
        {
            Utility.showMessageAlertDialog(MainActivity.this, aBoolean ? getString(R.string.export_success, Constants.EXPORT_FILE.getAbsolutePath()) : getString(R.string.export_failed, failMessage), null, null);
            progressDialog.dismiss();
        }
    }
}
