package com.zoomlee.zoo.ui.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.zoomlee.zoo.R;

import butterknife.ButterKnife;
import butterknife.BindView;

/**
 * Author vbevans94.
 */
public class AppItemView extends FrameLayout {

    @BindView(R.id.image_icon)
    ImageView imageIcon;

    @BindView(R.id.text_label)
    TextView textLabel;

    public AppItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        ButterKnife.bind(this);
    }

    public void bind(Drawable icon, CharSequence label) {
        imageIcon.setImageDrawable(icon);
        textLabel.setText(label);
    }
}
