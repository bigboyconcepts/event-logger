package rs.pedjaapps.eventlogger;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import au.com.bytecode.opencsv.CSVWriter;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.dao.query.WhereCondition;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import rs.pedjaapps.eventlogger.adapter.EventAdapter;
import rs.pedjaapps.eventlogger.adapter.NavigationDrawerAdapter;
import rs.pedjaapps.eventlogger.constants.Constants;
import rs.pedjaapps.eventlogger.constants.EventLevel;
import rs.pedjaapps.eventlogger.constants.EventType;
import rs.pedjaapps.eventlogger.fragment.EventFilterDialog;
import rs.pedjaapps.eventlogger.fragment.EventInfoDialog;
import rs.pedjaapps.eventlogger.iab.IabHelper;
import rs.pedjaapps.eventlogger.iab.IabResult;
import rs.pedjaapps.eventlogger.iab.Inventory;
import rs.pedjaapps.eventlogger.iab.Purchase;
import rs.pedjaapps.eventlogger.model.Event;
import rs.pedjaapps.eventlogger.model.EventDao;
import rs.pedjaapps.eventlogger.model.NDItem;
import rs.pedjaapps.eventlogger.service.EventService;
import rs.pedjaapps.eventlogger.utility.SettingsManager;
import rs.pedjaapps.eventlogger.utility.Utility;
import rs.pedjaapps.eventlogger.view.EventListView;

public class MainActivity extends AbsActivity implements AdapterView.OnItemClickListener, IabHelper.OnIabSetupFinishedListener
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
    final Object mIabHelperSetupLock = new Object();
    boolean autorefreshList = false;
    boolean doRefresh = true;

    public static final String ACTION_ADD_EVENT = "action_add_event";
    public static final String ACTION_REMOVE_ADS = "action_remove_ads";
    public static final String EXTRA_EVENT = "extra_event";
    public static final String ACTION_SHOW_DETAILS = "show_details";

    InterstitialAd interstitial;
    TextView tvNoEvents;
    ProgressBar pbLoading;
    AdView adView;
    boolean interstitialLoaded = false;

    //IAB
    public static final int REQUEST_CODE_PURCHASE = 1001;

    private String SKU_PRO = /*"pro_3_99"*/"android.test.purchased";
    private IabHelper mHelper;
    private boolean iabSettupInProgress = false;

	public static final String ACTION_REFRESH_ALL = "action_refresh_all";
	
	private DrawerLayout mDrawerLayout;
    private RelativeLayout mDrawerContent;
    private ListView lvDrawer;
    NavigationDrawerAdapter ndAdapter;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

	private List<NDItem> menuItems;

    private int currentItem = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setupActivityStyle();
        setContentView(R.layout.activity_main);

        mHelper = new IabHelper(this, Utility.getIABLKey());
        mHelper.enableDebugLogging(true, Constants.LOG_TAG);
        iabSettupInProgress = true;
        mHelper.startSetup(this);

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
		filter.addAction(ACTION_REFRESH_ALL);
        LocalBroadcastManager.getInstance(this).registerReceiver(localReceiver, filter);

        adView = (AdView)findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("5750ECFACEA6FCE685DE7A97D8C59A5F")
                .addTestDevice("05FBCDCAC44495595ACE7DC1AEC5C208")
                .build();
        if(!SettingsManager.isPro())adView.loadAd(adRequest);
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
        interstitial = new InterstitialAd(this);
        interstitial.setAdUnitId("ca-app-pub-6294976772687752/1839387229");
        if(!SettingsManager.isPro())interstitial.loadAd(adRequest);
        interstitial.setAdListener(new AdListener()
        {
            @Override
            public void onAdLoaded()
            {
                super.onAdLoaded();
                interstitialLoaded = true;
            }
        });
        new ATLoadEvents().execute();
        setupTitle();
		
		
		//setup nd
		mTitle = mDrawerTitle = getTitle();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerContent = (RelativeLayout) findViewById(R.id.left_drawer);
        lvDrawer = (ListView) findViewById(R.id.lvDrawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //tvLoginLogout = (TextView)findViewById(R.id.tvMenuLoginLogout);
        //tvLoginLogout.setOnClickListener(this);

		menuItems = generateMenuOptions();
        // set up the drawer's list view with items and click listener
        ndAdapter = new NavigationDrawerAdapter(this, menuItems);
        lvDrawer.setAdapter(ndAdapter);
        lvDrawer.setOnItemClickListener(this);


        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
			this,                  /* host Activity */
			mDrawerLayout,         /* DrawerLayout object */
			R.string.drawer_open,  /* "open drawer" description for accessibility */
			R.string.drawer_closed  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
				@Override
				public void run() {
					mDrawerToggle.syncState();
				}
			});

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void setupTitle()
    {
        getSupportActionBar().setTitle(SettingsManager.isPro() ? R.string.app_name_pro : R.string.app_name);
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        if(intent != null && ACTION_SHOW_DETAILS.equals(intent.getAction()))
        {
            Event event = intent.getParcelableExtra(EXTRA_EVENT);
            if(event != null)
            {
                EventInfoDialog info = EventInfoDialog.newInstance(event);
                info.show(getSupportFragmentManager(), "event_details");
            }
        }
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
        if (interstitial.isLoaded() && !SettingsManager.isPro())
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
        if (mHelper != null) mHelper.dispose();
        mHelper = null;

        if(adView != null)adView.destroy();
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        if(interstitialLoaded)
        {
            displayInterstitial();
        }
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
                interstitialLoaded = false;
            }
			if(ACTION_REFRESH_ALL.equals(intent.getAction()))
            {
                refreshList();
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        //boolean drawerOpen = mDrawerLayout.isDrawerOpen(drawerContent);
        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
		if (mDrawerToggle.onOptionsItemSelected(item))
		{
			return true;
        }
        switch (item.getItemId())
        {
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
            case R.id.action_upgrade:
                upgradeToPro();
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
		if(adapterView.getId() == R.id.lvEvents)
		{
        	EventInfoDialog dialog = EventInfoDialog.newInstance(mEventListAdapter.getItem(i));
        	dialog.show(getSupportFragmentManager(), "event_details");
		}
		else if(adapterView.getId() == R.id.lvDrawer)
		{
			NDItem item = ndAdapter.getItem(i);
			if(item.type == NDItem.TYPE_MAIN)
			{
				switch (item.id)
				{
					case login_logout:
						break;
					case settings:
						break;
				}
				lvDrawer.setItemChecked(currentItem, true);
			}
		}
    }

    public void refreshList()
    {
        new ATLoadEvents().execute();
    }

    @Override
    public void onIabSetupFinished(IabResult result)
    {
        iabSettupInProgress = false;
        if (result.isFailure())
        {
            Utility.showToast(this, getString(R.string.play_billing_error));
            return;
        }
        queryInventoryAsync(true);
        synchronized (mIabHelperSetupLock)
        {
            mIabHelperSetupLock.notify();
        }
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
            int limit = Utility.parseInt(SettingsManager.getItemsDisplayLimit(), -1);
            if(limit != -1)
            {
                long count = queryBuilder.count();
                queryBuilder.offset((int) (count - limit) - 1);
                queryBuilder.limit(limit);
                //queryBuilder.orderAsc(EventDao.Properties.Id);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (mHelper == null || !mHelper.handleActivityResult(requestCode, resultCode, data))
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void queryInventoryAsync(final boolean silent)
    {
        mHelper.queryInventoryAsync(true, Arrays.asList(SKU_PRO), new IabHelper.QueryInventoryFinishedListener()
        {
            @Override
            public void onQueryInventoryFinished(IabResult result, Inventory inv)
            {
                if (result.isFailure())
                {
                    if(silent)
                    {
                        Log.e(Constants.LOG_TAG, getString(R.string.billing_get_list_error));
                    }
                    else
                    {
                        Utility.showMessageAlertDialog(MainActivity.this, getString(R.string.billing_get_list_error), null, null);
                    }
                    return;
                }
                Purchase purchase = inv.getPurchase(SKU_PRO);
                if(purchase != null)
                {
                    SettingsManager.setPro(true);
                    Intent intent = new Intent();
                    intent.setAction(MainActivity.ACTION_REMOVE_ADS);
                    LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
                }
                else
                {
                    SettingsManager.setPro(false);
                }
                setupTitle();
                setIcon();
            }
        });
    }

    private void showError(int response)
    {

        int errorMessage = -1;
        switch (response)
        {
            case 2:
                errorMessage = R.string.iab_unknown_error;
                break;
            case 5:
                errorMessage = R.string.iab_developer_error;
                break;
            case 6:
            case -1006:
            case -1008:
                errorMessage = R.string.iab_unknown_error;
                break;
            case 7:
                errorMessage = R.string.iab_item_owned;
                break;
            case 8:
                errorMessage = R.string.iab_item_not_owned;
                break;
            case 3:
                errorMessage = R.string.billing_unavailable;
                break;
            case 4:
                errorMessage = R.string.item_not_available;
                break;
            case -1001:
                errorMessage = R.string.iab_remote_error;
                break;
            case -1002:
                errorMessage = R.string.iab_bad_response;
                break;
            case -1003:
                errorMessage = R.string.iab_signature_error;
                break;
            case -1004:
                errorMessage = R.string.iab_send_intent_failed;
                break;
            case -1007:
                errorMessage = R.string.iab_missing_token;
                break;
            case -1009:
                errorMessage = R.string.iab_subscriptions_not_available;
                break;
            case -1010:
                errorMessage = R.string.iab_invalid_consumption_attempt;
                break;
        }
        if(errorMessage != -1)Utility.showMessageAlertDialog(this, errorMessage, 0, null);

        try
        {
            //only only way to force crashlytics logging
            if(errorMessage != -1)throw new RuntimeException("Intentional crashlytics exception");
        }
        catch(Exception e)
        {
            Crashlytics.setString("iab_error_message", IabHelper.getResponseDesc(response));
            Crashlytics.logException(e);
        }
    }

    private void upgradeToPro()
    {
        if(mHelper == null)return;
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage(getString(R.string.please_wait));
        pd.show();
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while(iabSettupInProgress)
                {
                    synchronized (mIabHelperSetupLock)
                    {
                        try
                        {
                            mIabHelperSetupLock.wait();
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        pd.dismiss();
                        if(!mHelper.isSetupDone())
                        {
                            Utility.showToast(MainActivity.this, R.string.play_billing_error);
                            return;
                        }
                        mHelper.launchPurchaseFlow(MainActivity.this, SKU_PRO, REQUEST_CODE_PURCHASE, new IabHelper.OnIabPurchaseFinishedListener()
                        {
                            @Override
                            public void onIabPurchaseFinished(IabResult result, Purchase info)
                            {
                                if (result.isFailure())
                                {
                                    showError(result.getResponse());
                                    return;
                                }
                                queryInventoryAsync(false);
                            }
                        });
                    }
                });
            }
        });
        thread.start();
    }

    private void setIcon()
    {
        if(SettingsManager.isIconAlreadyChanged())return;
        int icon = SettingsManager.isPro() ? R.drawable.ic_launcher_pro : R.drawable.ic_launcher;
        PackageManager pm = getPackageManager();
        ActivityManager am = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);

        // Enable/disable activity-aliases

        pm.setComponentEnabledSetting(
                new ComponentName(this, "rs.pedjaapps.eventlogger.MainActivity-Free"),
                icon == R.drawable.ic_launcher ?
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
        );

        pm.setComponentEnabledSetting(
                new ComponentName(this, "rs.pedjaapps.eventlogger.MainActivity-Pro"),
                icon == R.drawable.ic_launcher_pro ?
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
        );

        // Find launcher and kill it

        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        List<ResolveInfo> resolves = pm.queryIntentActivities(i, 0);
        for (ResolveInfo res : resolves) {
            if (res.activityInfo != null) {
                am.killBackgroundProcesses(res.activityInfo.packageName);
            }
        }

        // Change ActionBar icon

        getSupportActionBar().setIcon(icon);
        SettingsManager.setIconChanged();
    }
	
	
	//nd

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

	private List<NDItem> generateMenuOptions()
	{
		List<NDItem> items = new ArrayList<>();
		/*NDItem item = new NDItem();
		item.title = getString(R.string.trending_shows);
		item.id = NDItem.Id.trending;
		item.type = NDItem.TYPE_MAIN;
        item.iconRes = R.drawable.ic_action_people;
		items.add(item);

		item = new NDItem();
		item.title = getString(R.string.my_shows);
		item.id = NDItem.Id.my_shows;
		item.type = NDItem.TYPE_MAIN;
        item.iconRes = R.drawable.ic_action_favorite;
		items.add(item);

        item = new NDItem();
        item.title = getString(R.string.my_watchlist);
        item.id = NDItem.Id.my_watchlist;
        item.type = NDItem.TYPE_MAIN;
        item.iconRes = R.drawable.ic_action_watchlist;
        items.add(item);

        item = new NDItem();
        item.type = NDItem.TYPE_SEPARATOR;
        items.add(item);

        item = new NDItem();
        item.title = getString(R.string.login);
        item.id = NDItem.Id.login_logout;
        item.type = NDItem.TYPE_OPT;
        items.add(item);

        item = new NDItem();
        item.title = getString(R.string.settings);
        item.id = NDItem.Id.settings;
        item.type = NDItem.TYPE_OPT;
        items.add(item);*/

		return items;
	}
}
