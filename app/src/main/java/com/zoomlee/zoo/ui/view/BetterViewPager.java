package com.zoomlee.zoo.ui.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Author vbevans94.
 */
public class BetterViewPager extends ViewPager {

    private boolean enableScrolling = true;

    public BetterViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setEnableScrolling(boolean enableScrolling) {
        this.enableScrolling = enableScrolling;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return enableScrolling && super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return enableScrolling && super.onInterceptTouchEvent(ev);
    }
}
