package rs.pedjaapps.eventlogger.utility;

import android.os.*;

public class Android
{
	private Android()
	{
		
	}
	
	public static boolean hasLolipop()
	{
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
	}
	
	public static boolean hasKitKat()
	{
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
	}
}
