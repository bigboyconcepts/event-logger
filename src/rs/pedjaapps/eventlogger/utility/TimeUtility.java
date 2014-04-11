package rs.pedjaapps.eventlogger.utility;

/**
 * Created by pedja on 2/6/14.
 * Converts different time units
 */
public class TimeUtility
{
    public static int getSeconds(long timeMillis, long timeMillisNow)
    {
        return (int) ((timeMillisNow - timeMillis) / 1000);
    }

    public static int getMinutes(long timeMillis, long timeMillisNow)
    {
        return (int) ((timeMillisNow - timeMillis) / 1000 / 60);
    }

    public static int getMinutes(long seconds)
    {
        return (int) (seconds / 60);
    }

    public static int getHours(long timeMillis, long timeMillisNow)
    {
        return getMinutes(timeMillis, timeMillisNow) / 60;
    }

    public static int getHours(long minutes)
    {
        return (int) (minutes / 60);
    }

    public static int getDays(long timeMillis, long timeMillisNow)
    {
        return getHours(timeMillis, timeMillisNow) / 24;
    }

    public static int getDays(long hours)
    {
        return (int) (hours / 24);
    }

    public static int getWeeks(long timeMillis, long timeMillisNow)
    {
        return getDays(timeMillis, timeMillisNow) / 7;
    }

    public static int getWeeks(long days)
    {
        return (int) (days / 7);
    }

    public static int getMonths(long weeks)
    {
        return (int) (weeks / 4);
    }

    public static int getYears(long timeMillis, long timeMillisNow)
    {
        return getDays(timeMillis, timeMillisNow) / 365;
    }

    public static int getYears(long days)
    {
        return (int) (days / 365);
    }
}
