package com.zoomlee.zoo.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.zoomlee.zoo.R;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 02.04.15.
 */
public class LoadingView extends ImageView {
    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setImageResource(R.drawable.loading);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.LoadingView, 0, 0);
        boolean run = array.getBoolean(R.styleable.LoadingView_init_run, false);
        array.recycle();

        if (run) {
            show();
        }
    }

    public void show() {
        setVisibility(View.VISIBLE);
        RotateAnimation r = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        r.setDuration(1000);
        r.setInterpolator(new LinearInterpolator());
        r.setRepeatCount(Animation.INFINITE);
        startAnimation(r);
    }

    public void hide() {
        clearAnimation();
        setVisibility(View.GONE);
    }
}
