package rs.pedjaapps.eventlogger.utility;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import rs.pedjaapps.eventlogger.constants.Constants;
import rs.pedjaapps.eventlogger.MainApp;
import rs.pedjaapps.eventlogger.R;

/**
 * Created by pedja on 10/9/13.
 */
public class Utility
{
    /**
     * This class can not be instantiated, it will throw exception if you try to instantiate it
     *
     * @throws IllegalStateException
     */
    public Utility()
    {
        throw new IllegalStateException("Class " + this.getClass().getName() + " is not instantiable!");
    }

    /**
     * General Purpose AlertDialog
     */
    public static AlertDialog showMessageAlertDialog(Context context, String message,
                                                     String title, DialogInterface.OnClickListener listener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title != null ? Html.fromHtml(title) : null);
        builder.setMessage(message != null ? Html.fromHtml(message) : null);
        builder.setPositiveButton(android.R.string.ok, listener);
        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }

    /**
     * General Purpose AlertDialog
     */
    public static AlertDialog showMessageAlertDialog(Context context, int message,
                                                     int title, DialogInterface.OnClickListener listener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(Html.fromHtml(context.getString(title)));
        builder.setMessage(Html.fromHtml(context.getString(message)));
        builder.setPositiveButton(android.R.string.ok, listener);
        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }

    /**
     * No Network dialog
     */
    public static AlertDialog buildNoNetworkDialog(Context context, int message,
                                                   int title, DialogInterface.OnClickListener listener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, listener);
        return builder.create();
    }

    /**
     * General Purpose Toast
     */
    public static void showToast(Context context, String message)
    {
        Toast.makeText(context, message != null ? Html.fromHtml(message) : null, Toast.LENGTH_LONG).show();
    }

    /**
     * General Purpose Toast
     */
    public static void showToast(Context context, int resId)
    {
        Toast.makeText(context, Html.fromHtml(context.getString(resId)), Toast.LENGTH_LONG).show();
    }

    /**
     * Tries to parse String as Integer
     * if error occurs default value will be returned
     *
     * @param value    String to parse as int
     * @param mDefault default value to return in case of value is not integer
     * @return parsed int or default value
     */
    public static int parseInt(String value, int mDefault)
    {
        try
        {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException e)
        {
            String log = "Utility.parseInt >> failed to parse " + value + " as integer";
            Log.w(Constants.LOG_TAG, log);
            Crashlytics.log(log);
        }
        return mDefault;
    }

    /**
     * Generate md5 sum from string
     */
    public static String md5(final String s)
    {
        final String MD5 = "MD5";
        try
        {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest)
            {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        }
        catch (NoSuchAlgorithmException e)
        {
            String log = "Utility.md5 >> error: " + e.getMessage();
            Log.e(Constants.LOG_TAG, log);
            Crashlytics.log(log);
        }
        return "";
    }


    /**
     * Generate user readable string from time seconds
     */
    public static String getTime(long timeMillis)
    {
        long timeMillisNow = new Date().getTime();
        int seconds = TimeUtility.getSeconds(timeMillis, timeMillisNow);
        if(seconds < 0) seconds = 0;
        int minutes = TimeUtility.getMinutes(seconds);
        int hours = TimeUtility.getHours(minutes);
        int days = TimeUtility.getDays(hours);
        int weeks = TimeUtility.getWeeks(days);
        int months = TimeUtility.getMonths(weeks);
        int years = TimeUtility.getYears(days);

        if (minutes <= 0)
        {
            return MainApp.getContext().getString(R.string.now);
        }
        else if (minutes < 60)
        {
            return minutes + MainApp.getContext().getString(R.string.minut);
        }
        else if (hours < 24)
        {
            return hours + MainApp.getContext().getString(R.string.hour);
        }
        else if (days < 7)
        {
            return days + MainApp.getContext().getString(R.string.day);
        }
        else if (weeks < 4)
        {
            return weeks + MainApp.getContext().getString(R.string.week);
        }
        else if (months < 12)
        {
            return months + MainApp.getContext().getString(R.string.month);
        }
        else if (years < 30)
        {
            return years + MainApp.getContext().getString(R.string.year);
        }
        else
        {
            return MainApp.getContext().getString(R.string._);
        }
    }

    /**
     * Encode string as URL UTF-8
     */
    @SuppressWarnings("deprecation")
    public static String encodeString(String string)
    {
        return URLEncoder.encode(string);
    }

    /**
     * Read file from /res/raw to string
     *
     * @param rawResId of the file
     * @return Content of the file as string
     */
    public static String readRawFile(int rawResId) throws IOException
    {
        InputStream is = MainApp.getContext().getResources().openRawResource(rawResId);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String read;
        StringBuilder sb = new StringBuilder();
        while ((read = br.readLine()) != null)
        {
            sb.append(read);
        }
        return sb.toString();
    }

    public static String sha1Hash(String toHash)
    {
        String hash = null;
        try
        {
            MessageDigest digest = MessageDigest.getInstance( "SHA-1" );
            byte[] bytes = toHash.getBytes("UTF-8");
            digest.update(bytes, 0, bytes.length);
            bytes = digest.digest();

            // This is ~55x faster than looping and String.formating()
            hash = bytesToHex( bytes );
        }
        catch( NoSuchAlgorithmException e )
        {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        catch( UnsupportedEncodingException e )
        {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        return hash;
    }

    // http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex( byte[] bytes )
    {
        char[] hexChars = new char[ bytes.length * 2 ];
        for( int j = 0; j < bytes.length; j++ )
        {
            int v = bytes[ j ] & 0xFF;
            hexChars[ j * 2 ] = hexArray[ v >>> 4 ];
            hexChars[ j * 2 + 1 ] = hexArray[ v & 0x0F ];
        }
        return new String( hexChars );
    }

    public static boolean arrayContainsString(String[] array, String value)
    {
        for(String s : array)
        {
            if(value.equals(s))return true;
        }
        return false;
    }

}
