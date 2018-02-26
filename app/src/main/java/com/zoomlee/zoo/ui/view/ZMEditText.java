package com.zoomlee.zoo.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import com.zoomlee.zoo.R;


public class ZMEditText extends EditText {

    private final static int[] ERROR_STATE = new int[] {R.attr.state_error};

    private boolean isError;

    public ZMEditText(Context context, AttributeSet attrs) {
		super(context, attrs);

        ZMTextView.considerTypefont(this, attrs);
        setBackgroundResource(R.drawable.edit_text_background);
	}

    @Override
    public void setBackgroundResource(int resid) {
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int paddingRight = getPaddingRight();

        super.setBackgroundResource(resid);

        setPadding(0, paddingTop, paddingRight, paddingBottom);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (isError) {
            isError = false;
        }
        refreshDrawableState();
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        int[] state = super.onCreateDrawableState(extraSpace + 1);
        if (isError) {
            state = mergeDrawableStates(state, ERROR_STATE);
        }
        return state;
    }

    public void setError(boolean isErrorState) {
        isError = isErrorState;
        refreshDrawableState();
    }
}