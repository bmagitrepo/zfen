package com.zoomlee.zoo.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zoomlee.zoo.R;

import java.security.InvalidParameterException;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @since 1/19/15
 */
public class NavigationDrawerItemView extends RelativeLayout implements Checkable {

    private final Checkable checkable = new CheckableImpl(this);

    private TextView label;
    private ImageView icon;
    private TextView reminder;

    public NavigationDrawerItemView(Context context) {
        this(context, null);
    }

    public NavigationDrawerItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NavigationDrawerItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initUi(context, attrs);
        setUpdatesCount(0);
    }

    private void initUi(Context context, AttributeSet attrs) {
        inflate(context, R.layout.view_navdrawer_item, this);

        label = (TextView) findViewById(R.id.labelTv);
        icon = (ImageView) findViewById(R.id.iconIv);
        reminder = (TextView) findViewById(R.id.newUpdateTv);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.NavigationDrawerItemView, 0, 0);
        String showText = a.getString(R.styleable.NavigationDrawerItemView_text);
        Drawable showIcon = a.getDrawable(R.styleable.NavigationDrawerItemView_itemIcon);
        a.recycle();

        label.setText(showText);
        icon.setImageDrawable(showIcon);
    }

    public void setUpdatesCount(int newCount) {
        if (newCount < 0) {
            throw new InvalidParameterException("Update count can't be less than 0!");
        }

        if (newCount == 0) {
            reminder.setVisibility(View.INVISIBLE);
        } else if (newCount < 10) {
            reminder.setVisibility(View.VISIBLE);
            reminder.setBackgroundResource(R.drawable.updates_background);
        } else {
            reminder.setVisibility(View.VISIBLE);
            reminder.setBackgroundResource(R.drawable.updates_background_long);
        }

        reminder.setText(Integer.toString(newCount));
    }

    public void setText(CharSequence text) {
        label.setText(text);
    }

    public void setIcon(Drawable drawable) {
        icon.setImageDrawable(drawable);
    }

    @Override
    public void setChecked(boolean checked) {
        checkable.setChecked(checked);
    }

    @Override
    public boolean isChecked() {
        return checkable.isChecked();
    }

    @Override
    public void toggle() {
        checkable.toggle();
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        int[] state = super.onCreateDrawableState(extraSpace + 1);
        if (checkable != null && checkable.isChecked()) {
            state = mergeDrawableStates(state, CheckableImpl.CHECKED_STATE);
        }
        return state;
    }
}
