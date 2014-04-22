package rs.pedjaapps.eventlogger.fragment;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import rs.pedjaapps.eventlogger.R;
import rs.pedjaapps.eventlogger.constants.Constants;
import rs.pedjaapps.eventlogger.utility.SettingsManager;

/**
 * Created by pedja on 13.4.14..
 */
public class EventFilterDialog extends DialogFragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener
{

    LinearLayout llTimeFilter, llTypeFilter, llLevelFilter;
    public static DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());

    EditText edTimeFrom;
    EditText edTimeTo;

    public static EventFilterDialog newInstance()
    {
        EventFilterDialog fragment = new EventFilterDialog();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.filder_dialog_layout, null);

        //GENERAL
        llTimeFilter = (LinearLayout)view.findViewById(R.id.llTimeFilter);
        llTypeFilter = (LinearLayout)view.findViewById(R.id.llTypeFilter);
        llLevelFilter = (LinearLayout)view.findViewById(R.id.llLevelFilter);

        CheckBox cbTimeFilter = (CheckBox)view.findViewById(R.id.cbTimeFilter);
        cbTimeFilter.setOnCheckedChangeListener(this);
        CheckBox cbTypeFilter = (CheckBox)view.findViewById(R.id.cbTypeFilter);
        cbTypeFilter.setOnCheckedChangeListener(this);
        CheckBox cbLevelFilter = (CheckBox)view.findViewById(R.id.cbLevelFilter);
        cbLevelFilter.setOnCheckedChangeListener(this);
        cbTimeFilter.setChecked(SettingsManager.isTimeFilterEnabled());
        cbTypeFilter.setChecked(SettingsManager.isTypeFilterEnabled());
        cbLevelFilter.setChecked(SettingsManager.isLevelFilterEnabled());
        llTimeFilter.setVisibility(cbTimeFilter.isChecked() ? View.VISIBLE : View.GONE);
        llTypeFilter.setVisibility(cbTypeFilter.isChecked() ? View.VISIBLE : View.GONE);
        llLevelFilter.setVisibility(cbLevelFilter.isChecked() ? View.VISIBLE : View.GONE);
        //GENERAL END

        //TIME FILTER
        edTimeFrom = (EditText) view.findViewById(R.id.edTimeFrom);
        edTimeTo = (EditText) view.findViewById(R.id.edTimeTo);
        edTimeFrom.setOnClickListener(this);
        edTimeTo.setOnClickListener(this);
        Date yesterday = new Date(new Date().getTime() - Constants.ONE_DAY_MS);
        edTimeFrom.setText(format.format(SettingsManager.getFilterTimeFrom(yesterday.getTime())));
        edTimeTo.setText(format.format(SettingsManager.getFilterTimeTo(new Date().getTime())));
        //TIME FILTER END

        //LEVEL FILTER
        CheckBox cbError = (CheckBox)view.findViewById(R.id.cbError);
        CheckBox cbWarning = (CheckBox)view.findViewById(R.id.cbWarning);
        CheckBox cbInfo = (CheckBox)view.findViewById(R.id.cbInfo);
        CheckBox cbOk = (CheckBox)view.findViewById(R.id.cbOk);
        cbError.setOnCheckedChangeListener(this);
        cbWarning.setOnCheckedChangeListener(this);
        cbInfo.setOnCheckedChangeListener(this);
        cbOk.setOnCheckedChangeListener(this);
        cbError.setChecked(SettingsManager.isFilterLevelErrorEnabled());
        cbWarning.setChecked(SettingsManager.isFilterLevelWarningEnabled());
        cbInfo.setChecked(SettingsManager.isFilterLevelInfoEnabled());
        cbOk.setChecked(SettingsManager.isFilterLevelOkEnabled());
        //LEVEL FILTER END

        builder.setView(view);
        builder.setPositiveButton(R.string.filter_now, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                //TODO filter now
            }
        });
        return builder.create();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked)
    {
        switch (compoundButton.getId())
        {
            case R.id.cbTimeFilter:
                llTimeFilter.setVisibility(checked ? View.VISIBLE : View.GONE);
                break;
            case R.id.cbTypeFilter:
                llTypeFilter.setVisibility(checked ? View.VISIBLE : View.GONE);
                break;
            case R.id.cbLevelFilter:
                llLevelFilter.setVisibility(checked ? View.VISIBLE : View.GONE);
                break;
            case R.id.cbError:
                SettingsManager.setFilterLevelError(checked);
                break;
            case R.id.cbWarning:
                SettingsManager.setFilterLevelWarning(checked);
                break;
            case R.id.cbInfo:
                SettingsManager.setFilterLevelInfo(checked);
                break;
            case R.id.cbOk:
                SettingsManager.setFilterLevelOk(checked);
                break;
        }
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.edTimeFrom:
                Date date;
                try
                {
                    date = format.parse(edTimeFrom.getText().toString());
                }
                catch (ParseException e)
                {
                    date = new Date();
                }
                DateTimePickerDialog dialog = DateTimePickerDialog.newInstance(date);
                dialog.setDateSetListener(new DateTimePickerDialog.DateSetListener()
                {
                    @Override
                    public void onDateSet(Date date)
                    {
                        edTimeFrom.setText(format.format(date));
                        SettingsManager.setTimeFilterFrom(date.getTime());
                    }
                });
                dialog.show(getChildFragmentManager(), "date_time_picker");
                break;
            case R.id.edTimeTo:
                try
                {
                    date = format.parse(edTimeTo.getText().toString());
                }
                catch (ParseException e)
                {
                    date = new Date();
                }
                dialog = DateTimePickerDialog.newInstance(date);
                dialog.setDateSetListener(new DateTimePickerDialog.DateSetListener()
                {
                    @Override
                    public void onDateSet(Date date)
                    {
                        edTimeTo.setText(format.format(date));
                        SettingsManager.setTimeFilterTo(date.getTime());
                    }
                });
                dialog.show(getChildFragmentManager(), "date_time_picker");
                break;
        }
    }
}
