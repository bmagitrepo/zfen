package com.zoomlee.zoo.ui.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Display;
import android.widget.ListView;

/**
 * Author vbevans94.
 */
public class HalfScreenListView extends ListView {

    private final int maxHeight;

    public HalfScreenListView(Context context, AttributeSet attrs) {
        super(context, attrs);

        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        maxHeight = size.y / 2;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int measuredHeight = getMeasuredHeight();
        if (measuredHeight > maxHeight) {
            setMeasuredDimension(getMeasuredWidth(), maxHeight);
            awakenScrollBars();
        }
    }
}
