package com.zoomlee.zoo.ui.view.field;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.model.Field;
import com.zoomlee.zoo.ui.DatePickerDialog;
import com.zoomlee.zoo.utils.TimeUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @since 2/18/15
 */
public class FieldDateEditView extends FieldEditView implements View.OnClickListener {

    private Calendar selectedDate = TimeUtil.getCalendarForCurrentTime();
    private Calendar remindOnDate;
    private boolean noReminder;
    private SimpleDateFormat format;

    public FieldDateEditView(Context context) {
        this(context, null);
    }

    public FieldDateEditView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FieldDateEditView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, true);
    }

    protected FieldDateEditView(Context context, AttributeSet attrs, int defStyleAttr, boolean initUi) {
        super(context, attrs, defStyleAttr);
        if (initUi) {
            initUi(attrs);
        }
    }

    private void initUi(AttributeSet attrs) {
        setOrientation(VERTICAL);
        int padding = getResources().getDimensionPixelSize(R.dimen.horizontal_margin);
        setPadding(padding, 0, padding, 0);

        inflate(getContext(), R.layout.view_field_date, this);

        titleView = (TextView) findViewById(R.id.titleTv);
        valueView = (TextView) findViewById(R.id.valueTv);

        setOnClickListener(this);

        if (isInEditMode()) {
            return;
        }

        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.FieldDateEditView, 0, 0);
        try {
            noReminder = array.getBoolean(R.styleable.FieldDateEditView_no_alert, false);
            String title = array.getString(R.styleable.FieldDateEditView_title);
            String dateFormat = array.getString(R.styleable.FieldDateEditView_date_format);
            titleView.setText(title);
            if (dateFormat != null)
                format = new SimpleDateFormat(dateFormat, Locale.US);
        } finally {
            array.recycle();
        }
    }

    @Override
    public void setValue(String value) {
        String notifyOn = super.getField().getNotifyOn();
        if (!noReminder && notifyOn != null) {
            try {
                remindOnDate = TimeUtil.getCalendarForServerTime(Long.parseLong(notifyOn));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            valueView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.alert_clock, 0, 0, 0);
        } else {
            valueView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            remindOnDate = null;
        }

        if (value == null) {
            return;
        }
        try {
            selectedDate.setTime(TimeUtil.parseDateUTC(value));
            valueView.setText(value);
        } catch (ParseException pe) {
            pe.printStackTrace();
            valueView.setText(TimeUtil.formatDateUTC(selectedDate.getTime()));
        }
    }

    public void setValue(Calendar calendar) {
        selectedDate = calendar;
        if (format == null) {
            valueView.setText(TimeUtil.formatDateUTC(calendar.getTime()));
        } else {
            valueView.setText(format.format(calendar.getTime()));
        }
    }

    public Calendar getCalendarValue() {
        return selectedDate;
    }

    @Override
    public String getValue() {
        return valueView.getText().toString();
    }

    @Override
    public void onClick(View v) {
        if (remindOnDate == null && field != null && field.getReminder() != -1) {
            remindOnDate = (Calendar) selectedDate.clone();
            remindOnDate.add(Calendar.DAY_OF_MONTH, 0-field.getReminder());
        }
        DatePickerDialog dialog = new DatePickerDialog(getContext(), selectedDate, remindOnDate, noReminder);
        dialog.setOnActionListener(new DatePickerDialog.OnPickerDialogActionListener() {
            @Override
            public void onCancel() {
            }

            @Override
            public void onSelectDate(Calendar choosedDate, Calendar remindOn) {
                if (format == null) {
                    valueView.setText(TimeUtil.formatDateUTC(choosedDate.getTime()));
                } else {
                    valueView.setText(format.format(choosedDate.getTime()));
                }
                selectedDate = choosedDate;
                remindOnDate = remindOn;
                valueView.setCompoundDrawablesWithIntrinsicBounds(remindOnDate == null ? 0 : R.drawable.alert_clock, 0, 0, 0);
            }
        });
        dialog.show();
    }

    @Override
    public Field getField() {
        Field field = super.getField();
        field.setValue(String.valueOf(selectedDate.getTimeInMillis() / 1000));
        if (remindOnDate != null)
            field.setNotifyOn(String.valueOf(remindOnDate.getTimeInMillis() / 1000));
        else
            field.setNotifyOn(null);
        return field;
    }
}
