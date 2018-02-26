package com.zoomlee.zoo.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextSwitcher;

/**
 * Author vbevans94.
 */
public class BetterTextSwitcher extends TextSwitcher {

    public BetterTextSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setBackgroundResource(int resid) {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        super.setBackgroundResource(resid);

        setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }
}
