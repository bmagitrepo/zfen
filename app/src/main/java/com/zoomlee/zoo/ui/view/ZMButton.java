package com.zoomlee.zoo.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;


public class ZMButton extends Button {

    public ZMButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(attrs);
    }

    public ZMButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(attrs);
    }

    private void init(AttributeSet attrs) {
        ZMTextView.considerTypefont(this, attrs);
    }
}
