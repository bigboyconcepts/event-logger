package rs.pedjaapps.eventlogger;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;
import rs.pedjaapps.eventlogger.R;
import rs.pedjaapps.eventlogger.utility.SettingsManager;

public class AboutActivity extends AbsActivity
{

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		TextView tvAbout = (TextView)findViewById(R.id.tvAbout);
		tvAbout.setText(Html.fromHtml(getString(R.string.about_text, getString(SettingsManager.isPro() ? R.string.app_name_pro_styled : R.string.app_name), getAppVersion())));
		
		TextView tvOsl = (TextView)findViewById(R.id.tvOsl);
		tvOsl.setText(Html.fromHtml(getString(R.string.osl_text)));
		
		TextView tvDev = (TextView) findViewById(R.id.tvDev);
		tvDev.setText(Html.fromHtml(getString(R.string.developer_text)));
	}
	
	private String getAppVersion()
	{
		try
		{
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			return pInfo.versionName;
		}
		catch (PackageManager.NameNotFoundException e)
		{
			e.printStackTrace();
			//should never happen
			return null;
		}
	}
}
