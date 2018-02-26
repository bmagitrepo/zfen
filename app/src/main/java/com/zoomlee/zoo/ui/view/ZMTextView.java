package com.zoomlee.zoo.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.TextView;

import com.zoomlee.zoo.R;


public class ZMTextView extends AppCompatTextView implements Checkable {

    private final Checkable checkable = new CheckableImpl(this);

	public ZMTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		init(attrs);
	}

    public ZMTextView(Context context, AttributeSet attrs) {
		super(context, attrs);

		init(attrs);
	}

    private void init(AttributeSet attrs) {
        ZMTextView.considerTypefont(this, attrs);
    }

	public static void considerTypefont(TextView textView, AttributeSet attrs) {
        if (textView.isInEditMode()) {
            return;
        }

		Context context = textView.getContext();
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ZMTextView, 0, 0);
		
		try {
			String fontName = a.getString(R.styleable.ZMTextView_font);
			if (fontName == null) {
				fontName = context.getString(R.string.font_regular);
			}
			Typeface tf = Typeface.createFromAsset(context.getAssets(), fontName);
			textView.setTypeface(tf);
            textView.setPaintFlags(textView.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
		} finally {
			a.recycle();
		}
	}

    @Override
    public void setChecked(boolean b) {
        checkable.setChecked(b);
    }

    @Override
    public boolean isChecked() {
        return checkable.isChecked();
    }

    @Override
    public void toggle() {
        checkable.toggle();
    }

    public void setRightDrawable(int drawableRes) {
        setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableRes, 0);
    }

    public void unsetAllDrawables() {
        setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        int[] baseState = super.onCreateDrawableState(extraSpace + 1);
        if (checkable != null && isChecked()) {
            return mergeDrawableStates(baseState, CheckableImpl.CHECKED_STATE);
        }
        return baseState;
    }
}
