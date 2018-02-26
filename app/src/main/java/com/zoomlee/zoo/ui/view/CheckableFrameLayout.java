package com.zoomlee.zoo.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.FrameLayout;

/**
 * Author vbevans94.
 */
public class CheckableFrameLayout extends FrameLayout implements Checkable {

    private final Checkable checkable = new CheckableImpl(this);

    public CheckableFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setChecked(boolean checked) {
        checkable.setChecked(checked);
    }

    @Override
    public boolean isChecked() {
        return checkable != null && checkable.isChecked();
    }

    @Override
    public void toggle() {
        checkable.toggle();
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        int[] state = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(state, CheckableImpl.CHECKED_STATE);
        }
        return state;
    }
}
