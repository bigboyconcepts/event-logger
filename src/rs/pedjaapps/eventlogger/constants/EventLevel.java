package rs.pedjaapps.eventlogger.constants;

import android.graphics.Color;

import rs.pedjaapps.eventlogger.R;

/**
 * Created by pedja on 12.4.14..
 */
public enum EventLevel
{
    /**
     * Indicates error<br>
     * eg. device exploded<br>
     * errors are rare
     * */
    error(Color.RED),
    /**
     * indicates warning<br>
     * eg. cd card has been removed without being previously unmounted
     * */
    warning(Color.YELLOW),
    /**
     * indicates info<br>
     * eg. wifi turned on
     * */
    info(Color.GREEN),
    /**
     * This type is used when system recovers from error<br>
     * For example after battery has been low and it again ok
     * */
    ok(Color.CYAN);

    int mColor;

    EventLevel(int color)
    {
        mColor = color;
    }

    public int color()
    {
        return mColor;
    }

    public static EventLevel getLevelForInt(int level)
    {
        switch (level)
        {
            case 0:
                return error;
            case 1:
                return warning;
            case 2:
                return info;
            case 3:
                return ok;
            default:
                return info;
        }
    }

    public static int getTextForInt(int level)
    {
        switch (level)
        {
            case 0:
                return R.string.error;
            case 1:
                return R.string.warning;
            case 2:
                return R.string.info;
            case 3:
                return R.string.ok;
            default:
                return R.string.info;
        }
    }

    public static int getIntForLevel(EventLevel level)
    {
        switch (level)
        {
            case error:
                return 0;
            case warning:
                return 1;
            case info:
                return 2;
            case ok:
                return 3;
            default:
                return 2;
        }
    }
}
