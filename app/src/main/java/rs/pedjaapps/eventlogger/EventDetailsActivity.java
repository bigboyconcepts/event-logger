package rs.pedjaapps.eventlogger;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Locale;

import rs.pedjaapps.eventlogger.constants.EventLevel;
import rs.pedjaapps.eventlogger.constants.EventType;
import rs.pedjaapps.eventlogger.model.Event;
import rs.pedjaapps.eventlogger.model.Icon;
import rs.pedjaapps.eventlogger.utility.Utility;

/**
 * Created by pedja on 5.11.14. 09.58.
 * This class is part of the event-logger
 * Copyright © 2014 ${OWNER}
 */
public class EventDetailsActivity extends AbsActivity
{
    public static final String EXTRA_EVENT = "extra_event";
    Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        event = getIntent().getParcelableExtra(EXTRA_EVENT);

        if(event == null)
        {
            Utility.showToast(this, R.string.invalid_event);
            finish();
            return;
        }

        setContentView(R.layout.activity_event_info);

        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault());
        TextView tvTimestamp = (TextView)findViewById(R.id.tvTimestamp);
        TextView tvEventLevelColor = (TextView)findViewById(R.id.tvEventLevelColor);
        TextView tvEventLevelText = (TextView)findViewById(R.id.tvEventLevelText);
        TextView tvLongDesc = (TextView)findViewById(R.id.tvLongDesc);
        TextView tvEventDetails = (TextView)findViewById(R.id.tvEventDetails);
        tvTimestamp.setText(format.format(event.getTimestamp().getTime()));
        tvEventLevelColor.setBackgroundColor(EventLevel.getLevelForInt(event.getLevel()).color());
        tvLongDesc.setText(Html.fromHtml(event.getLong_desc()));
        Icon icon = MainApp.getInstance().getDaoSession().getIconDao().load(event.getIcon_id());
        if(icon == null || icon.getIcon() == null || icon.getIcon().length == 0)
        {
            tvEventDetails.setCompoundDrawablesWithIntrinsicBounds(EventType.getIconForId(event.getType()), 0, 0, 0);
        }
        else
        {
            Bitmap bmp = BitmapFactory.decodeByteArray(icon.getIcon(), 0, icon.getIcon().length);//DO this
            if(bmp != null)
            {
                BitmapDrawable drawable = new BitmapDrawable(getResources(), bmp);
                drawable.setBounds(0, 0, getResources().getDimensionPixelOffset(R.dimen.dp32), getResources().getDimensionPixelOffset(R.dimen.dp32));
                //FIXME drawable looses aspect ratio
                tvEventDetails.setCompoundDrawables(drawable, null, null, null);
            }
            else
            {
                tvEventDetails.setCompoundDrawablesWithIntrinsicBounds(EventType.getIconForId(event.getType()), 0, 0, 0);
            }
        }

        tvEventLevelText.setText(EventLevel.getTextForInt(event.getLevel()));
    }
}
