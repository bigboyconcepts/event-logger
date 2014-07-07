package rs.pedjaapps.eventlogger.constants;

import android.os.Environment;

import java.io.File;

/**
 * Created by pedja on 12.4.14..
 */
public class Constants
{
    public static final String DB_NAME = "events";
    public static final String LOG_TAG = "event logger";
    public static final long LIST_REFRESH_INTERVAL = 60000l;//ms
    public static final long APP_LAUNCH_CHECK_INTERVAL = 1000l;
    public static final String PACKAGE_NAME = "rs.pedjaapps.eventlogger";
    public static final String UNLOCK_ADS_KEY_HASH = "84037fa835a3c13fa6d3f98b0dacb0c7";//md5sum of "r3m0v3_@ds"
    public static final long ONE_DAY_MS = 1000 * 60 * 60 * 24;
    public static final long ONE_HOUR_MS = 1000 * 60 * 60;
    public static final String PREFS_FILTER = "preferences_filter";
    public static final String EXTERNAL_APP_FOLDER = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator + "event_logger" + File.separator;
    public static final String EXPORT_FILENAME = "event_logger.csv";
    public static final File EXPORT_FILE = new File(EXTERNAL_APP_FOLDER, EXPORT_FILENAME);
}
