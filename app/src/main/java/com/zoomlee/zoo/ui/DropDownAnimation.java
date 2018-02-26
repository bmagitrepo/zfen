package com.zoomlee.zoo.ui;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @since 12.05.15.
 */
public class DropDownAnimation extends Animation {

    private final View animateView;
    private final int viewHeight;
    private final boolean hide;

    public DropDownAnimation(View animateView, int viewHeight, int duration, boolean hide) {
        this.animateView = animateView;
        this.viewHeight = viewHeight;
        this.hide = hide;


        setDuration(duration);

        if(!hide) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) animateView.getLayoutParams();
            params.topMargin = 0 - viewHeight;
            animateView.setLayoutParams(params);
            this.animateView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) animateView.getLayoutParams();
        if (interpolatedTime < 1.0f) {
            if(hide) {
                params.topMargin = 0 - (int)(viewHeight * interpolatedTime);
            } else {
                params.topMargin = (int)(viewHeight * interpolatedTime) - viewHeight;
            }
        } else {
            if(hide) {
                animateView.setVisibility(View.GONE);
                params.topMargin = 0 - viewHeight;
            } else {
                params.topMargin = 0;
            }
        }
        animateView.setLayoutParams(params);
    }
}
