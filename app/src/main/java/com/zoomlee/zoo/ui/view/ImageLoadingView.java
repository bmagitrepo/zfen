package com.zoomlee.zoo.ui.view;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.zoomlee.zoo.R;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 02.04.15.
 */
public class ImageLoadingView extends LoadingView {

    private final int loaderViewWidth;
    private final int loaderViewHeight;
    private final int errorDocIconWidth;
    private final int errorDocIconHeight;

    public ImageLoadingView(Context context) {
        this(context, null);
    }

    public ImageLoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setImageResource(R.drawable.loading);

        final Resources res = getResources();
        loaderViewWidth = res.getDimensionPixelSize(R.dimen.loading_spinner_width);
        loaderViewHeight = res.getDimensionPixelSize(R.dimen.loading_spinner_height);
        errorDocIconWidth = res.getDimensionPixelSize(R.dimen.error_doc_icon_width);
        errorDocIconHeight = res.getDimensionPixelSize(R.dimen.error_doc_icon_height);
    }

    public void show() {
        setImageResource(R.drawable.loading);
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams instanceof FrameLayout.LayoutParams)
            ((FrameLayout.LayoutParams)layoutParams).gravity = Gravity.CENTER;
        else if (layoutParams instanceof LinearLayout.LayoutParams)
            ((LinearLayout.LayoutParams)layoutParams).gravity = Gravity.CENTER;
        layoutParams.width = loaderViewWidth;
        layoutParams.height = loaderViewHeight;
        setLayoutParams(layoutParams);
        super.show();
    }

    public void showError() {
        hide();
        setVisibility(VISIBLE);
        setImageResource(R.drawable.error_file);
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams instanceof FrameLayout.LayoutParams)
            ((FrameLayout.LayoutParams)layoutParams).gravity = Gravity.CENTER;
        else if (layoutParams instanceof LinearLayout.LayoutParams)
            ((LinearLayout.LayoutParams)layoutParams).gravity = Gravity.CENTER;
        layoutParams.width = errorDocIconWidth;
        layoutParams.height = errorDocIconHeight;
        setLayoutParams(layoutParams);
    }
}