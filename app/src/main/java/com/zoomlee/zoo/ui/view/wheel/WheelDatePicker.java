package com.zoomlee.zoo.ui.view.wheel;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.ui.view.wheel.adapters.ArrayWheelAdapter;
import com.zoomlee.zoo.ui.view.wheel.adapters.NumericWheelAdapter;

import java.util.Calendar;
import java.util.TimeZone;

import butterknife.ButterKnife;
import butterknife.BindView;

/**
 * Author vbevans94.
 */
public class WheelDatePicker extends RelativeLayout implements OnWheelChangedListener, OnWheelScrollListener {

    @BindView(R.id.year)
    WheelView wheelYear;

    @BindView(R.id.month)
    WheelView wheelMonth;

    @BindView(R.id.day)
    WheelView wheelDay;

    @BindView(R.id.view_selected_background)
    View viewSelectedBackground;

    private OnDateChangeListener changeListener;
    /**
     * Timezone of the date picker. To be able to receive in the same time zone as we set it.
     */
    private TimeZone timeZone;

    public WheelDatePicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_wheel_date_picker, this);

        ButterKnife.bind(this);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.WheelDatePicker, 0, 0);

        int selectedBackground = array.getResourceId(R.styleable.WheelDatePicker_selectedBackground, android.R.color.transparent);
        viewSelectedBackground.setBackgroundResource(selectedBackground);

        String font = array.getString(R.styleable.WheelDatePicker_textFont);
        wheelDay.setFont(font);
        wheelMonth.setFont(font);
        wheelYear.setFont(font);

        int fromColor = array.getColor(R.styleable.WheelDatePicker_shadowFrom, Color.WHITE);
        int toColor = array.getColor(R.styleable.WheelDatePicker_shadowTo, Color.TRANSPARENT);
        wheelDay.setShadows(fromColor, toColor);
        wheelMonth.setShadows(fromColor, toColor);
        wheelYear.setShadows(fromColor, toColor);

        int textColor = array.getColor(R.styleable.WheelDatePicker_textColor, Color.BLACK);
        int selectedTextColor = array.getColor(R.styleable.WheelDatePicker_textSelectedColor, Color.BLACK);
        wheelDay.setTextColors(selectedTextColor, textColor);
        wheelMonth.setTextColors(selectedTextColor, textColor);
        wheelYear.setTextColors(selectedTextColor, textColor);

        array.recycle();

        // update listeners
        OnWheelChangedListener updateDaysListener = new OnWheelChangedListener() {
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                updateDays();
            }
        };

        wheelMonth.addChangingListener(updateDaysListener);
        wheelYear.addChangingListener(updateDaysListener);
    }

    public void setDate(Calendar calendar) {
        timeZone = calendar.getTimeZone();

        int selectedMonth = calendar.get(Calendar.MONTH);
        String months[] = getContext().getResources().getStringArray(R.array.months_name);
        wheelMonth.setViewAdapter(new ArrayWheelAdapter<>(getContext(), months, R.layout.item_wheel));
        wheelMonth.setCurrentItem(selectedMonth);

        int selectedYear = calendar.get(Calendar.YEAR);
        int curYear = Calendar.getInstance().get(Calendar.YEAR);
        wheelYear.setViewAdapter(new NumericWheelAdapter(getContext(), curYear - 150, curYear + 150, R.layout.item_wheel));
        wheelYear.setCurrentItem(selectedYear - curYear + 150);

        updateDays();
        wheelDay.setCurrentItem(calendar.get(Calendar.DAY_OF_MONTH) - 1);
    }

    public void setOnDateChangedListener(OnDateChangeListener listener) {
        // clear listeners to prevent multiple firing of the same event
        wheelDay.removeChangingListener(this);
        wheelMonth.removeChangingListener(this);
        wheelYear.removeChangingListener(this);

        // register self on the change in any wheel
        wheelDay.addChangingListener(this);
        wheelMonth.addChangingListener(this);
        wheelYear.addChangingListener(this);

        wheelDay.removeScrollingListener(this);
        wheelMonth.removeScrollingListener(this);
        wheelYear.removeScrollingListener(this);

        wheelDay.addScrollingListener(this);
        wheelMonth.addScrollingListener(this);
        wheelYear.addScrollingListener(this);

        changeListener = listener;
    }

    private void updateDays() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 150 + wheelYear.getCurrentItem());
        calendar.set(Calendar.MONTH, wheelMonth.getCurrentItem());

        int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        wheelDay.setViewAdapter(new NumericWheelAdapter(getContext(), 1, maxDays, R.layout.item_wheel));
        int curDay = Math.min(maxDays, wheelDay.getCurrentItem() + 1);
        wheelDay.setCurrentItem(curDay - 1, true);
    }

    public Calendar getDate() {
        Calendar chosen = Calendar.getInstance();
        chosen.setTimeZone(timeZone);
        int yearValue = chosen.get(Calendar.YEAR) - 150 + wheelYear.getCurrentItem();
        int monthValue = wheelMonth.getCurrentItem();
        int dayValue = wheelDay.getCurrentItem() + 1;
        chosen.set(yearValue, monthValue, dayValue);

        return chosen;
    }

    @Override
    public void onChanged(WheelView wheel, int oldValue, int newValue) {
        if (changeListener != null) {
            changeListener.onDateChanged();
        }
    }

    @Override
    public void onScrollingStarted(WheelView wheel) {
    }

    @Override
    public void onScrollingFinished(WheelView wheel) {
        if (changeListener != null) {
            changeListener.onDateScrollingFinished();
        }
    }

    public interface OnDateChangeListener {

        /**
         * Triggered every time some wheel value change.
         */
        void onDateChanged();

        /**
         * Triggered when wheel changing finished.
         */
        void onDateScrollingFinished();
    }

    public static class OnDateChangeListenerAdapter implements OnDateChangeListener {

        @Override
        public void onDateChanged() {
        }

        @Override
        public void onDateScrollingFinished() {
        }
    }
}
