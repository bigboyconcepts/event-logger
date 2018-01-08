package rs.pedjaapps.eventlogger;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import rs.pedjaapps.eventlogger.fragment.SettingsFragment;
/**
 * Created by pedja on 20.4.14..
 */
public class SettingsActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getFragmentManager().beginTransaction().replace(R.id.content_frame, new SettingsFragment()).commit();

    }

}
