package com.zoomlee.zoo.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.ListView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.ui.adapters.RadioListAdapter;
import com.zoomlee.zoo.ui.view.wheel.WheelDatePicker;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @date 2/20/15
 */
public class DatePickerDialog extends Dialog implements View.OnClickListener {

    private WheelDatePicker datePicker;
    private RadioListAdapter timerAdapter;
    private long[] timerValues;
    private boolean noReminder;

    public interface OnPickerDialogActionListener {
        void onCancel();

        void onSelectDate(Calendar selectedDate, Calendar remindOn);
    }

    private OnPickerDialogActionListener listener;

    public DatePickerDialog(Context context, Calendar timestamp, Calendar remindOn, boolean noReminder) {
        super(context, R.style.AppTheme);

        setContentView(R.layout.dialog_date_picker);

        Resources resources = context.getResources();

        int[] timerValuesSecs = resources.getIntArray(R.array.timer_values);
        timerValues = new long[timerValuesSecs.length];
        int i = 0;
        for (int time : timerValuesSecs) {
            timerValues[i++] = time == -1 ? -1 : 1000L * time;
        }

        ListView listView = (ListView) findViewById(android.R.id.list);
        String[] timerNames = resources.getStringArray(R.array.timer_names);
        timerAdapter = new RadioListAdapter(context, timerNames, getSelectedRemindPosition(timestamp, remindOn));
        listView.setAdapter(timerAdapter);

        this.noReminder = noReminder;
        datePicker = (WheelDatePicker) findViewById(R.id.date_picker);
        datePicker.setDate(timestamp);

        findViewById(R.id.submitFrame).setOnClickListener(this);
        findViewById(R.id.cancelFrame).setOnClickListener(this);

        if (noReminder) {
            findViewById(R.id.remindMeTv).setVisibility(View.GONE);
            findViewById(android.R.id.list).setVisibility(View.GONE);
        }
    }

    private int getSelectedRemindPosition(Calendar timestamp, Calendar remindOn) {
        if (remindOn == null) {
            return 0;
        }

        long diff = timestamp.getTimeInMillis() - remindOn.getTimeInMillis();
        if (diff < 0) {
            return 0;
        }

        int remindPosition = 0;
        for (int i = 1; i < timerValues.length - 1; i++) {
            if (diff <= timerValues[i] || diff < (timerValues[i+1] + timerValues[i]) / 2) {
                return i;
            }
        }
        if (remindPosition == 0) {
            remindPosition = timerValues.length - 1;
        }

        return remindPosition;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.submitFrame:
                if (listener != null) {
                    Calendar chosen = datePicker.getDate();
                    Calendar remindOn = null;
                    if(!noReminder && timerAdapter.getSelectedPosition() != -1 && timerValues[timerAdapter.getSelectedPosition()] != -1) {
                        remindOn = Calendar.getInstance();
                        remindOn.setTimeZone(TimeZone.getTimeZone("UTC"));
                        long remindOnMs = chosen.getTimeInMillis() - timerValues[timerAdapter.getSelectedPosition()];
                        remindOn.setTimeInMillis(remindOnMs);
                    }
                    listener.onSelectDate(chosen, remindOn);
                    dismiss();
                }
                break;
            case R.id.cancelFrame:
                if (listener != null) {
                    listener.onCancel();
                    dismiss();
                }
                break;
        }
    }

    public void setOnActionListener(OnPickerDialogActionListener listener) {
        this.listener = listener;
    }
}
