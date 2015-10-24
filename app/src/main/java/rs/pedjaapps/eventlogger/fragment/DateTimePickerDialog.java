package rs.pedjaapps.eventlogger.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.sql.Time;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import rs.pedjaapps.eventlogger.R;

/**
 * Created by pedja on 21.4.14..
 */
public class DateTimePickerDialog extends DialogFragment
{
    public static final String ARGS = "args";
    public static final String EXTRA_DATE = "date";
    public DateSetListener dateSetListener;

    public Date date;

    public static DateTimePickerDialog newInstance(Date date)
    {
        DateTimePickerDialog dialog = new DateTimePickerDialog();
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_DATE, date);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        date = (Date) getArguments().getSerializable(EXTRA_DATE);
        if (date == null)
        {
            date = new Date();
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.layout_date_time_picker, null);
        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.datePicker);
        final TimePicker timePicker = (TimePicker) view.findViewById(R.id.timePicker);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null);
        timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
        builder.setView(view);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, datePicker.getYear());
                calendar.set(Calendar.MONTH, datePicker.getMonth());
                calendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                calendar.set(Calendar.MINUTE, timePicker.getCurrentMinute());
                Date date = calendar.getTime();
                if (dateSetListener != null)
                {
                    dateSetListener.onDateSet(date);
                }
            }
        });
        return builder.create();
    }

    public void setDateSetListener(DateSetListener dateSetListener)
    {
        this.dateSetListener = dateSetListener;
    }

    public static interface DateSetListener
    {
        public void onDateSet(Date date);
    }
}
