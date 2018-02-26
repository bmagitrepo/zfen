package com.zoomlee.zoo.ui.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.ui.DropDownAnimation;
import com.zoomlee.zoo.ui.view.wheel.WheelDatePicker;
import com.zoomlee.zoo.utils.TimeUtil;

import java.util.Calendar;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @since 06.05.15.
 */
public class TaxDateView extends LinearLayout implements View.OnClickListener, WheelDatePicker.OnDateChangeListener {

    public static final int ANIM_DURATION = 400;
    private View headerLayout;
    private TextView nameTv;
    private TextView dateTv;
    private View topDivider;
    private View bottomDivider;
    private View separator;
    private WheelDatePicker datePicker;
    private OnStateChangedListener dropDownListener;
    private boolean checked = false;

    private int dateColorGreen;
    private int dateColorGray;
    private int dateColorDisabled;
    private int dateWheelViewHeight;

    public TaxDateView(Context context) {
        this(context, null);
    }

    public TaxDateView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TaxDateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.view_tax_date, this);

        Resources resources = context.getResources();
        dateColorGreen = resources.getColor(R.color.green_zoomlee);
        dateColorGray = resources.getColor(R.color.text_gray);
        dateColorDisabled = resources.getColor(R.color.text_disabled);
        dateWheelViewHeight = resources.getDimensionPixelSize(R.dimen.date_wheel_view_height);

        nameTv = (TextView) findViewById(R.id.nameTv);
        dateTv = (TextView) findViewById(R.id.dateTv);
        topDivider = findViewById(R.id.topDivider);
        bottomDivider = findViewById(R.id.bottomDivider);
        separator = findViewById(R.id.separator);
        datePicker = (WheelDatePicker) findViewById(R.id.dateWheelView);
        headerLayout = findViewById(R.id.header);

        datePicker.setOnDateChangedListener(this);
        headerLayout.setOnClickListener(this);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TaxDateView, 0, 0);
        String name = a.getString(R.styleable.TaxDateView_name);
        setName(name);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        headerLayout.setEnabled(enabled);

        if (!enabled && isExpanded()) changeState();

        nameTv.setTextColor(enabled ? dateColorGray : dateColorDisabled);
        dateTv.setTextColor(enabled ? dateColorGray : dateColorDisabled);
    }

    public void setDropDownListener(OnStateChangedListener dropDownListener) {
        this.dropDownListener = dropDownListener;
    }

    public void setName(String name) {
        nameTv.setText(name);
    }

    public void setCalendar(Calendar calendar) {
        datePicker.setDate(calendar);
        dateTv.setText(TimeUtil.formatDate(calendar.getTime()));
    }

    public void setTracking() {
        dateTv.setText(R.string.tracking);
    }

    public Calendar getCalendar() {
        return datePicker.getDate();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Calendar calendar = datePicker.getDate();

        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putString("name", nameTv.getText().toString());
        bundle.putLong("date", calendar.getTimeInMillis());
        bundle.putBoolean("checked", checked);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;

            nameTv.setText(bundle.getString("name"));
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(bundle.getLong("date"));
            setCalendar(calendar);
            checked = bundle.getBoolean("checked");
            updateCheckedState();

            super.onRestoreInstanceState(bundle.getParcelable("instanceState"));
            return;
        }

        super.onRestoreInstanceState(state);
    }

    private void updateCheckedState() {
        if (checked) {
            topDivider.setVisibility(VISIBLE);
            bottomDivider.setVisibility(VISIBLE);

            DropDownAnimation dropDownAnimation = new DropDownAnimation(datePicker, dateWheelViewHeight, ANIM_DURATION, false);
            datePicker.startAnimation(dropDownAnimation);
        } else {
            DropDownAnimation dropDownAnimation = new DropDownAnimation(datePicker, dateWheelViewHeight, ANIM_DURATION, true);
            dropDownAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    topDivider.setVisibility(GONE);
                    bottomDivider.setVisibility(GONE);

                    datePicker.setVisibility(GONE);
                    datePicker.animate().translationY(0).setDuration(0).setListener(null).start();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            datePicker.startAnimation(dropDownAnimation);
        }

        separator.setVisibility(checked ? GONE : VISIBLE);
        dateTv.setTextColor(checked ? dateColorGreen : dateColorGray);
    }

    @Override
    public void onClick(View v) {
        changeState();
    }

    /**
     * collapse/expand Date Wheel
     */
    public void changeState() {
        checked = !checked;
        if (dropDownListener != null && checked) {
            dropDownListener.onDropDown();
        }
        updateCheckedState();
    }

    @Override
    public void onDateChanged() {
    }

    @Override
    public void onDateScrollingFinished() {
        Calendar date = datePicker.getDate();
        dateTv.setText(TimeUtil.formatDate(date.getTime()));
        if (dropDownListener != null) {
            dropDownListener.onDateChanged(date);
        }
    }

    public boolean isExpanded() {
        return checked;
    }

    public interface OnStateChangedListener {
        void onDropDown();
        void onDateChanged(Calendar calendar);
    }
}
