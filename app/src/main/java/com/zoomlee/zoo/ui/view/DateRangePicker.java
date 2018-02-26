package com.zoomlee.zoo.ui.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.ui.MaterialDialog;
import com.zoomlee.zoo.ui.view.wheel.WheelDatePicker;
import com.zoomlee.zoo.utils.PreferencesKeys;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;
import com.zoomlee.zoo.utils.TimeUtil;
import com.zoomlee.zoo.utils.UiUtil;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Author vbevans94.
 */
public class DateRangePicker extends FrameLayout implements WheelDatePicker.OnDateChangeListener {

    private static final ActionBar.LayoutParams ACTION_PARAMS
            = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    @BindView(R.id.text_range_from)
    ZMTextView textFrom;

    @BindView(R.id.text_range_to)
    ZMTextView textTo;

    @BindView(R.id.date_picker)
    WheelDatePicker datePicker;

    @BindView(R.id.cover)
    View cover;

    @BindView(R.id.layout_end_toggle)
    View layoutEndToggle;

    private RangeState rangeState;
    private Calendar toDate;
    private Calendar fromDate;
    private AppCompatActivity openerActivity;
    private View toolbar;
    // remember navigation icon and inset, to restore after moving to normal action bar
    private Drawable navigationIcon;
    private int insetStart;
    private int insetEnd;
    private Calendar initToDate;
    private Calendar initFromDate;
    private final int pickerHeight;
    // Indicate that collapsing is in progress.
    private boolean collapsing;
    // Indicate that expanding is in progress.
    private boolean expanding;
    private RangeSetListener rangeSetListener;

    public DateRangePicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.date_range_picker, this);
        ButterKnife.bind(this);

        toolbar = View.inflate(context, R.layout.toolbar_cancel_ok, null);
        ButterKnife.bind(new ToolbarListeners(), toolbar);

        // get "to" and "from" dates
        // we use last set value or start of the year to this day range in first use
        long lastTo = SharedPreferenceUtils.getUtils().getLongSetting(PreferencesKeys.TO_DATE);
        if (lastTo == 0l) {
            toDate = Calendar.getInstance();
        } else {
            toDate = Calendar.getInstance();
            toDate.setTime(new Date(lastTo));
        }

        long lastFrom = SharedPreferenceUtils.getUtils().getLongSetting(PreferencesKeys.FROM_DATE);
        if (lastFrom == 0l) {
            fromDate = Calendar.getInstance();
            fromDate.set(Calendar.DAY_OF_MONTH, 1);
            fromDate.set(Calendar.MONTH, Calendar.JANUARY);
        } else {
            fromDate = Calendar.getInstance();
            fromDate.setTime(new Date(lastFrom));
        }
        trimDates();

        datePicker.setOnDateChangedListener(this);

        // set texts to dates
        textTo.setText(TimeUtil.formatDate(toDate.getTime()));
        textFrom.setText(TimeUtil.formatDate(fromDate.getTime()));

        // initially we are on "from" end
        rangeState = RangeState.FROM;
        datePicker.setDate(fromDate);

        pickerHeight = getResources().getDimensionPixelSize(R.dimen.range_picker_height);
    }

    public void trimDates() {
        toDate.set(Calendar.HOUR_OF_DAY, 23);
        toDate.set(Calendar.MINUTE, 59);
        toDate.set(Calendar.MILLISECOND, 999);
        fromDate.set(Calendar.HOUR_OF_DAY, 0);
        fromDate.set(Calendar.MINUTE, 0);
        fromDate.set(Calendar.MILLISECOND, 0);
    }

    private void expand() {
        if (!isExpanded() && !expanding) {
            expanding = true;
            // remember to restore in case of cancelling all work
            initFromDate = fromDate;
            initToDate = toDate;

            // indicate that we are opened by remembering activity
            final AppCompatActivity activity = (AppCompatActivity) getContext();

            // prepare for animation
            datePicker.setY(-pickerHeight);
            UiUtil.show(datePicker);

            setCustomActionBar(activity, true);
            datePicker.animate().translationY(0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    openerActivity = activity;
                    expanding = false;
                }
            });
            // show cover
            UiUtil.fadeIn(cover);
        }
    }

    private void collapse(boolean save) {
        if (isExpanded() && !collapsing) {
            collapsing = true;
            datePicker.animate().translationY(-pickerHeight).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    UiUtil.hide(datePicker);
                    setCustomActionBar(openerActivity, false);

                    // indicate we are closed
                    openerActivity = null;
                    collapsing = false;

                    // remove checked state from selected range end
                    textFrom.setChecked(false);
                    textTo.setChecked(false);
                }
            }).start();

            // hide cover
            UiUtil.fadeOut(cover);

            if (!save) {
                // if not saving we must restore previous values
                toDate = initToDate;
                fromDate = initFromDate;
                textFrom.setText(TimeUtil.formatDate(fromDate.getTime()));
                textTo.setText(TimeUtil.formatDate(toDate.getTime()));
            }
        }
    }

    private boolean isExpanded() {
        return openerActivity != null;
    }

    private void setCustomActionBar(AppCompatActivity activity, boolean custom) {
        ActionBar actionBar = activity.getSupportActionBar();
        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
        if (custom) {
            navigationIcon = toolbar.getNavigationIcon();
            insetStart = toolbar.getContentInsetStart();
            insetEnd = toolbar.getContentInsetEnd();
            toolbar.setContentInsetsAbsolute(0, 0);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(this.toolbar, ACTION_PARAMS);
        } else {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
            toolbar.setNavigationIcon(navigationIcon);
            toolbar.setContentInsetsAbsolute(insetStart, insetEnd);
        }
    }

    class ToolbarListeners {
        @OnClick(R.id.cancelFrame)
        @SuppressWarnings("unused")
        void onCancelClicked() {
            collapse(false);
        }

        @OnClick(R.id.submitFrame)
        @SuppressWarnings("unused")
        void onSubmitClicked() {
            // check if "from" not bigger than "to"
            if (toDate.before(fromDate)) {
                new MaterialDialog(getContext())
                        .setTitle(R.string.title_cannot_set_filter)
                        .setMessage(R.string.message_start_before_end)
                        .setCancelable(false)
                        .setPositiveButton(R.string.ok_upper, null)
                        .show();
                return;
            }

            // save values for future use
            SharedPreferenceUtils.getUtils().setLongSetting(PreferencesKeys.FROM_DATE, fromDate.getTime().getTime());
            SharedPreferenceUtils.getUtils().setLongSetting(PreferencesKeys.TO_DATE, toDate.getTime().getTime());

            collapse(true);

            if (rangeSetListener != null) {
                rangeSetListener.onRangeSet();
            }
        }
    }

    @OnClick(R.id.text_range_from)
    @SuppressWarnings("unused")
    void onFromClicked() {
        // expand self if not yet
        expand();

        rangeState = RangeState.FROM;
        // visually change state
        textFrom.setChecked(true);
        textTo.setChecked(false);

        datePicker.setDate(fromDate);
    }

    @OnClick(R.id.text_range_to)
    @SuppressWarnings("unused")
    void onToClicked() {
        expand();

        rangeState = RangeState.TO;
        // visually change state
        textFrom.setChecked(false);
        textTo.setChecked(true);

        datePicker.setDate(toDate);
    }

    @OnClick(R.id.cover)
    @SuppressWarnings("unused")
    void onCover() {
        collapse(false);
    }

    @Override
    public void onDateChanged() {
        Calendar newDate = datePicker.getDate();
        if (rangeState == RangeState.FROM) {
            fromDate = newDate;
            textFrom.setText(TimeUtil.formatDate(newDate.getTime()));
        } else {
            toDate = newDate;
            textTo.setText(TimeUtil.formatDate(newDate.getTime()));
        }

        trimDates();
    }

    @Override
    public void onDateScrollingFinished() {
    }

    /**
     * Sets view above which picker should be to set margin.
     *
     * @param view to set
     */
    public void above(View view) {
        FrameLayout.LayoutParams params = (LayoutParams) view.getLayoutParams();
        params.topMargin = getResources().getDimensionPixelSize(R.dimen.button_height);
        view.setLayoutParams(params);
    }

    /**
     * Sets callback listener.
     *
     * @param listener to be set
     */
    public void setRangeSetListener(RangeSetListener listener) {
        rangeSetListener = listener;
    }

    /**
     * @return "from" date
     */
    public Calendar getFromDate() {
        return fromDate;
    }

    /**
     * @return "to" date
     */
    public Calendar getToDate() {
        return toDate;
    }

    private enum RangeState {

        FROM, TO
    }

    public interface RangeSetListener {

        void onRangeSet();
    }
}
