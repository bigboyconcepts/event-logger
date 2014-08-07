package rs.pedjaapps.eventlogger.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

import rs.pedjaapps.eventlogger.MainApp;
import rs.pedjaapps.eventlogger.R;
import rs.pedjaapps.eventlogger.constants.EventLevel;
import rs.pedjaapps.eventlogger.constants.EventType;
import rs.pedjaapps.eventlogger.model.Event;
import rs.pedjaapps.eventlogger.utility.SettingsManager;
import rs.pedjaapps.eventlogger.utility.Utility;

/**
 * Created by pedja on 12.4.14..
 */
public class EventAdapter extends ArrayAdapter<Event>
{
	DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
	
    public EventAdapter(Context context, List<Event> events)
    {
        super(context, 0, events);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;
        Event event = getItem(position);

        if(convertView == null)
        {
            convertView = ((LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.event_item_layout, null);
            holder = new ViewHolder();
            holder.ivType = (ImageView)convertView.findViewById(R.id.ivType);
            holder.tvColorStamp = (TextView)convertView.findViewById(R.id.tvColorStamp);
            holder.tvDescription = (TextView)convertView.findViewById(R.id.tvDescription);
            holder.tvTimestamp = (TextView)convertView.findViewById(R.id.tvTimestamp);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvDescription.setText(Html.fromHtml(event.getShort_desc()));
        holder.tvColorStamp.setBackgroundColor(EventLevel.getLevelForInt(event.getLevel()).color());
        
		if("passed".equals(SettingsManager.getTimeDisplay()))
		{
			holder.tvTimestamp.setText(Utility.getTime(event.getTimestamp().getTime()));
		}
		else
		{
			holder.tvTimestamp.setText(format.format(event.getTimestamp()));
		}
        byte[] icon = event.getIcon();
        if(icon == null || icon.length == 0)
        {
            holder.ivType.setImageResource(EventType.getIconForId(event.getType()));
        }
        else
        {
            /*Bitmap bmp = BitmapFactory.decodeByteArray(icon, 0, icon.length);//FIXME async decoding icon
            if(bmp != null)
            {
                holder.ivType.setImageBitmap(bmp);
            }
            else
            {
                holder.ivType.setImageResource(EventType.getIconForId(event.getType()));
            }*/
            DisplayImageOptions dio = new DisplayImageOptions.Builder().cloneFrom(MainApp.getInstance().getDefaultDisplayImageOptions()).extraForDownloader(icon).build();
            ImageLoader.getInstance().displayImage("db://" + event.getId(), holder.ivType, dio);
        }

        return convertView;
    }

    class ViewHolder
    {
        TextView tvColorStamp, tvTimestamp, tvDescription;
        ImageView ivType;
    }
}
