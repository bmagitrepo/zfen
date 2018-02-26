package com.zoomlee.zoo.ui;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @date 1/26/15
 */
public class CustomDrawerLayout extends DrawerLayout {
    public CustomDrawerLayout(Context context) {
        super(context);
    }

    public CustomDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private View mView;
    private boolean lastTouchOnDrawer = true;
    private boolean scrollBreaked = true;

    public void setView(View v) {
        mView = v;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int[] leftTop = new int[2];
        mView.getLocationOnScreen(leftTop);
        int height = mView.getHeight();
        int width = mView.getWidth();
        Rect r = new Rect(leftTop[0], leftTop[1], leftTop[0] + width, leftTop[1] + height);
        try {
            boolean currentTouchOnDrawer = !r.contains((int) ev.getX(), (int) ev.getY());
            if (currentTouchOnDrawer != lastTouchOnDrawer) {
                lastTouchOnDrawer = currentTouchOnDrawer;
                if (scrollBreaked && ev.getAction() == MotionEvent.ACTION_MOVE)
                    ev.setAction(MotionEvent.ACTION_DOWN);
            }

            scrollBreaked = ev.getAction() == MotionEvent.ACTION_UP;

            if (!currentTouchOnDrawer) {
                return mView.dispatchTouchEvent(ev);
            } else {
                return super.onTouchEvent(ev);
            }
        } catch (IllegalArgumentException iae) {
            return false;
        }
    }
}
